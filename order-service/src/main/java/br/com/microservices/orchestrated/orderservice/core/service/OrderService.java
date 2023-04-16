package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.model.Event;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final SagaProducer producer;
    private final JsonUtil jsonUtil;
    private final OrderRepository repository;
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

    private Event createPayload(Order order) {
        return Event
            .builder()
            .orderId(order.getId())
            .transactionId(order.getTransactionId())
            .payload(order)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
