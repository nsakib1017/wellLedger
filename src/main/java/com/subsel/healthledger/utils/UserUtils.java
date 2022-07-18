package com.subsel.healthledger.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class UserUtils {
    public static boolean userLoggedIn(String username, String orgMsp) throws Exception {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);

        Map<String, Object> isUserLoggedIn = FabricUtils.getFabricResults(
                FabricUtils.ContractName.UserIsLoggedIn.toString(),
                username,
                orgMsp,
                requestBody
        );
        return Boolean.parseBoolean(isUserLoggedIn.get("results").toString());
    }

    public static String getUserPassword (String username, String orgMSP) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", username);

        Map<String, Object> response = FabricUtils.getFabricResults(
                FabricUtils.ContractName.ReadUser.toString(),
                username,
                orgMSP,
                requestBody
        );

        JsonNode resultObj = (JsonNode) response.get("results");
        return String.valueOf(resultObj.get("Password")).replace("\"", "");
    }
}
