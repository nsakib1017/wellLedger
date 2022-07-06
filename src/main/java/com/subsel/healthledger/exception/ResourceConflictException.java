package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ResourceConflictException extends RuntimeException
{
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceConflictException(String resourceName, String fieldName, Object fieldValue)
    {
        super(String.format("%s conflict with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
