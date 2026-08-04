package com.example.greeting.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingServiceTest {
    private final GreetingService greetingService = new GreetingService();

    @Test
    void greetsProvidedName() {
        assertEquals("Hello, Codex!", greetingService.greet("Codex").message());
    }

    @Test
    void usesWorldForMissingOrBlankName() {
        assertEquals("Hello, World!", greetingService.greet(null).message());
        assertEquals("Hello, World!", greetingService.greet("  ").message());
    }
}
