package io.joshuasalcedo.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response for validation errors
 */
@Getter
@Setter
@Schema(description = "Validation error response")
public class ValidationErrorResponse {
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Error title", example = "Validation Failed")
    private String title;
    
    @Schema(description = "Brief error message", example = "One or more fields failed validation")
    private String message;
    
    @Schema(description = "Field validation errors with field name as key and error message as value")
    private Map<String, String> errors;
    
    @Schema(description = "Timestamp when the error occurred", example = "2025-05-03T14:20:30")
    private LocalDateTime timestamp;
    
    public ValidationErrorResponse(int status, String title, String message, 
                                Map<String, String> errors, LocalDateTime timestamp) {
        this.status = status;
        this.title = title;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }
}