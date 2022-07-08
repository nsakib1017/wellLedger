package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.EhrPOJO;
import org.hyperledger.fabric.gateway.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/ehr")
public class EhrController extends BaseController {

    @GetMapping(value = "/", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAllEhr(@RequestBody EhrPOJO ehrPOJO) throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, ehrPOJO.getUname()).networkConfig(networkConfigPath).discovery(true);

        Map<String, Object> queryResults = new HashMap<String, Object>();

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            byte[] result;

            result = contract.evaluateTransaction("GetAllEhr", ehrPOJO.getUname());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(new String(result));
            queryResults.put("results", actualObj);

            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<Map<String, Object>>(queryResults, headers, HttpStatus.OK);
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getEhr(@PathVariable String id, @RequestBody EhrPOJO ehrPOJO) throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, ehrPOJO.getUname()).networkConfig(networkConfigPath).discovery(true);

        Map<String, Object> queryResults = new HashMap<String, Object>();

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            byte[] result;

            result = contract.evaluateTransaction("ReadEhr", id);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(new String(result));
            queryResults.put("results", actualObj);

            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<Map<String, Object>>(queryResults, headers, HttpStatus.OK);
        }
    }
}
