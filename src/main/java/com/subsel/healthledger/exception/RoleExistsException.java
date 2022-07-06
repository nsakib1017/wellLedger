package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class RoleExistsException extends ResourceExitsException
{
    public RoleExistsException(Object fieldValue)
    {
        this("name", fieldValue);
    }

    public RoleExistsException(String fieldName, Object fieldValue)
    {
        super("Role", fieldName, fieldValue);
    }

}
