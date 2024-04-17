# caffeine-poc

A POC for figuring out how caffeine cache works. This is built over [Helidon MP Quickstart](https://helidon.io/docs/v3/mp/guides/quickstart) project with me using [GitHub - ben-manes/caffeine: A high performance caching library for Java](https://github.com/ben-manes/caffeine) library and some loggers (SLF4J and Log4j2).


## Refresh and Expire behavior

For a cache defined as follows
```java
private LoadingCache<String,String> messageCache = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .refreshAfterWrite(1,TimeUnit.MINUTES)
        .evictionListener((key,value,reason) -> {
            LOGGER.warn("Expiring cache key '{}' with value '{}' --- '{}'",key,value,reason);
        })
        .removalListener((key,value,reason) -> {
            LOGGER.warn("Removing cache key '{}' with value '{}' --- '{}'",key,value,reason) ;
        })
        .recordStats()
        .build(this::makeMessage);

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
            
```

- If the value does not exist in cache, cache uses the loading function (in this case, `makeMessage()`) to build a new value and return it
- Access to the same value before the refresh interval is instantly returned. The eligibility for "being refreshed" is checked each time the value is accessed. 
- If we have accessed the value after it has been marked for refresh, cache returns the value and asynchronously loads a new value
- If we have accessed the value after it has been marked for expiry, it is evicted from the cache and a new value is loaded. **Also, when this value is evicted, cache performs a maintenance task in a separate thread which evicts all the values which have been marked for expiry**

> Eviction         : eviction means removal due to the policy (like, expireAfterAccess or expireAfterWrite)

> Invalidation     : invalidation means manual removal by the caller

> Removal          : removal occurs as a consequence of invalidation or eviction _(If a value is refreshed, it is marked as `REPLACED` and is considered a removal)_

# Info provided by helidon and me

## Build and run

With JDK17+
```bash
mvn package
java -jar target/caffeine-poc.jar

# To run in debug mode and skip tests
# Use Remote JVM Debug on port 7044
mvn package -DskipTests ;
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7044  -jar target/caffeine-poc.jar ;

# Do it in one line 
mvn package -DskipTests && java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7044  -jar target/caffeine-poc.jar


```

## Usage

```bash
curl -X GET http://localhost:8081/greet
{"message":"Hello World!"}

curl -X GET http://localhost:8081/greet/Joe
{"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8081/greet/greeting

curl -X GET http://localhost:8081/greet/Jose
{"message":"Hola Jose!"}
```