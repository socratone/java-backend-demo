package com.example.multidb.orders;

import com.example.multidb.api.TableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** /api/orders 요청을 받아 주문 저장소를 호출하고 결과를 JSON으로 반환합니다. */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "orders", description = "orders_db의 주문 조회·생성")
public class OrderController {
    // 조회 결과가 없어도 컬럼명과 표시 순서를 응답에 유지합니다.
    private static final List<String> COLUMNS = List.of("id", "itemName", "quantity");
    private final OrderRepository repository;

    /** Spring이 등록한 주문 저장소를 생성자로 주입받습니다. */
    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }

    /** 전체 행을 테이블 형태로 반환합니다. 정상 응답의 기본 상태 코드는 200입니다. */
    @GetMapping
    @Operation(summary = "주문 목록 조회")
    public TableResponse<Order> list() {
        return new TableResponse<>(COLUMNS, repository.findAll());
    }

    /** JSON을 요청 객체로 변환하고 @Valid로 검증한 뒤, 생성한 행 한 개를 201로 반환합니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "주문 생성")
    public TableResponse<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        return new TableResponse<>(COLUMNS, List.of(repository.create(request)));
    }
}
