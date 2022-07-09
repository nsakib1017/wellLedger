package com.subsel.healthledger.core.controller;
import com.subsel.healthledger.common.controller.BaseController;

import com.subsel.healthledger.util.FabricUtils;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.subsel.healthledger.util.FabricNetworkConstants;

import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping(path = "/api/admin")
public class AdminController extends BaseController {

    @PostMapping(value = "/enroll", produces = "application/json")
    public ResponseEntity<Map<String, Object>> enrollAdmin() throws Exception {
        // Create a CA client for interacting with the CA.
        Map<String, Object> response = new HashMap<>();
        Properties props = new Properties();
        props.put("pemFile", FabricNetworkConstants.pathToOrg1TestNetwork);
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

        // Check to see if we've already enrolled the admin user.
        if (wallet.get("admin") != null) {
            HttpHeaders headers = new HttpHeaders();
            response.put("message", "An identity for the admin user \"admin\" already exists in the wallet");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.BAD_REQUEST);
        }

        // Enroll the admin user, and import the new identity into the wallet.
        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");
        Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
        Identity user = Identities.newX509Identity(String.valueOf(FabricUtils.OrgMsp.Org1MSP), enrollment);
        wallet.put("admin", user);

        response.put("message", "Successfully enrolled user \"admin\" and imported it into the wallet");
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

}