package com.subsel.healthledger.core.model;

import lombok.Data;

@Data
public class UserPOJO {
    private final String userName;
    private final String password;
    private final String mspOrg;
    private final String adminName;
}
