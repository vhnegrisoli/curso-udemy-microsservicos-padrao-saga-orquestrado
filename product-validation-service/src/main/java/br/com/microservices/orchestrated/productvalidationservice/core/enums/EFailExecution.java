package br.com.microservices.orchestrated.productvalidationservice.core.enums;

public enum EFailExecution {

    CURRENT_FAIL_PENDING_ROLLBACK,
    CURRENT_FAIL_EXECUTED_ROLLBACK,
    CURRENT_IS_SUCCESS
}
