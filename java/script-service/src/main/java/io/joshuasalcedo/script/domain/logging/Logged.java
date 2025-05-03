package io.joshuasalcedo.script.domain.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for logging method calls.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Logged {
    /**
     * The log level to use.
     * @return the log level
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * Whether to log the return value of the method.
     * @return true if the return value should be logged, false otherwise
     */
    boolean logReturnValue() default false;

    /**
     * Whether to log the parameters of the method.
     * @return true if the parameters should be logged, false otherwise
     */
    boolean logParameters() default false;
}