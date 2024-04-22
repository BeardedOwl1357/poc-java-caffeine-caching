
package me.beardedowl.caffeine.basic.usage;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.PathParam;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import me.beardedowl.caffeine.singleton.cache.CacheSingleton;
import me.beardedowl.caffeine.singleton.cache.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple JAX-RS resource to greet you. Examples:
 *
 * Get default greeting message:
 * curl -X GET http://localhost:8080/simple-greet
 *
 * The message is returned as a JSON object.
 */
@Path("/simple-greet")
@ApplicationScoped
public class SimpleGreetController {

    private static Logger LOGGER = LoggerFactory.getLogger(SimpleGreetController.class.getName());

    private LoadingCache<String,String> messageCache = Caffeine.newBuilder()
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
            .build(this::makeMessage);

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

    @GET
    @Path("/cache/singleton/message/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Map.Entry<String, List<UserDTO>>> getSingletonCacheMessageCacheData(){
        Instant start = Instant.now();
        String fName = "getSingletonCacheMessageCacheData";
        try{
            return CacheSingleton.getInstance().getMessageCache().asMap().entrySet();
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
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getMessageCacheStats(){
        Instant start = Instant.now();
        String fName = "getMessageCacheStats";
        try{
            return Json.createObjectBuilder()
                    .add("cacheHashcode", CacheSingleton.getInstance().getMessageCache().hashCode())
                    .add("evictionCount",CacheSingleton.getInstance().getMessageCache().stats().evictionCount())
                    .add("averageLoadPenalty",CacheSingleton.getInstance().getMessageCache().stats().averageLoadPenalty())
                    .add("hitCount",CacheSingleton.getInstance().getMessageCache().stats().hitCount())
                    .add("hitRate",CacheSingleton.getInstance().getMessageCache().stats().hitRate())
                    .build();
        } finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} operation completed in {} ms", fName,duration.toMillis());
        }
    }

    /**
     * An endpoint which returns message based on the provided name
     * @param name  : A string
     * @return      : Message
     */
    @Path("/{name}")
    @GET
    public String getMessage(@PathParam("name") String name) {
        Instant start = Instant.now();
        String fName = "getMessage" ;
        try{
            LOGGER.info("Greeting {}",name);
            return messageCache.get(name);
        } finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} operation completed in {} ms", fName, duration.toMillis());
        }
    }

    /**
     * Simulates the behavior of a function which takes "too" much time to calculate data
     * Cachine is supposed to reduce the calls to this function
     * @param message   : String
     * @return  Customised message
     * @throws  InterruptedException
     */
    private String makeMessage(String message) throws InterruptedException {
        LOGGER.warn("Could not find value for {} key in cache. Building....",message);
        LOGGER.info("Making current thread sleep for {} seconds",3);
        Thread.sleep(3000);
        return String.format("%s + %s",message,message.length());
    }

}
