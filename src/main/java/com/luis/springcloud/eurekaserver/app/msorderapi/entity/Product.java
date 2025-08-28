package com.luis.springcloud.eurekaserver.app.msorderapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * Entidad que representa un producto en el sistema.
 * Mapea la tabla `products` de la base de datos.
 * Campos:
 * - id: Identificador único del producto.
 * - name: Nombre del producto.
 * - price: Precio del producto.
 * - description: Descripción del producto.
 * - stock: Cantidad disponible en inventario.
 * - categoryId: Identificador de la categoría a la que pertenece el producto.
 *
 * @author Luis Ernesto Daza Firma
 * @version 1.0
 * @since 2024-06-15
 */
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @Column("id_product")
    private Long id;

    @Column("name_product")
    private String name;

    private BigDecimal price;

    @Column("description_producto")
    private String description;

    @Column("stock_product")
    private Integer stock;

    @Column("id_category")
    private Long categoryId;
}
