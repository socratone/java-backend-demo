package com.example.multidb.orders;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) ->
            new Order(rs.getLong("id"), rs.getString("item_name"), rs.getInt("quantity"));
    private final JdbcTemplate jdbc;

    public OrderRepository(@Qualifier("ordersJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Order> findAll() {
        return jdbc.query("SELECT id, item_name, quantity FROM orders ORDER BY id", ROW_MAPPER);
    }

    public Order create(CreateOrderRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO orders (item_name, quantity) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.itemName());
            statement.setInt(2, request.quantity());
            return statement;
        }, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey(), "Generated ID is missing").longValue();
        return jdbc.queryForObject("SELECT id, item_name, quantity FROM orders WHERE id = ?",
                ROW_MAPPER, id);
    }
}
