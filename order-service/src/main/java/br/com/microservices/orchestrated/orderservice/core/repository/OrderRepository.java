package br.com.microservices.orchestrated.orderservice.core.repository;

import br.com.microservices.orchestrated.orderservice.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
