package br.com.microservices.orchestrated.orderservice.core.controller;

import br.com.microservices.orchestrated.orderservice.core.dto.OrderFilters;
import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.model.Order;
import br.com.microservices.orchestrated.orderservice.core.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@RestController
@AllArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private OrderService orderService;

    @PostMapping
    public Order create(@RequestBody OrderRequest order) {
        return orderService.createOrder(order);
    }

    @GetMapping
    public HashMap<String, Object> findByFilters(OrderFilters filters) {
        return orderService.findByFilters(filters);
    }

    @PostMapping("async")
    public CompletableFuture<HashMap<String, Object>> createAndReceive(@RequestBody OrderRequest order) {
        return orderService.createAndReceive(order);
    }
}
