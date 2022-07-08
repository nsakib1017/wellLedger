package com.subsel.healthledger.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.subsel.healthledger.common.controller.BaseController;
import com.subsel.healthledger.core.model.UserPOJO;
import com.subsel.healthledger.util.FabricNetworkConstants;
import com.subsel.healthledger.util.FabricUtils;
import okhttp3.Request;
import org.hyperledger.fabric.gateway.*;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.*;

@RestController
@RequestMapping(path = "/api/users")
public class UserController extends BaseController {

    @PostMapping(value = "/", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserPOJO userPOJO, Request req) throws Exception{
        // Create a CA client for interacting with the CA.
        Map<String, Object> response = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        Properties props = new Properties();
        props.put("pemFile", FabricUtils.getNetworkConfigCertPath(userPOJO.getMspOrg()));
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get(FabricNetworkConstants.wallet));

        // Check to see if we've already enrolled the user.
        if (wallet.get(userPOJO.getUserName()) != null) {
            String message = "An identity for the user \"appUser\" already exists in the wallet";
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.BAD_REQUEST);

        }

        X509Identity adminIdentity = (X509Identity)wallet.get("admin");
        if (adminIdentity == null) {
            String message = "\"admin\" needs to be enrolled and added to the wallet first";
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.BAD_REQUEST);
        }
        User admin = new User() {

            @Override
            public String getName() {
                return "admin";
            }

            @Override
            public Set<String> getRoles() {
                return null;
            }

            @Override
            public String getAccount() {
                return null;
            }

            @Override
            public String getAffiliation() {
                return "org1.department1";
            }

            @Override
            public Enrollment getEnrollment() {
                return new Enrollment() {

                    @Override
                    public PrivateKey getKey() {
                        return adminIdentity.getPrivateKey();
                    }

                    @Override
                    public String getCert() {
                        return Identities.toPemString(adminIdentity.getCertificate());
                    }
                };
            }

            @Override
            public String getMspId() {
                return "Org1MSP";
            }

        };

        // Register the user, enroll the user, and import the new identity into the wallet.
        RegistrationRequest registrationRequest = new RegistrationRequest("appUser");
        registrationRequest.setAffiliation("org1.department1");
        registrationRequest.setEnrollmentID("appUser");
        String enrollmentSecret = caClient.register(registrationRequest, admin);
        Enrollment enrollment = caClient.enroll("appUser", enrollmentSecret);
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put("appUser", user);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(
                userPOJO.getPassWord().getBytes(StandardCharsets.UTF_8));
        String sha256hex = new String(Hex.encode(hash));

        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", userPOJO.getUserName());
            requestBody.put("certificate", enrollment.getCert());
            requestBody.put("password", sha256hex);
            requestBody.put("mspId", user.getMspId());


            contract.evaluateTransaction("Register", userPOJO.getUserName(), enrollment.getCert(),sha256hex, user.getMspId());
            response.put("message", "User registered");

            return new ResponseEntity<Map<String, Object>>(response, httpHeaders, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody UserPOJO userPOJO, Request req) throws IOException, ContractException, NoSuchAlgorithmException {
        Map<String, Object> response = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(
                userPOJO.getPassWord().getBytes(StandardCharsets.UTF_8));
        String sha256hex = new String(Hex.encode(hash));

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            byte[] result;
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", userPOJO.getUserName());
            requestBody.put("password", sha256hex);

            result = contract.evaluateTransaction("Login", userPOJO.getUserName(), sha256hex);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(new String(result));
            if(!actualObj.get("username").isEmpty())
                response.put("message", "Login Successful");
            else
                response.put("message", "Login Failed");

            return new ResponseEntity<Map<String, Object>>(response, httpHeaders, HttpStatus.OK);
        }
    }

    @GetMapping(value="/data/{id}")
    public ResponseEntity<Map<String, Object>> getUserData(@PathVariable String id, Request req) throws IOException, ContractException {

        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Map<String, Object> response = new HashMap<>();
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
            } else {
                response.put("message", "Permission denied");
            }
            HttpHeaders httpHeaders = new HttpHeaders();
            return new ResponseEntity<Map<String,Object>>(response, httpHeaders, HttpStatus.OK);
        }
    }
}
