package com.example.multidb.customers;


/**
 * 고객 조회·생성 응답의 행 한 개를 표현하는 불변 데이터 객체입니다.
 * record가 생성자, 접근자, equals/hashCode 등을 자동으로 제공합니다.
 *
 * @param id DB가 자동 생성한 고객 ID
 * @param name 고객명
 * @param email 고객 이메일 주소
 */
public record Customer(long id, String name, String email) {
}
