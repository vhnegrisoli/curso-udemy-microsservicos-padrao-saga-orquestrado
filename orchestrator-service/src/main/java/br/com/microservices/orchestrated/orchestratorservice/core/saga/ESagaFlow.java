package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.*;

@Getter
@AllArgsConstructor
public enum ESagaFlow {

    ORCHESTRATOR_FLOW_START (ORCHESTRATOR, SUCCESS, PRODUCT_VALIDATION_SUCCESS),
    ORCHESTRATOR_FLOW_FAIL  (ORCHESTRATOR, FAIL, FINISH_FAIL),

    PRODUCT_FLOW_FAIL       (PRODUCT_VALIDATION_SERVICE, ROLLBACK_PENDING, PRODUCT_VALIDATION_FAIL),
    PRODUCT_FLOW_ROLLBACK   (PRODUCT_VALIDATION_SERVICE, FAIL, FINISH_FAIL),
    PRODUCT_FLOW_SUCCESS    (PRODUCT_VALIDATION_SERVICE, SUCCESS, PAYMENT_SUCCESS),

    PAYMENT_FLOW_FAIL       (PAYMENT_SERVICE, ROLLBACK_PENDING, PAYMENT_FAIL),
    PAYMENT_FLOW_ROLLBACK   (PAYMENT_SERVICE, FAIL, PRODUCT_VALIDATION_FAIL),
    PAYMENT_FLOW_SUCCESS    (PAYMENT_SERVICE, SUCCESS, INVENTORY_SUCCESS),

    INVENTORY_FLOW_FAIL     (INVENTORY_SERVICE, ROLLBACK_PENDING, INVENTORY_FAIL),
    INVENTORY_FLOW_ROLLBACK (INVENTORY_SERVICE, FAIL, PAYMENT_FAIL),
    INVENTORY_FLOW_SUCCESS  (INVENTORY_SERVICE, SUCCESS, FINISH_SUCCESS);

    private final EEventSource source;
    private final ESagaStatus status;
    private final ETopics topic;
}
