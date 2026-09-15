package com.example.multidb.products;

import com.example.multidb.api.TableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "products", description = "products_db의 상품 조회·생성")
public class ProductController {
    private static final List<String> COLUMNS = List.of("id", "name", "price", "stock");
    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "상품 목록 조회")
    public TableResponse<Product> list() {
        return new TableResponse<>(COLUMNS, repository.findAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "상품 생성")
    public TableResponse<Product> create(@Valid @RequestBody CreateProductRequest request) {
        return new TableResponse<>(COLUMNS, List.of(repository.create(request)));
    }
}
