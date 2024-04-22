package me.beardedowl.caffeine.singleton.cache;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class SanchayMTSafeService {
    private static Logger LOGGER = LoggerFactory.getLogger(SanchayController.class.getName());

    private Cache<String, List<UserDTO>> cache1 = CacheSingleton.getInstance().getMessageCache();
    private Cache<String,List<UserDTO>> cache2 = CacheSingleton.getInstance().getMessageCache();

    public SanchayMTSafeService() {
    }

    public List<UserDTO> getCachedDataFromLf1(String userName){
        // the get() method is a blocking function
        // In case multiple threads are trying to use this, it will only allow one thread to use it
        // Doesn't need to use synchronized keyword
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

    private List<UserDTO> loadingFunction1(String userName) throws InterruptedException {
        LOGGER.info("Loading function 1");
        LOGGER.warn("Value not found for key {} in cache....Loading",userName);
        LOGGER.warn("Sleeping for {} ms",3000);
        Thread.sleep(3000);
        return List.of(new UserDTO(userName, null));
    }

    public synchronized List<UserDTO> loadingFunction2(String userName, String surName) {
        String key = String.format("%s:%s",userName,surName);
        List<UserDTO> result = cache2.getIfPresent(key);
        if(result != null){
            return result;
        }
        LOGGER.info("Thread = {} Loading function 2",Thread.currentThread().getName());
        LOGGER.warn("Value not found for key {} in cache....Loading",key);
        LOGGER.warn("Sleeping for {} ms",3000);
        try{
            Thread.sleep(3000);
            result = List.of(new UserDTO(userName,surName));
            cache2.put(key,result);
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
