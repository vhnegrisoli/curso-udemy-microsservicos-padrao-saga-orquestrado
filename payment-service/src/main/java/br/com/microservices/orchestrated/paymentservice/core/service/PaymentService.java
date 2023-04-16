package br.com.microservices.orchestrated.paymentservice.core.service;

import br.com.microservices.orchestrated.paymentservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaExecution;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private static final double MIN_VALUE_AMOUNT = 0.1;

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) {
        try {
            var orderId = event.getPayload().getId();
            var transactionId = event.getTransactionId();
            checkCurrentValidation(orderId, transactionId);
            createPendingPayment(event);
            var payment = findByOrderIdAndTransactionId(orderId, transactionId);
            validateAmount(payment.getTotalAmount());
            changePaymentToSuccess(payment);
            handleSuccess(event);
        } catch (Exception ex) {
            log.error("Error trying to make payment: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void checkCurrentValidation(String orderId, String transactionId) {
        if (paymentRepository.existsByOrderIdAndTransactionId(orderId, transactionId)) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
    }

    private void createPendingPayment(Event event) {
        var totalAmount = calculateAmount(event);
        var payment = Payment
            .builder()
            .orderId(event.getPayload().getId())
            .transactionId(event.getTransactionId())
            .totalAmount(totalAmount)
            .build();
        save(payment);
    }

    private void changePaymentToSuccess(Payment payment) {
        payment.setStatus(EPaymentStatus.SUCCESS);
        save(payment);
    }

    private void validateAmount(double amount) {
        if (amount < MIN_VALUE_AMOUNT) {
            throw new ValidationException("The minimal amount available is ".concat(String.valueOf(MIN_VALUE_AMOUNT)));
        }
    }

    private double calculateAmount(Event event) {
        return event
            .getPayload()
            .getProducts()
            .stream()
            .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
            .reduce(0.0, Double::sum);
    }

    private void handleSuccess(Event event) {
        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        event.setCurrentExecuted(ESagaExecution.CURRENT_IS_SUCCESS);
        addHistory(event, "Payment realized successfully!");
    }

    private void addHistory(Event event, String message) {
        var history = History
            .builder()
            .source(event.getSource())
            .status(event.getStatus())
            .message(message)
            .createdAt(LocalDateTime.now())
            .build();
        event.addToHistory(history);
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        event.setCurrentExecuted(ESagaExecution.CURRENT_FAIL_PENDING_ROLLBACK);
        addHistory(event, "Fail to realize payment: ".concat(message));
    }

    public void realizeRefund(Event event) {
        changePaymentStatusToRefund(event);
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        event.setCurrentExecuted(ESagaExecution.CURRENT_FAIL_EXECUTED_ROLLBACK);
        addHistory(event, "Rollback executed for payment!");
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void changePaymentStatusToRefund(Event event) {
        var payment = findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId());
        payment.setStatus(EPaymentStatus.REFUND);
        save(payment);
    }

    private Payment findByOrderIdAndTransactionId(String orderId, String transactionId) {
        return paymentRepository
            .findByOrderIdAndTransactionId(orderId, transactionId)
            .orElseThrow(() -> new ValidationException("Payment not found by orderID and transactionID"));
    }

    private void save(Payment payment) {
        paymentRepository.save(payment);
    }
}
