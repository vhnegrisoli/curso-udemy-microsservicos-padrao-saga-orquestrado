package br.com.microservices.orchestrated.orchestratorservice.core.dto;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaExecution;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "event")
public class Event {

    @Id
    private String id;
    private String orderId;
    private String transactionId;
    private Object payload;
    private EEventSource source;
    private ESagaStatus status;
    private ESagaExecution currentExecuted;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void addToHistory(History history) {
        if (isEmpty(eventHistory)) {
            eventHistory = new ArrayList<>();
        }
        eventHistory.add(history);
    }
}
