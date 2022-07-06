package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ClientNotFoundException extends ResourceNotFoundException
{
    public ClientNotFoundException(Object fieldValue)
    {
        this("unique id", fieldValue);
    }

    public ClientNotFoundException(String fieldName, Object fieldValue)
    {
        super("Client", fieldName, fieldValue);
    }
}
