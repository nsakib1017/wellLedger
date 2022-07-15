package com.subsel.healthledger.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsel.healthledger.core.model.EhrPOJO;
import org.hyperledger.fabric.gateway.*;
import org.springframework.security.crypto.codec.Hex;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FabricUtils {
    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    public enum OrgMsp {
        Org1MSP,
        Org2MSP
    }

    public enum permissionStatus {
        yes,
        no
    }

    public enum dataType {
        permission,
        wellBeing
    }

    public enum ContractName {
        Register,
        LogIn,
        CreateEhr,
        ReadEhr,
        ReadUser,
        DeleteTempEhr,
        EhrExists,
        ChangeData,
        UserExists,
        ExtendLimit,
        GetAllEhr,
        GetAllUser,
        GetAllEhrByUser,
        UserIsLoggedIn,
        LogOut
    }

    public static Map<String, Object> getFabricResults(String contractName, String userName, String orgMsp, Map<String, Object> contractBody) throws Exception {
        // Load a file system based wallet for managing identities.

        Path walletPath = Paths.get(FabricNetworkConstants.wallet);
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        Path networkConfigPath = Paths.get(getNetworkConfigPath(orgMsp));

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, userName).networkConfig(networkConfigPath).discovery(true);

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {
           return contractFactory(gateway, contractName, contractBody);
        }
    }

    private static String getNetworkConfigPath(String orgMsp) {
        OrgMsp resultOrgMsp = OrgMsp.valueOf(orgMsp);
        String connectionPath;
        switch (resultOrgMsp) {
            case Org1MSP:
                connectionPath = FabricNetworkConstants.org1ConnectionYAML;
                break;
            case Org2MSP:
                connectionPath = FabricNetworkConstants.org2ConnectionYAML;
                break;
            default:
                connectionPath = "";
        }
        return connectionPath;
    }

    public static String getNetworkConfigCertPath(String orgMsp) {
        OrgMsp resultOrgMsp = OrgMsp.valueOf(orgMsp);
        String connectionPath;
        switch (resultOrgMsp) {
            case Org1MSP:
                connectionPath = FabricNetworkConstants.pathToOrg1TestNetwork;
                break;
            case Org2MSP:
                connectionPath = FabricNetworkConstants.pathToOrg2TestNetwork;
                break;
            default:
                connectionPath = "";
        }
        return connectionPath;
    }

    public static String getHFAClientURL(String orgMsp) {
        OrgMsp resultOrgMsp = OrgMsp.valueOf(orgMsp);
        String connectionPath;
        switch (resultOrgMsp) {
            case Org1MSP:
                connectionPath = FabricNetworkConstants.org1HfaClientURL;
                break;
            case Org2MSP:
                connectionPath = FabricNetworkConstants.org2HfaClientURL;
                break;
            default:
                connectionPath = "";
        }
        return connectionPath;
    }

    public static String getAffiliatedDept(String orgMsp) {
        OrgMsp resultOrgMsp = OrgMsp.valueOf(orgMsp);
        String affiliation;
        switch (resultOrgMsp) {
            case Org1MSP:
                affiliation = "org1.department1";
                break;
            case Org2MSP:
                affiliation = "org2.department1";
                break;
            default:
                affiliation = "";
        }
        return affiliation;
    }

    public static String getWellBeingStringData (EhrPOJO ehrPOJO) {
        return String.format("username => %s | type => %s | steps => %s | calorie => %s | sleep => %s | date => %s | heartRate => %s | orgMSP => %s",
                ehrPOJO.getUname(), ehrPOJO.getType(), ehrPOJO.getStepsCount(), ehrPOJO.getCalorieBurnt(), ehrPOJO.getSleepTime(), ehrPOJO.getDate(), ehrPOJO.getHeartRate(), ehrPOJO.getOrgMsp());
    }

    private static String getPasswordDigest (String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));
        return new String(Hex.encode(hash));
    }

    private static Map<String, Object> contractFactory(Gateway gateway, String contractStringValue, Map<String, Object> requestResult) throws Exception {

        Network network = gateway.getNetwork(FabricNetworkConstants.networkName);
        Contract contract = network.getContract(FabricNetworkConstants.contractName);

        Map<String, Object> response = new HashMap<>();
        byte[] result;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj;
        ContractName contractName = ContractName.valueOf(contractStringValue);

        switch (contractName) {
            case Register:
                contract.submitTransaction(
                        contractName.toString(),
                        requestResult.get("username").toString(),
                        requestResult.get("certificate").toString(),
                        getPasswordDigest(requestResult.get("password").toString()),
                        requestResult.get("mspId").toString()
                );
                response.put("message", "User registered");
                break;

            case LogIn:
                contract.submitTransaction(
                        contractName.toString(),
                        requestResult.get("username").toString(),
                        getPasswordDigest(requestResult.get("password").toString())
                );
                response.put("message", "Login Successful");
                break;

            case LogOut:
                contract.submitTransaction(
                        contractName.toString(),
                        requestResult.get("username").toString()
                );
                response.put("message", "Logout Successful");
                break;

            case CreateEhr:
                 contract.submitTransaction(
                        contractName.toString(),
                        requestResult.get("pointer").toString(),
                        requestResult.get("key").toString(),
                        requestResult.get("username").toString(),
                        requestResult.get("type").toString(),
                        requestResult.get("data").toString(),
                        requestResult.get("issued").toString(),
                        requestResult.get("maturity").toString()
                );
                response.put("ehrData", requestResult.get("pointer").toString());
                break;

            case GetAllEhrByUser:
            case ReadUser:
            case UserIsLoggedIn:
                result = contract.evaluateTransaction(
                        contractName.toString(),
                        requestResult.get("username").toString()
                );
                actualObj = mapper.readTree(new String(result));
                response.put("results", actualObj);
                break;

            case ReadEhr:
                result = contract.evaluateTransaction(
                        contractName.toString(),
                        requestResult.get("id").toString()
                );
                actualObj = mapper.readTree(new String(result));
                response.put("results", actualObj);
                break;

            case DeleteTempEhr:
                contract.submitTransaction(
                        contractName.toString(),
                        requestResult.get("id").toString()
                );
                response.put("message", "Permission revoked!!");
                break;

            case ExtendLimit:
                contract.submitTransaction(
                        contractName.toString(),
                        requestResult.get("id").toString(),
                        requestResult.get("maturity").toString());
                response.put("message", "Permission extended!!");
                break;

            case GetAllUser:
            case GetAllEhr:
                result = contract.evaluateTransaction(
                        contractName.toString());
                actualObj = mapper.readTree(new String(result));
                response.put("results", actualObj);
                break;

            default:
                response.put("message", "Invalid contract");
        }
        gateway.close();
        return response;
    }

}
