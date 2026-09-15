package com.example.multidb.customers;

import com.example.multidb.api.TableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** /api/customers 요청을 받아 고객 저장소를 호출하고 결과를 JSON으로 반환합니다. */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "customers", description = "customers_db의 고객 조회·생성")
public class CustomerController {
    // 조회 결과가 없어도 컬럼명과 표시 순서를 응답에 유지합니다.
    private static final List<String> COLUMNS = List.of("id", "name", "email");
    private final CustomerRepository repository;

    /** Spring이 등록한 고객 저장소를 생성자로 주입받습니다. */
    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    /** 전체 행을 테이블 형태로 반환합니다. 정상 응답의 기본 상태 코드는 200입니다. */
    @GetMapping
    @Operation(summary = "고객 목록 조회")
    public TableResponse<Customer> list() {
        return new TableResponse<>(COLUMNS, repository.findAll());
    }

    /** JSON을 요청 객체로 변환하고 @Valid로 검증한 뒤, 생성한 행 한 개를 201로 반환합니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "고객 생성")
    public TableResponse<Customer> create(@Valid @RequestBody CreateCustomerRequest request) {
        return new TableResponse<>(COLUMNS, List.of(repository.create(request)));
    }
}
