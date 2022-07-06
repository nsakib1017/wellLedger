package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends ResourceNotFoundException
{
    public RoleNotFoundException(Object fieldValue)
    {
        this("unique id", fieldValue);
    }

    public RoleNotFoundException(String fieldName, Object fieldValue)
    {
        super("Role", fieldName, fieldValue);
    }
}
