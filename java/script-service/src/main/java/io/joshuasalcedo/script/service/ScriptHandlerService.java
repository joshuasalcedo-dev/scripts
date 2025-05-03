package io.joshuasalcedo.script.service;

import io.joshuasalcedo.exception.ScriptHandlerRunException;
import io.joshuasalcedo.script.model.Script;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ScriptHandlerService {

    private final CrudService crudService;

    @Value("${script.file.storage.location:${user.home}/script-files}")
    private String fileStorageLocation;

    public ScriptHandlerService(CrudService crudService) {
        this.crudService = crudService;
    }

    @PostConstruct
    public void init() {
        // Create directory if it doesn't exist
        try {
            if (fileStorageLocation == null) {
                fileStorageLocation = System.getProperty("user.home") + "/script-files";
            }
            Files.createDirectories(Paths.get(fileStorageLocation));
        } catch (IOException e) {
            throw new RuntimeException("Could not create script file storage directory", e);
        }
    }

    /**
     * Run a script using the appropriate script engine
     *
     * @param script The script to run
     * @return true if the script execution was successful
     * @throws ScriptHandlerRunException if there is an error executing the script
     */
    public boolean runScript(Script script) throws ScriptHandlerRunException {
        if (script == null || script.getContent() == null || script.getContent().trim().isEmpty()) {
            throw new ScriptHandlerRunException("Script content cannot be empty", script);
        }

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByExtension(script.getLanguage().getFileExtension());

        if (engine == null) {
            throw new ScriptHandlerRunException(
                    "No script engine found for language: " + script.getLanguage(), script);
        }

        try {
            // Create a file for the script if needed
            File scriptFile = createFile(script);

            // Run the script and record execution time
            LocalDateTime startTime = LocalDateTime.now();
            Object result = engine.eval(script.getContent());
            LocalDateTime endTime = LocalDateTime.now();

            // Log successful execution
            String executionLog = String.format(
                    "<h3>Execution Successful</h3>" +
                            "<p>Started: %s</p>" +
                            "<p>Completed: %s</p>" +
                            "<p>Result: %s</p>",
                    formatDateTime(startTime),
                    formatDateTime(endTime),
                    result != null ? result.toString() : "No result"
            );

            // Store the execution log
            script.getLogs().put(UUID.randomUUID().toString(), executionLog);
            crudService.save(script);

            return true;
        } catch (ScriptException e) {
            // Format error message for the log
            String errorLog = String.format(
                    "<h3>Execution Failed</h3>" +
                            "<p>File: %s</p>" +
                            "<p>Line: %d</p>" +
                            "<p>Column: %d</p>" +
                            "<p>Error: %s</p>" +
                            "<pre><code>%s</code></pre>",
                    e.getFileName() != null ? e.getFileName() : "Unknown",
                    e.getLineNumber(),
                    e.getColumnNumber(),
                    e.getMessage(),
                    formatErrorContext(script.getContent(), e.getLineNumber())
            );

            // Store the error log
            script.getLogs().put(UUID.randomUUID().toString(), errorLog);
            crudService.save(script);

            // Throw exception with detailed info
            throw new ScriptHandlerRunException(
                    "Script execution failed at line " + e.getLineNumber(), script, e);
        }
    }

    /**
     * Create a file for the script
     *
     * @param script The script to create a file for
     * @return The created file
     * @throws ScriptHandlerRunException if there is an error creating the file
     */
    public File createFile(Script script) throws ScriptHandlerRunException {
        try {
            // Generate unique filename based on script ID and title
            String filename = String.format(
                    "%d_%s.%s",
                    script.getId(),
                    script.getTitle().replaceAll("[^a-zA-Z0-9]", "_"),
                    script.getLanguage().getFileExtension()
            );

            Path filePath = Paths.get(fileStorageLocation, filename);
            File file = filePath.toFile();

            // Write script content to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(script.getContent());
            }

            return file;
        } catch (IOException e) {
            throw new ScriptHandlerRunException("Failed to create script file", script, e);
        }
    }

    /**
     * Upload a script file and create or update a Script entity
     *
     * @param file The uploaded file
     * @param scriptId Script data (optional, for updating existing script)
     * @return The created or updated Script
     * @throws IOException if there is an error reading the file
     */
    public Script uploadScriptFile(MultipartFile file, Long scriptId) throws IOException, ScriptHandlerRunException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Get file extension to determine language
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Original filename cannot be null");
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex < 0) {
            throw new IllegalArgumentException("File must have an extension");
        }

        String extension = originalFilename.substring(lastDotIndex + 1);
        String content = new String(file.getBytes());

        // Update existing script or create new one
        Script script;
        if (scriptId != null) {
            script = crudService.findById(scriptId)
                    .orElseThrow(() -> new IllegalArgumentException("Script not found with ID: " + scriptId));
            script.setContent(content);
        } else {
            // Create new script with default values
            script = Script.builder()
                    .title(originalFilename.substring(0, lastDotIndex))
                    .content(content)
                    .isEnabled(true)
                    .build();

            // Try to determine script language from extension
            try {
                script.setLanguage(getLanguageFromExtension(extension));
            } catch (IllegalArgumentException e) {
                throw new ScriptHandlerRunException("Unsupported script language for extension: " + extension, script);
            }
        }

        return crudService.save(script);
    }

    /**
     * Download a script as a file
     *
     * @param id The ID of the script to download
     * @return The script file as a Resource
     * @throws MalformedURLException if there is an error creating the URL
     */
    public Resource downloadScript(Long id) throws MalformedURLException, ScriptHandlerRunException {
        Script script = crudService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Script not found with ID: " + id));

        // Create the file
        File file = createFile(script);

        // Return the file as a Resource
        Path path = file.toPath();
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read the script file");
        }
    }

    // Helper methods

    /**
     * Format the date and time for logs
     */
    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * Format error context for logs
     */
    private String formatErrorContext(String content, int errorLine) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String[] lines = content.split("\n");
        StringBuilder contextBuilder = new StringBuilder();

        // Show a few lines before and after the error
        int startLine = Math.max(0, errorLine - 3);
        int endLine = Math.min(lines.length, errorLine + 2);

        for (int i = startLine; i < endLine; i++) {
            boolean isErrorLine = i == errorLine - 1; // ScriptException line numbers are 1-based

            contextBuilder.append(String.format(
                    "%s%d: %s%s\n",
                    isErrorLine ? "➤ " : "  ",
                    i + 1,
                    lines[i],
                    isErrorLine ? " ← ERROR" : ""
            ));
        }

        return contextBuilder.toString();
    }

    /**
     * Get script language from file extension
     */
    private io.joshuasalcedo.script.model.ScriptLanguage getLanguageFromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "js" -> io.joshuasalcedo.script.model.ScriptLanguage.JAVASCRIPT;
            case "py" -> io.joshuasalcedo.script.model.ScriptLanguage.PYTHON;
            case "groovy" -> io.joshuasalcedo.script.model.ScriptLanguage.GROOVY;
            case "rb" -> io.joshuasalcedo.script.model.ScriptLanguage.RUBY;
            // Add more languages as needed
            default -> throw new IllegalArgumentException("Unsupported file extension: " + extension);
        };
    }
}
