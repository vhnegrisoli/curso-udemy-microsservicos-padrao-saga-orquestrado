package br.com.microservices.orchestrated.orderservice.core.controller;

import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.document.Order;
import br.com.microservices.orchestrated.orderservice.core.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private OrderService orderService;

    @PostMapping
    public Order create(@RequestBody OrderRequest order) {
        return orderService.createOrder(order);
    }
}
