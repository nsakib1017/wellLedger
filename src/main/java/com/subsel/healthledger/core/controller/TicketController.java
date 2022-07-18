package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.TicketPOJO;
import com.subsel.healthledger.utils.*;

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

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(ticketPOJO.getUname(), ticketPOJO.getOrgMsp());

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }

        String ticketId = TxnIdGeneretaror.generate();
        long issued = new Date().getTime();
        String maturity = String.valueOf(60 * 1000 * Long.parseLong(ticketPOJO.getLimit()) + issued);
        String wellBeingId = ticketPOJO.getKey();
        requestBody.put("id", ticketPOJO.getKey());

        Map<String, Object> pointerData = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadWellBeingData.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );
        requestBody.clear();
        requestBody.put("pointer", ticketId);
        requestBody.put("key", wellBeingId);
        requestBody.put("username", ticketPOJO.getUname());
        requestBody.put("type", FabricUtils.dataType.ticket);

        JsonNode pointerDataObj = (JsonNode) pointerData.get("results");
        String pointerQmHash = String.valueOf(pointerDataObj.get("Data")).replace("\"", "");
        String pointerDataAsString = IpfsClientUtils.getContentFromCid(pointerQmHash);

        String plainText = AESUtils.decryptString(pointerDataAsString, UserUtils.getUserPassword(ticketPOJO.getUname(), ticketPOJO.getOrgMsp()));

        requestBody.put("data", plainText);
        requestBody.put("issued", String.valueOf(issued));
        requestBody.put("maturity", maturity);

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.CreateWellBeingData.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.CREATED);
    }

    @PostMapping(value = "/revoke/{ticketId}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> revokePermission(@PathVariable String ticketId, @RequestBody TicketPOJO ticketPOJO) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(ticketPOJO.getUname(), ticketPOJO.getOrgMsp());

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }
        requestBody.put("id", ticketId);
        requestBody.put("data", String.valueOf(FabricUtils.permissionStatus.no));

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.DeleteTempWellBeingData.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/extend/{ticketId}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> extendPermission(@PathVariable String ticketId, @RequestBody TicketPOJO ticketPOJO) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(ticketPOJO.getUname(), ticketPOJO.getOrgMsp());

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }
        requestBody.put("id", ticketId);

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadWellBeingData.toString(),
                ticketPOJO.getUname(),
                ticketPOJO.getOrgMsp(),
                requestBody
        );

        JsonNode resultObj = (JsonNode) response.get("results");
        Long oldMaturity = Long.parseLong(String.valueOf(resultObj.get("Maturity")).replace("\"", ""));

        if (oldMaturity < new Date().getTime()) {
            FabricUtils.getFabricResults(
                    FabricUtils.ContractName.DeleteTempWellBeingData.toString(),
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

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}
