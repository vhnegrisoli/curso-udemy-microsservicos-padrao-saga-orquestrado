package br.com.microservices.orchestrated.orchestratorservice;

import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EFailExecution.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SagaExecutionControllerTest {

    private final SagaExecutionController sagaExecutionController = new SagaExecutionController();

    @BeforeEach
    public void configure() {
        sagaExecutionController.defineSagas();
    }

    @Test
    public void shouldReturnProductValidationSuccess_whenIsOrchestratorSuccess() {
        var topic = sagaExecutionController.getNextTopic(ORCHESTRATOR, SUCCESS, CURRENT_IS_SUCCESS);
        assertThat(topic).isEqualTo(PRODUCT_VALIDATION_SUCCESS);
    }

    @Test
    public void shouldReturnFinishFail_whenIsOrchestratorFail() {
        var topic = sagaExecutionController.getNextTopic(ORCHESTRATOR, FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
        assertThat(topic).isEqualTo(FINISH_FAIL);
    }

    @Test
    public void shouldReturnPaymentSuccess_whenIsProductValidationSuccess() {
        var topic = sagaExecutionController.getNextTopic(PRODUCT_VALIDATION_SERVICE, SUCCESS, CURRENT_IS_SUCCESS);
        assertThat(topic).isEqualTo(PAYMENT_SUCCESS);
    }

    @Test
    public void shouldReturnFinishFail_whenIsProductValidationFailCurrentExecuted() {
        var topic = sagaExecutionController.getNextTopic(PRODUCT_VALIDATION_SERVICE, FAIL, CURRENT_FAIL_PENDING_ROLLBACK);
        assertThat(topic).isEqualTo(FINISH_FAIL);
    }

    @Test
    public void shouldReturnProductValidationFail_whenIsProductValidationFailCurrentNotExecuted() {
        var topic = sagaExecutionController.getNextTopic(PRODUCT_VALIDATION_SERVICE, FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
        assertThat(topic).isEqualTo(PRODUCT_VALIDATION_FAIL);
    }

    @Test
    public void shouldReturnInventorySuccess_whenIsPaymentSuccess() {
        var topic = sagaExecutionController.getNextTopic(PAYMENT_SERVICE, SUCCESS, CURRENT_IS_SUCCESS);
        assertThat(topic).isEqualTo(INVENTORY_SUCCESS);
    }

    @Test
    public void shouldReturnPaymentFail_whenIsPaymentFailCurrentNotExecuted() {
        var topic = sagaExecutionController.getNextTopic(PAYMENT_SERVICE, FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
        assertThat(topic).isEqualTo(PAYMENT_FAIL);
    }

    @Test
    public void shouldReturnProductValidationFail_whenIsPaymentFailCurrentNotExecuted() {
        var topic = sagaExecutionController.getNextTopic(PAYMENT_SERVICE, FAIL, CURRENT_FAIL_PENDING_ROLLBACK);
        assertThat(topic).isEqualTo(PRODUCT_VALIDATION_FAIL);
    }

    @Test
    public void shouldReturnFinishSuccess_whenIsInventorySuccess() {
        var topic = sagaExecutionController.getNextTopic(INVENTORY_SERVICE, SUCCESS, CURRENT_IS_SUCCESS);
        assertThat(topic).isEqualTo(FINISH_SUCCESS);
    }

    @Test
    public void shouldReturnInventoryFail_whenIsInventoryFailCurrentNotExecuted() {
        var topic = sagaExecutionController.getNextTopic(INVENTORY_SERVICE, FAIL, CURRENT_FAIL_EXECUTED_ROLLBACK);
        assertThat(topic).isEqualTo(INVENTORY_FAIL);
    }

    @Test
    public void shouldReturnPaymentFail_whenIsInventoryFailCurrentExecuted() {
        var topic = sagaExecutionController.getNextTopic(INVENTORY_SERVICE, FAIL, CURRENT_FAIL_PENDING_ROLLBACK);
        assertThat(topic).isEqualTo(PAYMENT_FAIL);
    }

    @Test
    public void shouldThrowException_whenIsOrchestratorFailExecuted() {
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> sagaExecutionController.getNextTopic(ORCHESTRATOR, FAIL, CURRENT_FAIL_PENDING_ROLLBACK))
            .withMessage("Topic not found.");
    }

    @Test
    public void shouldThrowException_whenParameterAreNull() {
        var message = "Source, status and current execution must be informed.";

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> sagaExecutionController.getNextTopic(null, FAIL, CURRENT_FAIL_PENDING_ROLLBACK))
            .withMessage(message);

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> sagaExecutionController.getNextTopic(ORCHESTRATOR, null, CURRENT_FAIL_PENDING_ROLLBACK))
            .withMessage(message);

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> sagaExecutionController.getNextTopic(ORCHESTRATOR, FAIL, null))
            .withMessage(message);
    }
}
