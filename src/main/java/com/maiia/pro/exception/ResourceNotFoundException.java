
package com.maiia.pro.exception;

/**
 * Exception thrown when a requested resource cannot be found
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Integer id) {
        super(resourceType + " with ID " + id + " not found");
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(resourceType + " with identifier " + identifier + " not found");
    }
}