package com.subsel.healthledger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class HealthLedgerApplication
{
    private static final Logger log = LoggerFactory.getLogger(HealthLedgerApplication.class);

    public static void main(String[] args)
    {
        log.info("Starting Health Ledger Server");
        SpringApplication.run(HealthLedgerApplication.class, args);
    }
}
