package com.maiia.pro.exception;

import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler. I put minimal functionalities for demonstration purpose
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Build a standard error response object
     */
    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message
        );
    }

    /**
     * Handle when a requested resource is not found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    /**
     * Handle when a requested resource is not found
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        logger.warn("Not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    /**
     * Handle when a method is not implemented yet
     */
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ExceptionHandler(NotImplementedException.class)
    public ErrorResponse handleNotImplementedException(NotImplementedException ex) {
        logger.warn("Not implemented: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_IMPLEMENTED, "Method not implemented yet", ex.getMessage());
    }

    /**
     * Catch-all handler for unexpected exceptions
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(Exception ex) {
        logger.error("Internal server error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred : "+ex.getMessage()
        );
    }
}
