package br.com.microservices.orchestrated.inventoryservice.core.service;

import br.com.microservices.orchestrated.inventoryservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.History;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Order;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.inventoryservice.core.model.Inventory;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import br.com.microservices.orchestrated.inventoryservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.inventoryservice.core.repository.InventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.repository.OrderInventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.orchestrated.inventoryservice.core.enums.ESagaStatus.*;

@Slf4j
@Service
@AllArgsConstructor
public class InventoryService {

    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final InventoryRepository inventoryRepository;
    private final OrderInventoryRepository orderInventoryRepository;

    public void updateInventory(Event event) {
        try {
            checkCurrentValidation(event.getPayload().getId(), event.getTransactionId());
            createOrderInventory(event);
            updateInventory(event.getPayload());
            handleSuccess(event);
        } catch (Exception ex) {
            log.error("Error trying to update inventory: ", ex);
            handleFailCurrentNotExecuted(event, ex.getMessage());
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void checkCurrentValidation(String orderId, String transactionId) {
        if (orderInventoryRepository.existsByOrderIdAndTransactionId(orderId, transactionId)) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
    }

    private void createOrderInventory(Event event) {
        event.getPayload().getProducts()
            .forEach(product -> {
                var inventory = findInventoryByProductCode(product.getProduct().getCode());
                var orderInventory = createOrderInventory(
                    event.getPayload().getId(), event.getTransactionId(), product, inventory);
                orderInventoryRepository.save(orderInventory);
            });
    }

    private void updateInventory(Order order) {
        order.getProducts().forEach(product -> {
           var inventory = findInventoryByProductCode(product.getProduct().getCode());
            checkInventory(inventory.getAvailable(), product.getQuantity());
            inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
            inventoryRepository.save(inventory);
        });
    }

    private void checkInventory(int available, int orderQuantity) {
        if (orderQuantity > available) {
            throw new ValidationException("Product is out of stock!");
        }
    }

    private OrderInventory createOrderInventory(String orderId,
                                                String transactionId,
                                                OrderProducts product,
                                                Inventory inventory) {
        return OrderInventory
            .builder()
            .inventory(inventory)
            .oldQuantity(inventory.getAvailable())
            .orderQuantity(product.getQuantity())
            .newQuantity(inventory.getAvailable() - product.getQuantity())
            .orderId(orderId)
            .transactionId(transactionId)
            .build();
    }

    private void handleSuccess(Event event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Inventory updated successfully!");
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
        event.setStatus(ROLLBACK_PENDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to update inventory: ".concat(message));
    }

    public void rollbackInventory(Event event) {
        returnInventoryToPreviousValues(event);
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Rollback executed for inventory!");
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void returnInventoryToPreviousValues(Event event) {
        orderInventoryRepository
            .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
            .forEach(orderInventory -> {
                var inventory = findById(orderInventory.getInventory().getId());
                inventory.setAvailable(orderInventory.getOldQuantity());
                inventoryRepository.save(inventory);
                log.info("Restored inventory for order {}: from {} to {}",
                    event.getPayload().getId(), orderInventory.getOldQuantity(), inventory.getAvailable());
            });
    }

    private Inventory findById(Integer id) {
        return inventoryRepository
            .findById(id)
            .orElseThrow(() -> new ValidationException("Inventory not found by id."));
    }

    private Inventory findInventoryByProductCode(String productCode) {
        return inventoryRepository
            .findByProductCode(productCode)
            .orElseThrow(() -> new ValidationException("Inventory not found by informed product."));
    }
}
