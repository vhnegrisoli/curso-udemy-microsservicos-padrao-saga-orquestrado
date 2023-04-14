package br.com.microservices.orchestrated.orderservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String transactionId;
    private Object payload;
    private String source;
    private String status;
    private String currentExecuted;
    private List<Object> eventHistory;
}
