package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.core.dto.OrderFilters;
import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.model.Order;
import br.com.microservices.orchestrated.orderservice.core.producer.SagaProducer;
import br.com.microservices.orchestrated.orderservice.core.repository.OrderRepository;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String PAYLOAD = "payload";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_PATTERN = "%s_%s";
    private static final Long AWAIT_FOR_COMPLETE_IN_SECONDS = 5000L;

    private final SagaProducer producer;
    private final JsonUtil jsonUtil;
    private final OrderRepository repository;
    private final NotificationService notificationService;
    @Value("${spring.kafka.topic.start-saga}")
    private String startSagaTopic;

    public Order createOrder(OrderRequest orderRequest) {
        var order = new Order();
        order.setProducts(orderRequest.getProducts());
        var now = LocalDateTime.now();
        order.setCreatedAt(now);
        repository.save(order);
        order.setTransactionId(
            String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID())
        );
        producer.sendEvent(jsonUtil.toJson(createPayload(order)), startSagaTopic);
        return order;
    }

    private HashMap<String, Object> createPayload(Order order) {
        var payload = new HashMap<String, Object>();
        payload.put(PAYLOAD, order);
        payload.put(TRANSACTION_ID, order.getTransactionId());
        return payload;
    }

    public CompletableFuture<HashMap<String, Object>> createAndReceive(OrderRequest order) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var createdOrder = createOrder(order);
                var response = new HashMap<String, Object>();
                response.put("order", createdOrder);
                Thread.sleep(AWAIT_FOR_COMPLETE_IN_SECONDS);
                response.put("notification",
                    notificationService.findByTransactionId(createdOrder.getTransactionId()));
                return response;
            } catch (Exception ex) {
                throw new RuntimeException("Error trying to get order and notification: " + ex.getMessage());
            }
        });
    }

    public HashMap<String, Object> findByFilters(OrderFilters filters) {
        Order order;
        validateEmptyFilters(filters);
        if (!isEmpty(filters.getId())) {
            order = repository.findById(filters.getId())
                .orElseThrow(() -> new RuntimeException("Order not found by ID."));
        } else {
            order = repository.findByTransactionId(filters.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Order not found by transactionID."));
        }
        var execution = notificationService.findByTransactionId(order.getTransactionId());
        var response = new HashMap<String, Object>();
        response.put("order", order);
        response.put("execution", execution);
        return response;
    }

    private void validateEmptyFilters(OrderFilters filters) {
        if (isEmpty(filters.getId()) && isEmpty(filters.getTransactionId())) {
            throw new RuntimeException("ID or TransactionID must be informed.");
        }
    }
}
