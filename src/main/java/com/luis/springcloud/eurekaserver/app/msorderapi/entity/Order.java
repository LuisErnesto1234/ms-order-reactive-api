package com.luis.springcloud.eurekaserver.app.msorderapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @Column("id_order")
    private Long id;

    @Column("order_date")
    private LocalDate orderDate;

    private String status;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;

    @MappedCollection(idColumn = "id_order")
    private Set<OrderItem> orderItems;
}
