package io.joshuasalcedo.script.dto;

import io.joshuasalcedo.script.model.ScriptLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Script entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for Script entity")
public class ScriptDto {

    @Schema(description = "Unique identifier of the script", example = "1")
    private Long id;

    @Schema(description = "Title of the script", example = "Data Processing Script", required = true)
    private String title;

    @Schema(description = "Programming language of the script", example = "PYTHON", required = true)
    private ScriptLanguage language;

    @Schema(description = "Help text and usage instructions for the script", example = "This script processes CSV data files")
    private String help;

    @Schema(description = "Actual content/code of the script", example = "print('Hello, World!')")
    private String content;

    @Schema(description = "Flag indicating whether the script is enabled for execution", example = "true", defaultValue = "false")
    private Boolean isEnabled;
}