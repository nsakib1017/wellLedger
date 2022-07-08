package com.subsel.healthledger.common.controller;

import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
public class BaseController implements Serializable {
    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }
}
