package com.adricarrera.orderservice.common;

import com.adricarrera.orderservice.order.domain.OrderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request){
    return buildResponse(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage(), request);
  }

  private ResponseEntity<ErrorResponse> buildResponse(
HttpStatus status, String errorCode, String message, HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(
        LocalDateTime.now(),
        status.value(),
        errorCode,
        message,
        request.getRequestURI()
    );
    return new ResponseEntity<>(body, status);
  }


}
