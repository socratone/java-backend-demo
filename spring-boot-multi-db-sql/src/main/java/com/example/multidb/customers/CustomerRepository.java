package com.example.multidb.customers;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/** customers_db의 고객 테이블에 직접 SQL을 실행하는 저장소입니다. */
@Repository
public class CustomerRepository {
    // SELECT 결과의 각 행을 Customer 응답 객체로 변환합니다.
    private static final RowMapper<Customer> ROW_MAPPER = (rs, rowNum) ->
            new Customer(rs.getLong("id"), rs.getString("name"), rs.getString("email"));
    private final JdbcTemplate jdbc;

    /** 같은 타입의 빈이 3개이므로 @Qualifier로 customers 전용 연결을 선택합니다. */
    public CustomerRepository(@Qualifier("customersJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** ID 오름차순으로 조회하며, 데이터가 없으면 빈 목록을 반환합니다. */
    public List<Customer> findAll() {
        return jdbc.query("SELECT id, name, email FROM customers ORDER BY id", ROW_MAPPER);
    }

    /** 고객을(를) 저장하고 DB가 생성한 ID로 다시 조회하여 실제 저장된 행을 반환합니다. */
    public Customer create(CreateCustomerRequest request) {
        // INSERT 후 DB가 자동 생성한 기본 키를 받을 객체입니다.
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        // JdbcTemplate이 연결 대여·반납과 Statement 정리를 담당합니다.
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO customers (name, email) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            // 사용자 입력을 SQL 문자열에 합치지 않고 ? 위치에 값으로 바인딩합니다.
            statement.setString(1, request.name());
            statement.setString(2, request.email());
            return statement;
        }, keyHolder);
        // 다른 요청의 ID와 혼동하지 않도록 이번 INSERT가 반환한 키를 사용합니다.
        long id = Objects.requireNonNull(keyHolder.getKey(), "Generated ID is missing").longValue();
        return jdbc.queryForObject("SELECT id, name, email FROM customers WHERE id = ?",
                ROW_MAPPER, id);
    }
}
