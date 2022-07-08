package com.subsel.healthledger.core.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsel.healthledger.common.controller.BaseController;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.subsel.healthledger.util.EhrUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.*;

@RestController
@RequestMapping(path = "/api/wallet")
public class WalletController extends BaseController {

    @GetMapping(value = "/enrollAdmin", produces = "application/json")
    public ResponseEntity<String> enrollAdmin() throws Exception {
        // Create a CA client for interacting with the CA.
        Properties props = new Properties();
        props.put("pemFile",
                String.format("%s/org1.example.com/ca/ca.org1.example.com-cert.pem", EhrUtils.pathToTestNetwork));
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check to see if we've already enrolled the admin user.
        if (wallet.get("admin") != null) {
            System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<String>("An identity for the admin user \"admin\" already exists in the wallet", headers, HttpStatus.BAD_REQUEST);
        }

        // Enroll the admin user, and import the new identity into the wallet.
        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");
        Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
        Identity user = Identities.newX509Identity("Org1MSP", enrollment);
        wallet.put("admin", user);
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<String>("Successfully enrolled user \"admin\" and imported it into the wallet", headers, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/registerUser", produces = "application/json")
    public ResponseEntity<String> registerUser() throws Exception {
        // Create a CA client for interacting with the CA.
        // Create a CA client for interacting with the CA.
        Properties props = new Properties();
        props.put("pemFile",
                String.format("%s/org1.example.com/ca/ca.org1.example.com-cert.pem", EhrUtils.pathToTestNetwork));
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check to see if we've already enrolled the user.
        if (wallet.get("appUser") != null) {
            String message = "An identity for the user \"appUser\" already exists in the wallet";
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<String>(message, headers, HttpStatus.BAD_REQUEST);

        }

        X509Identity adminIdentity = (X509Identity)wallet.get("admin");
        if (adminIdentity == null) {
            String message = "\"admin\" needs to be enrolled and added to the wallet first";
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<String>(message, headers, HttpStatus.BAD_REQUEST);
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
        String message = "Successfully enrolled user \"appUser\" and imported it into the wallet";
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<String>(message, headers, HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/testChainCode", produces = "application/json")
    public ResponseEntity<Map<String, Object>> testChainCode() throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);
        // load a CCP
        Path networkConfigPath = Paths.get("/Users/nsakibpriyo/go/src/github.com/nsakib1017/fabric-samples/fabcar/java/healthledger-2/src/main/java/com/subsel/healthledger/fabricnetwork/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);

        Map<String, Object> queryResults = new HashMap<String, Object>();

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            byte[] result;

            result = contract.evaluateTransaction("GetAllEhr", "genesis");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(new String(result));
            queryResults.put("queryAllCars", actualObj);

            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<Map<String, Object>>(queryResults, headers, HttpStatus.OK);
        }
    }


}
