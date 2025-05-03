package io.joshuasalcedo.script.domain.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for logging messages.
 */
@Service
public class LoggingService {

    /**
     * Logs a message with the specified level.
     * @param level the log level
     * @param message the message to log
     * @param source the source of the log message
     */
    public void log(LogLevel level, String message, Class<?> source) {
        Logger logger = LoggerFactory.getLogger(source);
        
        switch (level) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
            default:
                logger.info(message);
                break;
        }
    }

    /**
     * Logs a message with the specified level.
     * @param level the log level
     * @param message the message to log
     * @param source the source of the log message
     * @param throwable the throwable to log
     */
    public void log(LogLevel level, String message, Class<?> source, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(source);
        
        switch (level) {
            case TRACE:
                logger.trace(message, throwable);
                break;
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
            default:
                logger.info(message, throwable);
                break;
        }
    }
}