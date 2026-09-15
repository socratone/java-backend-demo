package com.example.multidb.orders;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** 주문 생성 시 받는 JSON 구조입니다. ID는 DB가 생성하므로 입력 필드에서 제외합니다. */
public record CreateOrderRequest(
        // null·빈 문자열·공백만 있는 이름을 거부하고 최대 길이를 100자로 제한합니다.
        @NotBlank @Size(max = 100)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "키보드") String itemName,
        // 수량 누락을 거부하고 최소 주문 수량을 1로 제한합니다.
        @NotNull @Min(1)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "2") Integer quantity
) {
}
