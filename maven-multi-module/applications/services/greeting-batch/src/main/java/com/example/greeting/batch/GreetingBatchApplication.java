package com.example.greeting.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.greeting")
public class GreetingBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreetingBatchApplication.class, args);
    }
}
