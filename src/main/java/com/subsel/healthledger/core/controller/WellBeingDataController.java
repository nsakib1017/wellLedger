package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.WellBeingPOJO;
import com.subsel.healthledger.utils.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/wellBeing")
public class WellBeingDataController extends BaseController {

    @PostMapping(value = "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> createWellBeingData(@RequestBody WellBeingPOJO wellBeingPOJO) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(wellBeingPOJO.getUname(), wellBeingPOJO.getOrgMsp());

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }

        String wellBeingId = TxnIdGeneretaror.generate();
        String issued = String.valueOf(new Date().getTime());
        String wellBeingDataString = FabricUtils.getWellBeingStringData(wellBeingPOJO);

        String cipherText = AESUtils.encryptString(wellBeingDataString, UserUtils.getUserPassword(wellBeingPOJO.getUname(), wellBeingPOJO.getOrgMsp()));

        byte[] strToBytes = cipherText.getBytes();
        String qmHash = IpfsClientUtils.getContentCid(strToBytes);

        requestBody.put("username", wellBeingPOJO.getUname());
        requestBody.put("pointer", wellBeingId);
        requestBody.put("key", wellBeingId);
        requestBody.put("type", FabricUtils.dataType.wellBeing);
        requestBody.put("data", qmHash);
        requestBody.put("issued", issued);
        requestBody.put("maturity", "N/A");

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.CreateWellBeingData.toString(),
                wellBeingPOJO.getUname(),
                wellBeingPOJO.getOrgMsp(),
                requestBody
        );

        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.CREATED);
    }

    @GetMapping(value = "/byUser", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAllWellBeingDataByUser(@RequestParam String username, @RequestParam String orgMsp) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(username, orgMsp);

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }
        requestBody.put("username", username);

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.GetAllWellBeingDataByUser.toString(),
                username,
                orgMsp,
                requestBody
        );
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getWellBeingData(@PathVariable String id, @RequestParam String username, @RequestParam String orgMsp) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> finalResult = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(username, orgMsp);

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }
        requestBody.put("id", id);

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadWellBeingData.toString(),
                username,
                orgMsp,
                requestBody
        );

        JsonNode pointerDataObj = (JsonNode) response.get("results");
        String pointerQmHash = String.valueOf(pointerDataObj.get("Data")).replace("\"", "");
        String pointerDataAsString = IpfsClientUtils.getContentFromCid(pointerQmHash);
        String plainText = AESUtils.decryptString(pointerDataAsString, UserUtils.getUserPassword(username, orgMsp));

        finalResult.put("fabricResult", pointerDataObj);
        finalResult.put("wellBeingData", FabricUtils.getWellBeingMappedData(plainText));

        return new ResponseEntity<Map<String, Object>>(finalResult, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/ticket/{ticketId}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getWellBeingDataWithTicket(@PathVariable String ticketId, @RequestParam String username, @RequestParam String mspOrg) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();

        boolean isLoggedIn = UserUtils.userLoggedIn(username, mspOrg);

        if(!isLoggedIn){
            response.put("message", "Please Login First");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.UNAUTHORIZED);
        }
        requestBody.put("id", ticketId);

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadWellBeingData.toString(),
                username,
                mspOrg,
                requestBody
        );

        JsonNode resultObj = (JsonNode) response.get("results");

        if (Long.parseLong(String.valueOf(resultObj.get("Maturity")).replace("\"", "")) < new Date().getTime()) {
                FabricUtils.getFabricResults(
                    FabricUtils.ContractName.DeleteTempWellBeingData.toString(),
                    username,
                    mspOrg,
                    requestBody
                );
                response.clear();
                response.put("message", "Ticket expired");
                return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }

        String pointerQmHashData = String.valueOf(resultObj.get("Data")).replace("\"", "");

        response.clear();
        response.put("fabricResult", resultObj);
        response.put("wellBeingData", FabricUtils.getWellBeingMappedData(pointerQmHashData));

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}
