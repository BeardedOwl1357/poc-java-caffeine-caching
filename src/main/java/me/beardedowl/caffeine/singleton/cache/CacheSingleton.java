package me.beardedowl.caffeine.singleton.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a singleton thread-safe pattern.
 * To deal with multithreading issue, I am doing early initialization of the cacheSingleton object
 * Refer to https://www.baeldung.com/java-singleton-double-checked-locking#1-early-initialization
 */
@ApplicationScoped
public class CacheSingleton {

    private static Logger LOGGER = LoggerFactory.getLogger(CacheSingleton.class.getName());

    // The volatile keyword in Java is used to indicate that a variable's value can be modified by different threads.
    // It ensures that changes made to a volatile variable by one thread are immediately visible to other threads.
    private volatile Cache<String, List<UserDTO>> messageCache;

    private static CacheSingleton cacheSingleton = new CacheSingleton();

    private CacheSingleton(){
        this.messageCache = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .evictionListener((key,value,reason) -> {
                    LOGGER.warn("Expiring cache key '{}' with value '{}' --- '{}'",key,value,reason);
                })
                .removalListener((key,value,reason) -> {
                    LOGGER.warn("Removing cache key '{}' with value '{}' --- '{}'",key,value,reason) ;
                })
                .recordStats()
                .build();
    }

    public Cache<String,List<UserDTO>> getMessageCache(){
        return messageCache;
    }

    public static CacheSingleton getInstance(){
        return cacheSingleton;
    }
}
