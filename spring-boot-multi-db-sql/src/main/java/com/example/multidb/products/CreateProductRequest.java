package com.example.multidb.products;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateProductRequest(
        @NotBlank @Size(max = 100)
        @Schema(example = "키보드") String name,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2)
        @Schema(example = "59000") BigDecimal price,
        @NotNull @Min(0)
        @Schema(example = "10") Integer stock
) {
}
