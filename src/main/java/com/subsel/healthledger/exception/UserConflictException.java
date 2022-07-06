package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserConflictException extends ResourceNotFoundException
{
    public UserConflictException(Object fieldValue)
    {
        this("version", fieldValue);
    }

    public UserConflictException(String fieldName, Object fieldValue)
    {
        super("User", fieldName, fieldValue);
    }
}
