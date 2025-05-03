package io.joshuasalcedo.exception;

import io.joshuasalcedo.script.model.Script;
import lombok.Getter;

import javax.script.ScriptException;

/**
 * Exception thrown when there is an error executing a script
 */
@Getter
public class ScriptHandlerRunException extends ScriptException {

    private final Script script;
    private final String errorDetails;

    /**
     * Creates a new ScriptHandlerRunException
     *
     * @param message Error message
     * @param script The script that caused the exception
     */
    public ScriptHandlerRunException(String message, Script script) {
        super(message);
        this.script = script;
        this.errorDetails = null;
    }

    /**
     * Creates a new ScriptHandlerRunException with detailed error information
     *
     * @param message Error message
     * @param script The script that caused the exception
     * @param errorDetails Additional error details
     */
    public ScriptHandlerRunException(String message, Script script, String errorDetails) {
        super(message);
        this.script = script;
        this.errorDetails = errorDetails;
    }

    /**
     * Creates a new ScriptHandlerRunException caused by another exception
     *
     * @param message Error message
     * @param script The script that caused the exception
     * @param cause The underlying cause
     */
    public ScriptHandlerRunException(String message, Script script, Throwable cause) {
        super(message);
        this.script = script;
        this.errorDetails = cause.getMessage();
    }
}