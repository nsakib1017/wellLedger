package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.ExtendTimeLimitPOJO;
import org.hyperledger.fabric.gateway.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController extends BaseController {

    @PostMapping(value = "/revoke/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> revokePermission(@PathVariable String id) throws IOException, ContractException {
        Map<String, Object> response = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            contract.evaluateTransaction("ChangeData", id, "no");
            response.put("message", "Permission rvoked!!");

            return new ResponseEntity<Map<String, Object>>(response, httpHeaders, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/extend/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> extendPermission(@PathVariable String id, @RequestBody ExtendTimeLimitPOJO extendTimeLimitPOJO) throws IOException, ContractException {
        Map<String, Object> response = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            byte[] result;

            result = contract.evaluateTransaction("ReadEhr", id);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(new String(result));
            String data = String.valueOf(actualObj.get("Data"));
            int oldMaturity = Integer.parseInt(String.valueOf(actualObj.get("Maturity")));

            if (data.equals("yes")) {
                if(oldMaturity < new Date().getTime()) {
                    contract.evaluateTransaction("ChangeData", id, "no");
                    response.put("message", "Token has expired");
                } else {
                    oldMaturity = oldMaturity + Integer.parseInt(extendTimeLimitPOJO.getLimit())*60*1000;
                    contract.evaluateTransaction("ExtendLimit", id, String.valueOf(oldMaturity));
                    response.put("message", "Limit extended");
                }
            } else {
                response.put("message", "Permission denied");
            }
            return new ResponseEntity<Map<String,Object>>(response, httpHeaders, HttpStatus.OK);
        }
    }
}
