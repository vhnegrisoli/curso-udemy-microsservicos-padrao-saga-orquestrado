package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EFailExecution;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EFailExecution.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@AllArgsConstructor
public class SagaExecutionController {

    private static final List<SagaHandler> HANDLERS = new ArrayList<>();

    public SagaExecutionController defineSagas() {
        return SagaExecutionController
            .startSagaDefinition()
            .defineOrchestratorSaga()
            .defineProductValidationServiceSaga()
            .definePaymentServiceSaga()
            .defineInventoryServiceSaga()
            .finishSagaDefinition();
    }

    private SagaExecutionController defineOrchestratorSaga() {
        return this
            .defineSuccessSaga(ORCHESTRATOR, PRODUCT_VALIDATION_SUCCESS)
            .defineFailedSaga(ORCHESTRATOR, FINISH_FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
    }

    private SagaExecutionController defineProductValidationServiceSaga() {
        return this
            .defineSuccessSaga(PRODUCT_VALIDATION_SERVICE, PAYMENT_SUCCESS)
            .defineFailedSaga(PRODUCT_VALIDATION_SERVICE, PRODUCT_VALIDATION_FAIL, CURRENT_FAIL_PENDING_ROLLBACK)
            .defineFailedSaga(PRODUCT_VALIDATION_SERVICE, FINISH_FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
    }

    private SagaExecutionController definePaymentServiceSaga() {
        return this
            .defineSuccessSaga(PAYMENT_SERVICE, INVENTORY_SUCCESS)
            .defineFailedSaga(PAYMENT_SERVICE, PAYMENT_FAIL, CURRENT_FAIL_PENDING_ROLLBACK)
            .defineFailedSaga(PAYMENT_SERVICE, PRODUCT_VALIDATION_FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
    }

    private SagaExecutionController defineInventoryServiceSaga() {
        return this
            .defineSuccessSaga(INVENTORY_SERVICE, FINISH_SUCCESS)
            .defineFailedSaga(INVENTORY_SERVICE, INVENTORY_FAIL, CURRENT_FAIL_PENDING_ROLLBACK)
            .defineFailedSaga(INVENTORY_SERVICE, PAYMENT_FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
    }

    private static SagaExecutionController startSagaDefinition() {
        return new SagaExecutionController();
    }

    private SagaExecutionController finishSagaDefinition() {
        return this;
    }

    private SagaExecutionController defineSuccessSaga(EEventSource source,
                                                      ETopics topic) {
        var handler = buildHandler(source, SUCCESS, topic, CURRENT_IS_SUCCESS);
        return createHandler(handler);
    }

    private SagaExecutionController defineFailedSaga(EEventSource source,
                                                     ETopics topic,
                                                     EFailExecution failExecution) {
        var handler = buildHandler(source, FAIL, topic, failExecution);
        return createHandler(handler);
    }

    private SagaHandler buildHandler(EEventSource source,
                                     ESagaStatus status,
                                     ETopics topic,
                                     EFailExecution currentFailExecuted) {
        return SagaHandler
            .builder()
            .source(source)
            .status(status)
            .topic(topic.getTopic())
            .currentExecuted(currentFailExecuted)
            .build();
    }

    private SagaExecutionController createHandler(SagaHandler handler) {
        HANDLERS.add(handler);
        return this;
    }

    public ETopics getNextTopic(EEventSource source,
                                ESagaStatus status,
                                EFailExecution currentExecutionFailed) {
        if (isEmpty(source) || isEmpty(status) || isEmpty(currentExecutionFailed)) {
            throw new RuntimeException("Source, status and current execution must be informed.");
        }
        var topic = findBySourceStatusAndCurrentExecutionFail(source, status, currentExecutionFailed);
        logCurrentSaga(source, status, currentExecutionFailed, topic);
        return topic;
    }

    private ETopics findBySourceStatusAndCurrentExecutionFail(EEventSource source,
                                                              ESagaStatus status,
                                                              EFailExecution currentExecutionFailed) {
        return HANDLERS
            .stream()
            .filter(handler -> source.equals(handler.getSource())
                && status.equals(handler.getStatus())
                && currentExecutionFailed.equals(handler.getCurrentExecuted())
            )
            .map(SagaHandler::getTopic)
            .map(ETopics::getEnum)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Topic not found."));
    }

    private void logCurrentSaga(EEventSource source,
                                ESagaStatus status,
                                EFailExecution currentExecutionFailed,
                                ETopics topic) {
        var isFail = FAIL.equals(status);
        if (isFail) {
            var isCurrentExecuted = CURRENT_FAIL_PENDING_ROLLBACK.equals(currentExecutionFailed);
            if (isCurrentExecuted) {
                log.info("### CURRENT SAGA: '{}' | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC '{}'", source, topic);
            } else {
                log.info("### CURRENT SAGA: '{}' | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC '{}'", source, topic);
            }
        } else {
            log.info("### CURRENT SAGA: '{}' | SUCCESS | NEXT TOPIC '{}'", source, topic);
        }
    }
}
