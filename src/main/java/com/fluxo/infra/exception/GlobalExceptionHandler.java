package com.fluxo.infra.exception;

import com.fluxo.auth.exception.InvalidCredentialsException;
import com.fluxo.hours.exception.ActiveHoursNotFoundException;
import com.fluxo.hours.exception.HoursAlreadyOpenException;
import com.fluxo.report.exception.InvalidReportFileException;
import com.fluxo.report.exception.ReportNotFoundException;
import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.report.exception.ReportStorageException;
import com.fluxo.report.exception.ReportTemplateNotFoundException;
import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.report.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HoursAlreadyOpenException.class)
    public ResponseEntity<Map<String, String>> handleHoursAlreadyOpen(HoursAlreadyOpenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ActiveHoursNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleActiveHoursNotFound(ActiveHoursNotFoundException ex) {
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

    @ExceptionHandler(InvalidReportFileException.class)
    public ResponseEntity<Map<String, String>> handleInvalidReportFile(InvalidReportFileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ReportTemplateNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReportTemplateNotFound(ReportTemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(StudentProjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleStudentProjectNotFound(StudentProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ReportStorageException.class)
    public ResponseEntity<Map<String, String>> handleReportStorage(ReportStorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro ao processar o upload do relatório. Por favor, tente novamente."));
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReportNotFound(ReportNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ReportAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleReportAccessDenied(ReportAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Acesso negado. Você não tem permissão para acessar este relatório"));
    }
}
