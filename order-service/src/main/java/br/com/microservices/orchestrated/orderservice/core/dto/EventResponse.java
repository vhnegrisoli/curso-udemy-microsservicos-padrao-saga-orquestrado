package br.com.microservices.orchestrated.orderservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private String id;
    private String orderId;
    private String transactionId;
    private Object payload;
    private String source;
    private String status;
    private String currentExecuted;
    private List<Object> eventHistory;
    private LocalDateTime createdAt;
}
