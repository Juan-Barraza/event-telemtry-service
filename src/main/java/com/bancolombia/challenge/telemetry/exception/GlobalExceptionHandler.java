package com.bancolombia.challenge.telemetry.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ProblemDetail> handleNotFound(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return Mono.just(pd);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ProblemDetail> handleValidationErrors(WebExchangeBindException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Error to validated request");
        pd.setTitle("Invalid Request");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());
        return Mono.just(pd);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ProblemDetail> handleGenericError(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        pd.setTitle("Internal server error");
        pd.setProperty("timestamp", Instant.now());
        return Mono.just(pd);
    }
}