package com.example.multidb.products;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** 상품 생성 시 받는 JSON 구조입니다. ID는 DB가 생성하므로 입력 필드에서 제외합니다. */
public record CreateProductRequest(
        // null·빈 문자열·공백만 있는 이름을 거부하고 최대 길이를 100자로 제한합니다.
        @NotBlank @Size(max = 100)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "키보드") String name,
        // 필수 금액이며 0 이상, 정수부 10자리·소수부 2자리까지 허용합니다.
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "59000") BigDecimal price,
        // Integer를 사용해 누락을 null로 구분하며, 재고는 0 이상이어야 합니다.
        @NotNull @Min(0)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "10") Integer stock
) {
}
