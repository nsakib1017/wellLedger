package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.TicketPOJO;
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
@RequestMapping("/api/ticket")
public class TicketController extends BaseController {

    @PostMapping(value = "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createToken(@RequestBody TicketPOJO ticketPOJO) throws Exception {

        String pointer = TxnIdGeneretaror.generate();
        long issued = new Date().getTime();
        String maturity = String.valueOf(60 * 1000 * Long.parseLong(ticketPOJO.getLimit()) + issued);
        String ehrId = ticketPOJO.getKey();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pointer", pointer);
        requestBody.put("key", ehrId);
        requestBody.put("username", ticketPOJO.getUname());
        requestBody.put("type", FabricUtils.dataType.permission);
        requestBody.put("data", FabricUtils.permissionStatus.yes);
        requestBody.put("issued", String.valueOf(issued));
        requestBody.put("maturity", maturity);

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.CreateEhr.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.CREATED);
    }

    @PostMapping(value = "/revoke/{ticketId}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> revokePermission(@PathVariable String ticketId, @RequestBody TicketPOJO ticketPOJO) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", ticketId);
        requestBody.put("data", String.valueOf(FabricUtils.permissionStatus.no));

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.DeleteTempEhr.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/extend/{ticketId}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> extendPermission(@PathVariable String ticketId, @RequestBody TicketPOJO ticketPOJO) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", ticketId);

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadEhr.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        JsonNode resultObj = (JsonNode) response.get("results");
        Long oldMaturity = Long.parseLong(String.valueOf(resultObj.get("Maturity")).replace("\"", ""));

        if (oldMaturity < new Date().getTime()) {
            FabricUtils.getFabricResults(
                    FabricUtils.ContractName.DeleteTempEhr.toString(),
                    ticketPOJO.getUname(),
                    ticketPOJO.getOrgMsp(),
                    requestBody
            );
        } else {
            oldMaturity = oldMaturity + Long.parseLong(ticketPOJO.getLimit()) * 60 * 1000;
            requestBody.put("maturity", String.valueOf(oldMaturity));
            response = FabricUtils.getFabricResults(
                    FabricUtils.ContractName.ExtendLimit.toString(),
                    ticketPOJO.getUname(),
                    ticketPOJO.getOrgMsp(),
                    requestBody
            );
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);
    }
}
