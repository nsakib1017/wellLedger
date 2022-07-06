package com.subsel.healthledger.ping;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/ping")
public class PingController {

    @GetMapping("/basic")
    public ResponseEntity<Boolean> ping()
    {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(true, headers, HttpStatus.OK);
    }
}
