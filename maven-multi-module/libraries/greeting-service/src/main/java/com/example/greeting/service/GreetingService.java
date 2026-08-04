package com.example.greeting.service;

import com.example.greeting.domain.Greeting;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public Greeting greet(String name) {
        String recipient = name == null || name.isBlank() ? "World" : name.trim();
        return new Greeting("Hello, " + recipient + "!");
    }
}
