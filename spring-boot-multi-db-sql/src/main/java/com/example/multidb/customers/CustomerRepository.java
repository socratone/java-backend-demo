package com.example.multidb.customers;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {
    private static final RowMapper<Customer> ROW_MAPPER = (rs, rowNum) ->
            new Customer(rs.getLong("id"), rs.getString("name"), rs.getString("email"));
    private final JdbcTemplate jdbc;

    public CustomerRepository(@Qualifier("customersJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Customer> findAll() {
        return jdbc.query("SELECT id, name, email FROM customers ORDER BY id", ROW_MAPPER);
    }

    public Customer create(CreateCustomerRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO customers (name, email) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.name());
            statement.setString(2, request.email());
            return statement;
        }, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey(), "Generated ID is missing").longValue();
        return jdbc.queryForObject("SELECT id, name, email FROM customers WHERE id = ?",
                ROW_MAPPER, id);
    }
}
