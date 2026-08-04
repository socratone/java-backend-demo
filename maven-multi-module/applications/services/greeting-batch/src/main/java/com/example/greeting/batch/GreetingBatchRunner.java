package com.example.greeting.batch;

import com.example.greeting.service.GreetingService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GreetingBatchRunner implements ApplicationRunner {
    private final GreetingService greetingService;

    public GreetingBatchRunner(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> names = args.getOptionValues("name");
        String name = names == null || names.isEmpty() ? null : names.getFirst();
        System.out.println(greetingService.greet(name).message());
    }
}
