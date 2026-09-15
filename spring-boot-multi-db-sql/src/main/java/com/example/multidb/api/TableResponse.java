package com.example.multidb.api;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "컬럼명과 행 객체 목록으로 구성된 테이블")
public record TableResponse<T>(List<String> columns, List<T> rows) {
}
