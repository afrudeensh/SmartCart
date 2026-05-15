package com.smartcart.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ── Success ───────────────────────────────────────────────────────────────

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success(T data) {
        return success(data, "Success");
    }

    // ── Error — without data (used in SecurityConfig + most exception handlers)
    public static <T> BaseResponse<T> error(int status, String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .build();
    }

    // ── Error — with data (used in validation handler to return field errors) ─
    public static <T> BaseResponse<T> error(int status, String message, T data) {
        return BaseResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}