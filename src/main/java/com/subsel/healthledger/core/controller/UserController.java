package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.subsel.healthledger.common.model.BaseController;
import org.hyperledger.fabric.gateway.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/users")
public class UserController extends BaseController {

    @GetMapping(value="/{id}")
    public ResponseEntity<Map<String, Object>> getUserData(@PathVariable String id) throws IOException, ContractException {

        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Map<String, Object> response = new HashMap<>();
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricNetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);

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
            String data = String.valueOf(actualObj.get("Data"));

            if (data.equals("yes")) {
                if(Integer.parseInt(String.valueOf(actualObj.get("Maturity"))) < new Date().getTime()) {
                    contract.evaluateTransaction("ChangeData", id, "no");
                    response.put("message", "Token has expired");
                } else {
                    byte[] mainResult;
                    mainResult = contract.evaluateTransaction("ReadEhr", String.valueOf(actualObj.get("Key")));
                    JsonNode mainResultObject = mapper.readTree(new String(mainResult));
                    response.put("result", mainResultObject);
                }
            }else {
                response.put("message", "Permission denied");
            }
            HttpHeaders httpHeaders = new HttpHeaders();
            return new ResponseEntity<Map<String,Object>>(response, httpHeaders, HttpStatus.OK);
        }
    }
}
