package com.example.multidb.api;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모든 목록·생성 API가 공유하는 테이블 응답 형식입니다.
 *
 * @param columns 행 객체의 필드명 목록으로, rows가 비어 있어도 유지합니다.
 * @param rows GET은 전체 행, POST는 생성한 행 한 개를 담습니다.
 * @param <T> 상품·고객·주문 등 각 행을 표현하는 객체 타입입니다.
 */
@Schema(description = "컬럼명과 행 객체 목록으로 구성된 테이블")
public record TableResponse<T>(List<String> columns, List<T> rows) {
}
