package com.subsel.healthledger.core.controller;
import com.subsel.healthledger.common.controller.BaseController;

import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import com.subsel.healthledger.core.model.AdminPOJO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.subsel.healthledger.util.FabricNetworkConstants;
import com.subsel.healthledger.util.FabricUtils;

import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping(path = "/api/admin")
public class AdminController extends BaseController {

    @PostMapping(value = "/enroll", produces = "application/json")
    public ResponseEntity<Map<String, Object>> enrollAdmin(@RequestBody AdminPOJO adminPOJO) throws Exception {
        // Create a CA client for interacting with the CA.
        Map<String, Object> response = new HashMap<>();
        Properties props = new Properties();
        props.put("pemFile", FabricUtils.getNetworkConfigCertPath(adminPOJO.getAdminOrgMsp()));
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = HFCAClient.createNewInstance(FabricUtils.getHFAClientURL(adminPOJO.getAdminOrgMsp()), props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);

        // Create a wallet for managing identities
        Wallet wallet = Wallets.newFileSystemWallet(Paths.get(FabricNetworkConstants.wallet));

        // Check to see if we've already enrolled the admin user.
        if (wallet.get(adminPOJO.getAdminName()) != null) {
            HttpHeaders headers = new HttpHeaders();
            response.put("message", "An identity for the admin user \"admin\" already exists in the wallet");
            return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.BAD_REQUEST);
        }

        // Enroll the admin user, and import the new identity into the wallet.
        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");
        Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
        Identity user = Identities.newX509Identity(FabricUtils.OrgMsp.valueOf(adminPOJO.getAdminOrgMsp()).toString(), enrollment);
        wallet.put(adminPOJO.getAdminName(), user);

        response.put("message", "Successfully enrolled user \"admin\" and imported it into the wallet");
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/getAllUser", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAllUser(@RequestParam String username, @RequestParam String orgMsp) throws Exception {
        // Create a CA client for interacting with the CA.
        Map<String, Object> response;

        Map<String, Object> requestBody = new HashMap<>();

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.GetAllUser.toString(),
                username,
                orgMsp,
                requestBody
        );
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/getAllEhr", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getAllEhr(@RequestParam String username, @RequestParam String orgMsp) throws Exception {
        // Create a CA client for interacting with the CA.
        Map<String, Object> response;

        Map<String, Object> requestBody = new HashMap<>();

        response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.GetAllEhr.toString(),
                username,
                orgMsp,
                requestBody
        );
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Map<String, Object>>(response, headers, HttpStatus.OK);
    }

}
