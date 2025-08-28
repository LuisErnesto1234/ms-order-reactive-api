package com.luis.springcloud.eurekaserver.app.msorderapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * Entidad que representa una categoría en el sistema.
 *
 * <p>
 * Mapea la tabla `categories` en la base de datos.
 * </p>
 *
 * <ul>
 *   <li><b>id</b>: Identificador único de la categoría (PK).</li>
 *   <li><b>name</b>: Nombre de la categoría.</li>
 *   <li><b>products</b>: Conjunto de productos asociados a la categoría (relación uno a muchos).</li>
 * </ul>
 *
 * @author Luis Ernesto Daza Firma
 * @version 1.0
 * @since 2024-06-15
 */
@Table(name = "categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @Column("id_category")
    private Long id; //PK de la tabla categories

    @Column("name_category")
    private String name;

    @MappedCollection(idColumn = "id_category") //Relacion uno a muchos con products
    private Set<Product> products;
}
