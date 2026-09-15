package com.example.multidb.customers;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 100)
        @Schema(example = "홍길동") String name,
        @NotBlank @Email @Size(max = 254)
        @Schema(example = "hong@example.com") String email
) {
}
