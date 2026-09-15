package com.example.multidb.orders;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateOrderRequest(
        @NotBlank @Size(max = 100)
        @Schema(example = "키보드") String itemName,
        @NotNull @Min(1)
        @Schema(example = "2") Integer quantity
) {
}
