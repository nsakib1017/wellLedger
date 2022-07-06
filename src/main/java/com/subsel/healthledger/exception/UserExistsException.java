package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserExistsException extends ResourceExitsException
{
    public UserExistsException(Object fieldValue)
    {
        this("value", fieldValue);
    }

    public UserExistsException(String fieldName, Object fieldValue)
    {
        super("User", fieldName, fieldValue);
    }

}
