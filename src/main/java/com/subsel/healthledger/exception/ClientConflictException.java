package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ClientConflictException extends ResourceNotFoundException
{
    public ClientConflictException(Object fieldValue)
    {
        this("version", fieldValue);
    }

    public ClientConflictException(String fieldName, Object fieldValue)
    {
        super("Client", fieldName, fieldValue);
    }
}
