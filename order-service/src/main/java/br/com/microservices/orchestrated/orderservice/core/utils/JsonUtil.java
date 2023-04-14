package br.com.microservices.orchestrated.orderservice.core.utils;

import br.com.microservices.orchestrated.orderservice.core.dto.NotificationResponse;
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

    public NotificationResponse toNotification(String json) {
        try {
            return objectMapper.readValue(json, NotificationResponse.class);
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
