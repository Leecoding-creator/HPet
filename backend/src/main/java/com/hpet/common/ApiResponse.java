package com.hpet.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 모든 API 응답을 감싸는 공통 포맷.
 * { "success": true, "data": {...} } 또는 { "success": false, "error": {...} }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorPayload error;

    private ApiResponse(boolean success, T data, ErrorPayload error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorPayload(code, message));
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorPayload getError() {
        return error;
    }

    public static class ErrorPayload {
        private final String code;
        private final String message;

        public ErrorPayload(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
