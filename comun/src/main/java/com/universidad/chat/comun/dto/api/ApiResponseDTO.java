package com.universidad.chat.comun.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO genérico para respuestas de la API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> implements Serializable {
    private boolean success;
    private String message;
    private T data;
    private long timestamp;
    
    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>(true, "Success", data, System.currentTimeMillis());
    }
    
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(true, message, data, System.currentTimeMillis());
    }
    
    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>(false, message, null, System.currentTimeMillis());
    }
}
