package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.model.Event;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private static final String ID = "id";

    private final EventRepository repository;

    public void notifyEnding(Event event) {
        var order = (LinkedHashMap) event.getPayload();
        event.setOrderId((String) order.get(ID));
        event.setCreatedAt(LocalDateTime.now());
        repository.save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters filters) {
        validateEmptyFilters(filters);
        if (!isEmpty(filters.getOrderId())) {
            return findByOrderId(filters.getOrderId());
        } else {
            return findByTransactionId(filters.getTransactionId());
        }
    }

    private void validateEmptyFilters(EventFilters filters) {
        if (isEmpty(filters.getTransactionId()) && isEmpty(filters.getTransactionId())) {
            throw new ValidationException("OrderID or TransactionID must be informed.");
        }
    }

    private Event findByTransactionId(String transactionId) {
        return repository
            .findByTransactionId(transactionId)
            .orElseThrow(() -> new ValidationException("Event not found by transactionId."));
    }

    private Event findByOrderId(String orderId) {
        return repository
            .findByOrderId(orderId)
            .orElseThrow(() -> new ValidationException("Event not found by orderID."));
    }
}
