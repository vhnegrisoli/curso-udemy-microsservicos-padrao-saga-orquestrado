package br.com.microservices.orchestrated.productvalidationservice.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "validation")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(nullable = false)
    private String orderId;

    @JoinColumn(nullable = false)
    private String transactionId;

    @JoinColumn(name = "product_id", nullable = false)
    private boolean success;
}
