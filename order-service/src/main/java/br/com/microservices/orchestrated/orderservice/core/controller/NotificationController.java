package br.com.microservices.orchestrated.orderservice.core.controller;

import br.com.microservices.orchestrated.orderservice.core.model.Notification;
import br.com.microservices.orchestrated.orderservice.core.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public List<Notification> findAll() {
        return service.findAll();
    }
}
