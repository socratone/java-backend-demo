package com.example.greeting.batch;

import com.example.greeting.service.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingBatchRunnerTest {
    private final GreetingBatchRunner runner = new GreetingBatchRunner(new GreetingService());

    @Test
    void printsGreetingForProvidedName() {
        assertEquals("Hello, Codex!", runAndCapture("--name=Codex"));
    }

    @Test
    void usesWorldWhenNameIsMissing() {
        assertEquals("Hello, World!", runAndCapture());
    }

    private String runAndCapture(String... args) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            runner.run(new DefaultApplicationArguments(args));
            return output.toString(StandardCharsets.UTF_8).trim();
        } finally {
            System.setOut(originalOut);
        }
    }
}
