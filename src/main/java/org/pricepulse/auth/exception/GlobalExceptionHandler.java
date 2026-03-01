package org.pricepulse.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.pricepulse.auth.dto.response.ExceptionResponseDTO;
import org.pricepulse.auth.exception.generic.DuplicateResourceException;
import org.pricepulse.auth.exception.generic.InvalidInputException;
import org.pricepulse.auth.exception.generic.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {

    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

    return buildResponse(
        new RuntimeException("Internal server error"),
        HttpStatus.INTERNAL_SERVER_ERROR,
        request
    );
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ExceptionResponseDTO> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
    return buildResponse(ex, HttpStatus.CONFLICT, request);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ExceptionResponseDTO> handleNotFound(NotFoundException ex, HttpServletRequest request) {
    return buildResponse(ex, HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<ExceptionResponseDTO> handleInvalidInput(InvalidInputException ex, HttpServletRequest request) {
    return buildResponse(ex, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fieldError -> {
              String defaultMessage = fieldError.getDefaultMessage();
              return defaultMessage != null ? defaultMessage : "Invalid value";
            },
            (existing, replacement) -> existing));

    ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(
        ex.getMessage(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        request.getRequestURI(),
        Instant.now(),
        validationErrors);

    return ResponseEntity.badRequest().body(exceptionResponseDTO);
  }

  private ResponseEntity<ExceptionResponseDTO> buildResponse(Exception ex, HttpStatus httpStatus, HttpServletRequest request) {
    ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(
        ex.getMessage(),
        httpStatus.value(),
        httpStatus.getReasonPhrase(),
        request.getRequestURI(),
        Instant.now(), null);

    return ResponseEntity.status(httpStatus).body(exceptionResponseDTO);
  }

}
