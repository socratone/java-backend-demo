package com.example.greeting.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
@AutoConfigureMockMvc
@Import(com.example.greeting.service.GreetingService.class)
class GreetingApiApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void greetsProvidedName() throws Exception {
        mockMvc.perform(get("/api/greetings").param("name", "Codex"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.message").value("Hello, Codex!"));
    }

    @Test
    void usesWorldWhenNameIsMissing() throws Exception {
        mockMvc.perform(get("/api/greetings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"));
    }
}
