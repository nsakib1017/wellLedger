package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.TicketPOJO;
import com.subsel.healthledger.util.FabricUtils;
import com.subsel.healthledger.util.TxnIdGeneretaror;

import org.apache.commons.math3.distribution.TriangularDistribution;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket")
public class TicketController extends BaseController {

    @PostMapping(value = "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createToken(@PathVariable String id, @RequestBody TicketPOJO ticketPojo) throws Exception {

        String pointer = TxnIdGeneretaror.generate();
        String maturity = String.valueOf(60 * 1000 * Long.parseLong(ticketPojo.getLimit()));
        String ehrId = TxnIdGeneretaror.generate();
        String issued = String.valueOf(new Date().getTime());
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pointer", pointer);
        requestBody.put("key", ehrId);
        requestBody.put("username", ticketPojo.getUname());
        requestBody.put("type", "permission");
        requestBody.put("data", "yes");
        requestBody.put("issued", issued);
        requestBody.put("maturity", maturity);

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.CreateEhr.toString(),
                ticketPojo.getUname(),
                FabricUtils.OrgMsp.Org1MSP.toString(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.CREATED);
    }

    @PostMapping(value = "/revoke/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> revokePermission(@PathVariable String id, @RequestBody TicketPOJO ticketPOJO) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", id);
        requestBody.put("data", String.valueOf(FabricUtils.permissionStatus.no));

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ChangeData.toString(),
                ticketPOJO.getUname(),
                FabricUtils.OrgMsp.Org1MSP.toString(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/extend/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> extendPermission(@PathVariable String id, @RequestBody TicketPOJO ticketPOJO) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", id);

        Map<String, Object>  response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadEhr.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        JsonNode resultObj = (JsonNode) response.get("results");
        String data = String.valueOf(resultObj.get("Data"));
        int oldMaturity = Integer.parseInt(String.valueOf(resultObj.get("Maturity")));

        if (data.equals(FabricUtils.permissionStatus.yes.toString())) {
            if(oldMaturity < new Date().getTime()) {
                requestBody.put("data", FabricUtils.permissionStatus.no.toString());
                response = FabricUtils.getFabricResults(
                        FabricUtils.ContractName.ChangeData.toString(),
                        ticketPOJO.getUname(),
                        ticketPOJO.getOrgMsp(),
                        requestBody
                );
            } else {
                oldMaturity = oldMaturity + Integer.parseInt(ticketPOJO.getLimit())*60*1000;
                requestBody.put("maturity", String.valueOf(oldMaturity));
                response = FabricUtils.getFabricResults(
                        FabricUtils.ContractName.ReadEhr.toString(),
                        ticketPOJO.getUname(),
                        ticketPOJO.getOrgMsp(),
                        requestBody
                );
            }
        } else {
            response.put("message", "Permission denied");
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);
    }
}
