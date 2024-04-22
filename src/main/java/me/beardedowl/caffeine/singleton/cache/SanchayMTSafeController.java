/**
 * This is an implementation of SanchayController class which is multithreading safe
 * In SanchayController class, since multiple threads were
 */
package me.beardedowl.caffeine.singleton.cache;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/sanchay/mt/safe/")
@ApplicationScoped

public class SanchayMTSafeController {

    private static Logger LOGGER = LoggerFactory.getLogger(SanchayController.class.getName());

    @Inject
    private SanchayMTSafeService service;

    @GET
    public Response greet(){
        return Response.status(Response.Status.OK).entity("Hello Sanchay").build();
    }

    @GET
    @Path("/user/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDTO> getUserDto(@PathParam("name") String userName){
        Instant start = Instant.now();
        String fName = "getUserDto";
        try{
            return service.getCachedDataFromLf1(userName);
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }
    }

    @GET
    @Path("/user/{name}/surname/{surName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDTO> getUserDtoWithSurname(@PathParam("name") String userName,
                                               @PathParam("surName") String surName){
        Instant start = Instant.now();
        String fName = "getUserDto";
        try{
            return service.loadingFunction2(userName, surName);
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }
    }

    /**
     * Converts the cache into a string and returns it to user
     * Note that this does not refresh the cache BUT it removes the entries which have been marked as expired
     * Also, I believe that both the key and value objects should have toString() overridden
     * @return  Cache data in a key-value pair
     */
    @GET
    @Path("/cache/message/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Map.Entry<String, List<UserDTO>>> getMessageCacheData(){
        Instant start = Instant.now();
        String fName = "getMessageCacheData";
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
                    .add("cacheHashcode",CacheSingleton.getInstance().getMessageCache().hashCode())
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

}
