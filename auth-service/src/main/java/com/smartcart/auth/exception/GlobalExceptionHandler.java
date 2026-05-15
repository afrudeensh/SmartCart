package com.smartcart.auth.exception;

import com.smartcart.auth.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Validation errors (@Valid → @Size, @NotBlank, @Email) ─────────────────
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return BaseResponse.error(400, "Validation failed", errors);
    }

    // ── Business logic errors ─────────────────────────────────────────────────
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleRuntime(RuntimeException ex) {
        return BaseResponse.error(400, ex.getMessage());
    }

    // ── Wrong credentials ─────────────────────────────────────────────────────
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public BaseResponse<?> handleBadCredentials(BadCredentialsException ex) {
        return BaseResponse.error(401, "Invalid email or password");
    }

    // ── Wrong role ────────────────────────────────────────────────────────────
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<?> handleAccessDenied(AccessDeniedException ex) {
        return BaseResponse.error(403, "Access denied: insufficient permissions");
    }
}