package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.ROLLBACK_PENDING;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.SUCCESS;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@AllArgsConstructor
public class SagaExecutionController {

    public ETopics getNextTopic(Event event) {
        if (isEmpty(event.getSource()) || isEmpty(event.getStatus())) {
            throw new ValidationException("Source and status must be informed.");
        }
        var topic = findTopicBySourceAndStatus(event);
        logCurrentSaga(event, topic);
        return topic;
    }

    private ETopics findTopicBySourceAndStatus(Event event) {
        return Arrays.stream(ESagaFlow.values())
            .filter(flow -> flow.getSource().equals(event.getSource())
                && flow.getStatus().equals(event.getStatus()))
            .findFirst()
            .orElseThrow(() -> new ValidationException("Topic not found."))
            .getTopic();
    }

    private void logCurrentSaga(Event event, ETopics topic) {
        if (SUCCESS.equals(event.getStatus())) {
            log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} - EVENT: {}", event.getSource(), topic, event.getId());
        } else {
            if (ROLLBACK_PENDING.equals(event.getStatus())) {
                log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} - EVENT: {}",
                    event.getSource(), topic, event.getId());
            } else {
                log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} - EVENT: {}",
                    event.getSource(), topic, event.getId());
            }
        }
    }
}
