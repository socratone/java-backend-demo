package com.example.multidb.orders;


/**
 * 주문 조회·생성 응답의 행 한 개를 표현하는 불변 데이터 객체입니다.
 * record가 생성자, 접근자, equals/hashCode 등을 자동으로 제공합니다.
 *
 * @param id DB가 자동 생성한 주문 ID
 * @param itemName 주문한 품목명으로, 상품 DB 참조 없이 저장하는 문자열
 * @param quantity 주문 수량
 */
public record Order(long id, String itemName, Integer quantity) {
}
