package io.joshuasalcedo.script.controller;

import io.joshuasalcedo.script.dto.ScriptDto;
import io.joshuasalcedo.script.model.Script;
import io.joshuasalcedo.script.service.CrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/scripts")
@Tag(name = "Script Management", description = "APIs for managing scripts")
public class CrudController {

    private final CrudService crudService;

    @Autowired
    public CrudController(CrudService crudService) {
        this.crudService = crudService;
    }

    @Operation(summary = "Get all scripts", description = "Retrieves a list of all available scripts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all scripts",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ScriptDto.class)))
    @GetMapping
    public ResponseEntity<List<ScriptDto>> getAllScripts() {
        List<ScriptDto> scripts = crudService.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(scripts);
    }

    @Operation(summary = "Get script by ID", description = "Retrieves a specific script by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the script",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScriptDto.class))),
            @ApiResponse(responseCode = "404", description = "Script not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ScriptDto> getScriptById(
            @Parameter(description = "ID of the script to retrieve", required = true)
            @PathVariable Long id) {
        Optional<Script> scriptOptional = crudService.findById(id);
        return scriptOptional
                .map(script -> ResponseEntity.ok(convertToDto(script)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new script", description = "Creates a new script with the provided details")
    @ApiResponse(responseCode = "201", description = "Script successfully created",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ScriptDto.class)))
    @PostMapping
    public ResponseEntity<ScriptDto> createScript(
            @Parameter(description = "Script details", required = true)
            @RequestBody ScriptDto scriptDto) {
        Script script = convertToEntity(scriptDto);
        Script savedScript = crudService.save(script);
        return new ResponseEntity<>(convertToDto(savedScript), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing script", description = "Updates a script with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Script successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScriptDto.class))),
            @ApiResponse(responseCode = "404", description = "Script not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ScriptDto> updateScript(
            @Parameter(description = "ID of the script to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated script details", required = true)
            @RequestBody ScriptDto scriptDto) {
        if (crudService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Script script = convertToEntity(scriptDto);
        script.setId(id);
        Script updatedScript = crudService.save(script);
        return ResponseEntity.ok(convertToDto(updatedScript));
    }

    @Operation(summary = "Delete a script", description = "Deletes the script with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Script successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Script not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScript(
            @Parameter(description = "ID of the script to delete", required = true)
            @PathVariable Long id) {
        if (crudService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        crudService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ScriptDto convertToDto(Script script) {
        return ScriptDto.builder()
                .id(script.getId())
                .title(script.getTitle())
                .language(script.getLanguage())
                .help(script.getHelp())
                .content(script.getContent())
                .isEnabled(script.getIsEnabled())
                .build();
    }

    private Script convertToEntity(ScriptDto scriptDto) {
        return Script.builder()
                .id(scriptDto.getId())
                .title(scriptDto.getTitle())
                .language(scriptDto.getLanguage())
                .help(scriptDto.getHelp())
                .content(scriptDto.getContent())
                .isEnabled(scriptDto.getIsEnabled())
                .build();
    }
}