package com.luis.springcloud.eurekaserver.app.msorderapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "order_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
        @Id
        @Column("id_order_item")
        private Long id;

        @Column("id_order")
        private Long orderId; // FK hacia orders

        @Column("id_product")
        private Long productId; // FK hacia products

        private Integer quantity;

        @Column("unit_price")
        private BigDecimal unitPrice;

        private BigDecimal subtotal;
    }
