package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserProfileNotFoundException extends ResourceNotFoundException
{
    public UserProfileNotFoundException(Object fieldValue)
    {
        this("unique id", fieldValue);
    }

    public UserProfileNotFoundException(String fieldName, Object fieldValue)
    {
        super("User profile", fieldName, fieldValue);
    }
}
