package com.example.multidb.products;

import java.math.BigDecimal;

/**
 * 상품 조회·생성 응답의 행 한 개를 표현하는 불변 데이터 객체입니다.
 * record가 생성자, 접근자, equals/hashCode 등을 자동으로 제공합니다.
 *
 * @param id DB가 자동 생성한 상품 ID
 * @param name 상품명
 * @param price 소수 금액을 정확하게 표현하는 BigDecimal 가격
 * @param stock 현재 재고 수량
 */
public record Product(long id, String name, BigDecimal price, Integer stock) {
}
