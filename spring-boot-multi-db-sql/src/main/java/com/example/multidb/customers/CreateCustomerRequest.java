package com.example.multidb.customers;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** 고객 생성 시 받는 JSON 구조입니다. ID는 DB가 생성하므로 입력 필드에서 제외합니다. */
public record CreateCustomerRequest(
        // null·빈 문자열·공백만 있는 이름을 거부하고 최대 길이를 100자로 제한합니다.
        @NotBlank @Size(max = 100)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "홍길동") String name,
        // 필수 이메일의 형식과 DB 컬럼 최대 길이를 함께 검증합니다.
        @NotBlank @Email @Size(max = 254)
        // Swagger UI에 표시할 입력 예시입니다.
        @Schema(example = "hong@example.com") String email
) {
}
