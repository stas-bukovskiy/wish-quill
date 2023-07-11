package com.wishquill.userservice.controllers;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected @NotNull Mono<ResponseEntity<Object>> handleResponseStatusException(@NotNull ResponseStatusException ex,
                                                                                  @NotNull HttpHeaders headers,
                                                                                  @NotNull HttpStatusCode status,
                                                                                  @NotNull ServerWebExchange exchange) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, (ex.getReason() != null ? ex.getReason() : ""));
        return handleExceptionInternal(ex, problemDetail, ex.getHeaders(), ex.getStatusCode(), exchange);
    }

    @Override
    protected @NotNull Mono<ResponseEntity<Object>> handleWebExchangeBindException(@NotNull WebExchangeBindException ex,
                                                                                   @NotNull HttpHeaders headers,
                                                                                   @NotNull HttpStatusCode status,
                                                                                   @NotNull ServerWebExchange exchange) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, (ex.getReason() != null ? ex.getReason() : ""));
        problemDetail.setProperty("errors", generateErrorMap(ex));
        return handleExceptionInternal(ex, problemDetail, ex.getHeaders(), ex.getStatusCode(), exchange);
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<Object>> handleAuthenticationException(AuthenticationException e,
                                                                      ServerWebExchange exchange) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        return handleExceptionInternal(e, problemDetail, HttpHeaders.EMPTY, HttpStatus.FORBIDDEN, exchange);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<Object>> handleAuthenticationException(BadCredentialsException e,
                                                                      ServerWebExchange exchange) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        return handleExceptionInternal(e, problemDetail, HttpHeaders.EMPTY, HttpStatus.UNAUTHORIZED, exchange);
    }

    private Map<String, List<String>> generateErrorMap(final WebExchangeBindException exception) {
        Map<String, List<String>> errorMap = new HashMap<>();
        for (FieldError fieldError : exception.getFieldErrors()) {
            if (fieldError.getDefaultMessage() == null) continue;
            errorMap.computeIfAbsent(fieldError.getField(), k -> new ArrayList<>());
            errorMap.get(fieldError.getField()).add(fieldError.getDefaultMessage());
        }
        return errorMap;
    }

}
