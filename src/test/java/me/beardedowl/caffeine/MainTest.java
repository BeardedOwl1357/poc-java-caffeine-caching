
package me.beardedowl.caffeine;

import me.beardedowl.caffeine.exception.handling.Message;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import io.helidon.microprofile.tests.junit5.HelidonTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
class MainTest {

    @Inject
    private MetricRegistry registry;

    @Inject
    private WebTarget target;


    @Test
    void testMicroprofileMetrics() {
        String message = target.path("simple-greet/Joe")
                .request()
                .get(String.class);

        assertThat(message, is("Hello Joe"));
        Counter counter = registry.counter("personalizedGets");
        double before = counter.getCount();

        message = target.path("simple-greet/Eric")
                .request()
                .get(String.class);

        assertThat(message, is("Hello Eric"));
        double after = counter.getCount();
        assertEquals(1d, after - before, "Difference in personalized greeting counter between successive calls");
    }

    @Test
    void testMetrics() {
        Response response = target
                .path("metrics")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    void testHealth() {
        Response response = target
                .path("health")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
    }

    @Test
    void testGreet() {
        Message message = target
                .path("simple-greet")
                .request()
                .get(Message.class);
        assertThat(message.getMessage(), is("Hello World!"));
    }
                
    @Test
    void testGreetings() {
        Message jsonMessage = target
                .path("greet/Joe")
                .request()
                .get(Message.class);
        assertThat(jsonMessage.getMessage(), is("Hello Joe!"));

        try (Response r = target
                .path("greet/greeting")
                .request()
                .put(Entity.entity("{\"greeting\" : \"Hola\"}", MediaType.APPLICATION_JSON))) {
            assertThat(r.getStatus(), is(204));
        }

        jsonMessage = target
                .path("greet/Jose")
                .request()
                .get(Message.class);
        assertThat(jsonMessage.getMessage(), is("Hola Jose!"));
    }
                
}
