package com.example.greeting.domain;

import java.util.Objects;

public record Greeting(String message) {
    public Greeting {
        Objects.requireNonNull(message, "message must not be null");
    }
}
