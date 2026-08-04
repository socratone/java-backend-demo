package com.example.greeting.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GreetingTest {
    @Test
    void storesMessage() {
        assertEquals("Hello!", new Greeting("Hello!").message());
    }

    @Test
    void rejectsNullMessage() {
        assertThrows(NullPointerException.class, () -> new Greeting(null));
    }
}
