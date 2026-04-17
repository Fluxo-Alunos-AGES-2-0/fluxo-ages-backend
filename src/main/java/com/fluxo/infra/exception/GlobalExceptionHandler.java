package com.fluxo.infra.exception;

import com.fluxo.attendance.exception.ActiveAttendanceNotFoundException;
import com.fluxo.attendance.exception.AttendanceAlreadyOpenException;
import com.fluxo.auth.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AttendanceAlreadyOpenException.class)
    public ResponseEntity<Map<String, String>> handleAttendanceAlreadyOpen(AttendanceAlreadyOpenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ActiveAttendanceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleActiveAttendanceNotFound(ActiveAttendanceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Dados invalidos");

        return ResponseEntity.badRequest()
                .body(Map.of("error", errorMessage));
    }
}
