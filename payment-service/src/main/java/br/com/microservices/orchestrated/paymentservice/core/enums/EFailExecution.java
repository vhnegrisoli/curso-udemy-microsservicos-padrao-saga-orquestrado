package br.com.microservices.orchestrated.paymentservice.core.enums;

public enum EFailExecution {

    CURRENT_FAIL_PENDING_ROLLBACK,
    CURRENT_FAIL_EXECUTED_ROLLBACK,
    CURRENT_IS_SUCCESS
}
