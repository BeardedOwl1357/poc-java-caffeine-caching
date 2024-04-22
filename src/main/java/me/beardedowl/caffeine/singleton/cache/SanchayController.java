package me.beardedowl.caffeine.singleton.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import jakarta.enterprise.context.ApplicationScoped;
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

@Path("/sanchay")
@ApplicationScoped
public class SanchayController {

    private static Logger LOGGER = LoggerFactory.getLogger(SanchayController.class.getName());

    private Cache<String,List<UserDTO>> cache1 = CacheSingleton.getInstance().getMessageCache();
    private Cache<String,List<UserDTO>> cache2 = CacheSingleton.getInstance().getMessageCache();

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
            LOGGER.info("Cache 1 hashcode : {} ",cache1.hashCode());
            return cache1.get(userName, (userNm) -> {
                try{
                    return loadingFunction1(userNm);
                }
                catch(InterruptedException i){
                    i.printStackTrace();
                }
                catch (Exception e){
                    throw e;
                }
                return null;
            });
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
//            LOGGER.info("Cache 2 hashcode : {} ",cache2.hashCode());
            String key = String.format("%s:%s",userName,surName);
            List<UserDTO> result;
            result = cache2.getIfPresent(key);
            if(null == result ){
                LOGGER.warn("Value with {} Not found in cache...",key);
                result = loadingFunction2(key);
                cache2.put(key,result);
            }
            return result;
        }
        finally {
            Duration duration = Duration.between(start,Instant.now());
            LOGGER.info("{} completed in {} ms",fName,duration.toMillis());
        }
    }


    private List<UserDTO> loadingFunction1(String userName) throws InterruptedException {
        LOGGER.info("Loading function 1");
        LOGGER.warn("Value not found for key {} in cache....Loading",userName);
        LOGGER.warn("Sleeping for {} ms",3000);
        Thread.sleep(3000);
        return List.of(new UserDTO(userName, null));
    }

    private List<UserDTO> loadingFunction2(String userNameAndSurname) {
        LOGGER.info("Thread = {} Loading function 2",Thread.currentThread().getName());
        String[] arr = userNameAndSurname.split(":");
        LOGGER.warn("Value not found for key {} in cache....Loading",userNameAndSurname);
        LOGGER.warn("Sleeping for {} ms",3000);
        try{
            Thread.sleep(3000);
            return List.of(new UserDTO(arr[0],arr[1]));
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
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
