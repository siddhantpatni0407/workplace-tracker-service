package com.sid.app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sid.app.model.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationUtils {

    /**
     * Gets json string.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the json string
     */
    public static <T> String getJSONString(T object) {
        if (object != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // ✅ Enables LocalDate serialization
            try {
                return objectMapper.writeValueAsString(object);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error occurred [{}] while converting to string [{}]", e.getMessage(), object);
                }
            }
        }
        return "";
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error occurred [{}] while reading from string [{}]", e.getMessage(), content);
            }
        }
        return null;
    }

    /**
     * Builds a response with dynamic status and message.
     *
     * @param data    The response data (nullable)
     * @param message Response message
     * @param status  HTTP status code
     * @param <T>     Type of the response data
     * @return ResponseEntity with standardized response format
     */
    public static <T> ResponseDTO<T> buildResponse(T data, String message, String status) {
        return ResponseDTO.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

}