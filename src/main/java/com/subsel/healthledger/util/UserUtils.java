package com.subsel.healthledger.util;

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
}
