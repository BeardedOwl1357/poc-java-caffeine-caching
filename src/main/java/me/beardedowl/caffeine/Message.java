package me.beardedowl.caffeine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Message {

    private String message;

    private String greeting;

    public Message() {
    }

    public Message(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getGreeting() {
        return this.greeting;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", greeting='" + greeting + '\'' +
                '}';
    }
}
