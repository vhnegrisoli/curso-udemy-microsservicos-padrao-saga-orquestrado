package br.com.microservices.orchestrated.orderservice.core.utils;

import br.com.microservices.orchestrated.orderservice.core.dto.EventResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception ex) {
            return "";
        }
    }

    public EventResponse toEvent(String json) {
        try {
            return objectMapper.readValue(json, EventResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public <T> T toObject(Object object, Class<T> classType) {
        try {
            return objectMapper.convertValue(object, classType);
        } catch (Exception ex) {
            return null;
        }
    }
}
