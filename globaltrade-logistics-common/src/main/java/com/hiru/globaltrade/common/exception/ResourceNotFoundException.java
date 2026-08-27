package com.hiru.globaltrade.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " was not found for identifier " + identifier);
    }
}
