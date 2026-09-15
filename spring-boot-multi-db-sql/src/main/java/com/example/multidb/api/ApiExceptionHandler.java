package com.example.multidb.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 컨트롤러에서 발생하는 입력 오류를 공통 400 ProblemDetail 응답으로 변환합니다. */
@RestControllerAdvice
public class ApiExceptionHandler {
    /** @Valid의 필드 검증에 실패하면 필드명과 오류 메시지를 함께 반환합니다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validationError(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "입력값을 확인해 주세요.");
        List<FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        // 표준 오류 필드에 errors 배열을 추가하여 클라이언트가 잘못된 필드를 찾게 합니다.
        problem.setProperty("errors", errors);
        return problem;
    }

    /** JSON 문법 오류, 읽을 수 없는 본문, 숫자 변환 실패 등을 처리합니다. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadableRequest(HttpMessageNotReadableException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "JSON 형식과 필드 자료형을 확인해 주세요.");
    }

    /** 검증에 실패한 필드명과 그 이유를 담습니다. */
    public record FieldError(String field, String message) {
    }
}
