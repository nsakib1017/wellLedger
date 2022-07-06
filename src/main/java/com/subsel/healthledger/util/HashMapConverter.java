package com.subsel.healthledger.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;

public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(HashMapConverter.class);

    @Override
    public String convertToDatabaseColumn(Map<String, Object> accountInfo) {

        String accountInfoJson = null;
        try {
            accountInfoJson = objectMapper.writeValueAsString(accountInfo);
        } catch (final JsonProcessingException e) {
            logger.error("JSON writing error", e);
        }

        return accountInfoJson;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String accountInfoJSON) {

        Map<String, Object> accountInfo = null;
        try {
            accountInfo = objectMapper.readValue(accountInfoJSON, Map.class);
        } catch (final IOException e) {
            logger.error("JSON reading error", e);
        }

        return accountInfo;
    }

}