package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.core.model.Notification;
import br.com.microservices.orchestrated.orderservice.core.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationService {

    private static final String ID = "id";

    private final NotificationRepository repository;

    public void notifyEnding(Notification notification) {
        var order = (LinkedHashMap) notification.getPayload();
        notification.setOrderId((String) order.get(ID));
        notification.setCreatedAt(LocalDateTime.now());
        repository.save(notification);
        log.info("Order {} with saga notified! TransactionId: {}", notification.getOrderId(), notification.getTransactionId());
    }

    public List<Notification> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Notification findByTransactionId(String transactionId) {
        return repository
            .findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Notification not found."));
    }
}
