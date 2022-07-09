package com.subsel.healthledger.core.controller;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.EhrPOJO;
import com.subsel.healthledger.util.FabricUtils;
import com.subsel.healthledger.util.TxnIdGeneretaror;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/ehr")
public class EhrController extends BaseController {

    @PostMapping(value = "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createEhr(@RequestBody EhrPOJO ehrPOJO) throws Exception {

        String ehrId = TxnIdGeneretaror.generate();
        String issued = String.valueOf(new Date().getTime());
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pointer", ehrId);
        requestBody.put("key", ehrId);
        requestBody.put("username", ehrPOJO.getUname());
        requestBody.put("type", "ehr");
        requestBody.put("data", "QmHash##");
        requestBody.put("issued", issued);
        requestBody.put("maturity", "N/A");

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.CreateEhr.toString(),
                ehrPOJO.getUname(),
                FabricUtils.OrgMsp.Org1MSP.toString(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.CREATED);
    }

    @GetMapping(value = "/getAllEhrByUser", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAllEhr(@RequestParam String username) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.GetAllEhr.toString(),
                username,
                FabricUtils.OrgMsp.Org1MSP.toString(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEhr(@PathVariable String id, @RequestParam String username) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", id);

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadEhr.toString(),
                username,
                FabricUtils.OrgMsp.Org1MSP.toString(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }
}
