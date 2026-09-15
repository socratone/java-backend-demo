package com.example.multidb.customers;

import com.example.multidb.api.TableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "customers", description = "customers_db의 고객 조회·생성")
public class CustomerController {
    private static final List<String> COLUMNS = List.of("id", "name", "email");
    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "고객 목록 조회")
    public TableResponse<Customer> list() {
        return new TableResponse<>(COLUMNS, repository.findAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "고객 생성")
    public TableResponse<Customer> create(@Valid @RequestBody CreateCustomerRequest request) {
        return new TableResponse<>(COLUMNS, List.of(repository.create(request)));
    }
}
