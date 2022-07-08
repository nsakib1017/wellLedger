package com.subsel.healthledger.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.gateway.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FabricUtils {

    enum OrgMsp {
        Org1MSP,
        Org2MSP
    }

    enum ContractName {
        Register,
        Login,
        CreateEhr,
        ReadEhr,
        ReadUser,
        DeleteTempEhr,
        EhrExists,
        ChangeData,
        UserExists,
        ExtendLimit,
        GetAllEhr
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

    public static String getNetworkConfigPath(String orgMsp) {
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

    public static Map<String, Object> contractFactory(Gateway gateway, String contractStringValue, Map<String, Object> requestResult) throws ContractException, JsonProcessingException {

        Network network = gateway.getNetwork(FabricNetworkConstants.wallet);
        Contract contract = network.getContract(FabricNetworkConstants.contractName);

        Map<String, Object> response = new HashMap<>();
        byte[] result;
        ContractName contractName = ContractName.valueOf(contractStringValue);

        switch (contractName){
            case Register:
                contract.evaluateTransaction(
                        contractName.toString(),
                        requestResult.get("username").toString(),
                        requestResult.get("certificate").toString(),
                        requestResult.get("password").toString(),
                        requestResult.get("mspId").toString()
                );
                response.put("message", "User registered");

                break;
            case Login:
                result = contract.evaluateTransaction(
                        contractName.toString(),
                        requestResult.get("username").toString(),
                        requestResult.get("password").toString()
                );
                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(new String(result));
                if (!actualObj.get("username").isEmpty())
                    response.put("message", "Login Successful");
                else
                    response.put("message", "Login Failed");
                break;
            default:
                response.put("message", "Invalid contract");
        }
        return response;
    }


}
