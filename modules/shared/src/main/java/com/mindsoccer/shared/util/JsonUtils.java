package com.mindsoccer.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utilitaires pour la sérialisation/désérialisation JSON.
 */
public final class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtils() {
        // Utility class
    }

    /**
     * Retourne l'ObjectMapper partagé.
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Sérialise un objet en JSON.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }

    /**
     * Sérialise un objet en JSON formaté (pretty print).
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }

    /**
     * Désérialise du JSON en objet.
     */
    public static <T> Optional<T> fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return Optional.empty();
        }
        try {
            return Optional.of(MAPPER.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Désérialise du JSON en objet avec TypeReference (pour les génériques).
     */
    public static <T> Optional<T> fromJson(String json, TypeReference<T> typeRef) {
        if (StringUtils.isBlank(json)) {
            return Optional.empty();
        }
        try {
            return Optional.of(MAPPER.readValue(json, typeRef));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Désérialise du JSON en Map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to Map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Désérialise du JSON en liste.
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json,
                MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to List: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Convertit un objet en Map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return MAPPER.convertValue(obj, Map.class);
    }

    /**
     * Convertit une Map en objet.
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        return MAPPER.convertValue(map, clazz);
    }
}
