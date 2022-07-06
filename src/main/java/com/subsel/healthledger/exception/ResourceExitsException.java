package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ResourceExitsException extends RuntimeException
{
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceExitsException(String resourceName, String fieldName, Object fieldValue)
    {
        super(String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceExitsException(String resourceName, Object instanceName)
    {
        super(String.format("%s already exists with the name : '%s'", resourceName, instanceName));
        this.resourceName = resourceName;
        this.fieldName = "name";
        this.fieldValue = instanceName;
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
