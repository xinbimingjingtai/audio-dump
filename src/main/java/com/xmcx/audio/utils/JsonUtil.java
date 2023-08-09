package com.xmcx.audio.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Json util
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static <T> T readObject(byte[] data, Class<T> cls) {
        try {
            return MAPPER.readValue(data, cls);
        } catch (IOException e) {
            // should not happen, otherwise modify the mapper configuration
            LoggerUtil.warn("Can not parse '%s'", new String(data, StandardCharsets.UTF_8));
            throw new RuntimeException(e);
        }
    }

    public static String writeString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // should not happen
            LoggerUtil.warn("Can not generate '%s'", obj);
            throw new RuntimeException(e);
        }
    }

}
