
package me.beardedowl.caffeine;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Provider for greeting message.
 */
@ApplicationScoped
public class GreetingProvider {
    private final AtomicReference<String> message = new AtomicReference<>();
    private final AtomicReference<Boolean> raiseException = new AtomicReference<>();

    /**
     * Create a new greeting provider, reading the message from configuration.
     *
     * @param message greeting to use
     */
    @Inject
    public GreetingProvider(
            @ConfigProperty(name = "app.greeting") String message,
            @ConfigProperty(name = "app.raise.exception") Boolean raiseException)
    {
        this.message.set(message);
        this.raiseException.set(raiseException);
    }

    String getMessage() {
        return message.get();
    }

    void setMessage(String message) {
        this.message.set(message);
    }


    void setRaiseException(boolean raiseException){
        this.raiseException.set(raiseException);
    }
    public AtomicReference<Boolean> getRaiseException() {
        return raiseException;
    }
}
