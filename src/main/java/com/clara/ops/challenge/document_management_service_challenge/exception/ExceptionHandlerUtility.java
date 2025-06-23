package com.clara.ops.challenge.document_management_service_challenge.exception;

import com.clara.ops.challenge.document_management_service_challenge.dtos.ApiResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ExceptionHandlerUtility {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponseWrapper<?>> handleAllExceptions(
      Exception ex, HttpServletRequest request) {
    ApiResponseWrapper<?> responseWrapper =
        new ApiResponseWrapper<>(null, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<?> handleMissingPart(MissingServletRequestPartException ex) {
    ApiResponseWrapper<?> response =
        new ApiResponseWrapper<>(
            null,
            "Required request parameter  " + ex.getRequestPartName() + " is not present",
            400);
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponseWrapper<?>> handleMissingParams(
      MissingServletRequestParameterException ex) {
    String name = ex.getParameterName();
    return ResponseEntity.badRequest()
        .body(
            new ApiResponseWrapper<>(
                null, "Required request parameter  " + name + " is not present", 400));
  }
}
