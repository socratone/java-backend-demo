package com.example.multidb.products;

import java.math.BigDecimal;

public record Product(long id, String name, BigDecimal price, Integer stock) {
}
