package br.com.microservices.orchestrated.orderservice.core.consumer;

import br.com.microservices.orchestrated.orderservice.core.model.Notification;
import br.com.microservices.orchestrated.orderservice.core.service.NotificationService;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationConsumer {

    private final NotificationService service;
    private final JsonUtil jsonUtil;

    @KafkaListener(
        groupId = "${spring.kafka.consumer.group-id}",
        topics = "${spring.kafka.topic.notify-ending}"
    )
    public void consumeStartSagaEvent(String payload) {
        log.info("Recieving ending notification event {} from notify-ending topic", payload);
        var response = jsonUtil.toNotification(payload);
        var notification = jsonUtil.toObject(response, Notification.class);
        service.notifyEnding(notification);
    }
}
