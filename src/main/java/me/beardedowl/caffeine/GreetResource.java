
package me.beardedowl.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A simple JAX-RS resource to greet you. Examples:
 *
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 *
 * Get greeting message for Joe:
 * curl -X GET http://localhost:8080/greet/Joe
 *
 * Change greeting
 * curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 *
 * The message is returned as a JSON object.
 */
@Path("/greet")
@ApplicationScoped
public class GreetResource {

    /**
     * The greeting message provider.
     */
    private final GreetingProvider greetingProvider;

    private static Logger LOGGER = LoggerFactory.getLogger(GreetResource.class.getName());

    private Set<String> invalidNames = new HashSet<>();

    private LoadingCache<String,Optional<Message>> messageCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .refreshAfterWrite(1,TimeUnit.MINUTES)
            /**
             *  Eviction         : eviction means removal due to the policy
             *  Invalidation     : invalidation means manual removal by the caller
             *  Removal          : removal occurs as a consequence of invalidation or eviction
             */
            .evictionListener((key,value,reason) -> {
                LOGGER.warn("Expiring cache key '{}' with value '{}' --- '{}'",key,value,reason);
            })
            .removalListener((key,value,reason) -> {
                LOGGER.warn("Removing cache key '{}' with value '{}' --- '{}'",key,value,reason) ;
            })
            .recordStats()
            .build(this::createResponse);

    /**
     * Using constructor injection to get a configuration property.
     * By default this gets the value from META-INF/microprofile-config
     *
     * @param greetingConfig the configured greeting message
     */
    @Inject
    public GreetResource(GreetingProvider greetingConfig) {
        this.greetingProvider = greetingConfig;
    }

    /**
     * Return a worldly greeting message.
     *
     * @return {@link Message}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getDefaultMessage() {
        String fName = "getDefaultMessage";
        Instant start = Instant.now();
        try{
            return messageCache.get("World").get();
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }
    }

    /**
     * Return a greeting message using the name that was provided.
     *
     * @param name the name to greet
     * @return {@link Message}
     */
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Message getMessage(@PathParam("name") String name) {
        String fName = "getMessage";
        Instant start = Instant.now();
        try{
            return messageCache.get(name).get();
        }
        catch(NoSuchElementException e){
            LOGGER.error("No value present");
            throw e;
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }
    }

    @POST
    @Path("/invalid/{name}")
    public Response addInvalidName(@PathParam("name") String name){
        String fName = "addInvalidName";
        Instant start = Instant.now();
        try{
            LOGGER.info("Adding {} to invalidNames list",name);
            invalidNames.add(name);
            return Response.status(Response.Status.OK).entity(invalidNames.toString()).build();
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }

    }

    @PATCH
    @Path("/toggle-raise-exception")
    public Response toggleRaiseException(){
        String fName = "toggleRaiseException";
        Instant start = Instant.now();
        try{
            boolean newValue = ! greetingProvider.getRaiseException().get();
            greetingProvider.setRaiseException(newValue);
            String message = String.format("New value of raiseException variable is : %s",newValue);
            return Response.status(Response.Status.OK).entity(message).build();
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }
    }

    /**
     * This returns an object of Message class which has been created based on the value passed to the function
     * This is used by cache for loading new data
     * We can simulate behavior of cache when we are not able to get data (and an exception is thrown) by toggling the value of "raiseException" behavior
     * For this, use the PATCH /greet/toggle-raise-exception
     *
     * @param who
     * @return
     * @throws InterruptedException
     */
    private Optional<Message> createResponse(String who) throws InterruptedException {
        if(greetingProvider.getRaiseException().get()){
            LOGGER.error("Returning null value...");
            return Optional.empty();
        }
        else if(invalidNames.contains(who)){
            LOGGER.error("Invalid name...");
            throw new RuntimeException();
        }
        LOGGER.warn("Message not found in cache for {}. Building...",who);
        LOGGER.info("Thread sleeping for {} ms",3000);
        Thread.sleep(3000); //ms
        String msg = String.format("%s %s!", greetingProvider.getMessage(), who);

        return Optional.of(new Message(msg));
    }

    // Endpoints for cache info
    /**
     * Converts the cache into a string and returns it to user
     * Note that this does not refresh the cache BUT it removes the entries which have been marked as expired
     * Also, I believe that both the key and value objects should have toString() overridden
     * @return  Cache data in a key-value pair
     */
    @GET
    @Path("/cache/message/data")
    public String getMessageCacheData(){
        Instant start = Instant.now();
        String fName = "getMessageCacheData";
        try{
            return messageCache.asMap().entrySet().toString();
        } finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} operation completed in {} ms", fName,duration.toMillis());
        }
    }

    /**
     * Get stats recorded by cache
     * The ability to record stats must be activated while building the cache
     * @return Cache stats
     */
    @GET
    @Path("/cache/message/stats")
    public String getMessageCacheStats(){
        Instant start = Instant.now();
        String fName = "getMessageCacheStats";
        try{
            return messageCache.stats().toString();
        } finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} operation completed in {} ms", fName,duration.toMillis());
        }
    }

}
