package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserRoleExistsException extends ResourceExitsException
{
    public UserRoleExistsException(Object fieldValue)
    {
        this("role", fieldValue);
    }

    public UserRoleExistsException(String fieldName, Object fieldValue)
    {
        super("UserRole", fieldName, fieldValue);
    }

}
