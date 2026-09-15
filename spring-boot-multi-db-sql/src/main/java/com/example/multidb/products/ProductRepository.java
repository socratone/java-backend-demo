package com.example.multidb.products;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private static final RowMapper<Product> ROW_MAPPER = (rs, rowNum) ->
            new Product(rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("price"), rs.getInt("stock"));
    private final JdbcTemplate jdbc;

    public ProductRepository(@Qualifier("productsJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.query("SELECT id, name, price, stock FROM products ORDER BY id", ROW_MAPPER);
    }

    public Product create(CreateProductRequest request) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, request.name());
            statement.setBigDecimal(2, request.price());
            statement.setInt(3, request.stock());
            return statement;
        }, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey(), "Generated ID is missing").longValue();
        return jdbc.queryForObject("SELECT id, name, price, stock FROM products WHERE id = ?",
                ROW_MAPPER, id);
    }
}
