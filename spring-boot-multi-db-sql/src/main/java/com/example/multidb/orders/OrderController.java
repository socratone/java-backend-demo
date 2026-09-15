package com.example.multidb.orders;

import com.example.multidb.api.TableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "orders", description = "orders_db의 주문 조회·생성")
public class OrderController {
    private static final List<String> COLUMNS = List.of("id", "itemName", "quantity");
    private final OrderRepository repository;

    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "주문 목록 조회")
    public TableResponse<Order> list() {
        return new TableResponse<>(COLUMNS, repository.findAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "주문 생성")
    public TableResponse<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        return new TableResponse<>(COLUMNS, List.of(repository.create(request)));
    }
}
