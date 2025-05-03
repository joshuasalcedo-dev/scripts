package io.joshuasalcedo.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response format
 */
@Getter
@Setter
@Schema(description = "Standard error response")
public class ErrorResponse {
    
    @Schema(description = "HTTP status code", example = "500")
    private int status;
    
    @Schema(description = "Error title", example = "Internal Server Error")
    private String title;
    
    @Schema(description = "Detailed error message", example = "An unexpected error occurred while processing your request")
    private String message;
    
    @Schema(description = "Request path", example = "/api/v1/scripts/1")
    private String path;
    
    @Schema(description = "Timestamp when the error occurred", example = "2025-05-03T14:20:30")
    private LocalDateTime timestamp;
    
    @Schema(description = "Additional information about the error")
    private Map<String, String> additionalInfo;
    
    public ErrorResponse(int status, String title, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.title = title;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
        this.additionalInfo = new HashMap<>();
    }
    
    public void addAdditionalInfo(String key, String value) {
        if (additionalInfo == null) {
            additionalInfo = new HashMap<>();
        }
        additionalInfo.put(key, value);
    }
}