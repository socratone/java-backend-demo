package com.example.greeting.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.greeting")
public class GreetingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreetingApiApplication.class, args);
    }
}
