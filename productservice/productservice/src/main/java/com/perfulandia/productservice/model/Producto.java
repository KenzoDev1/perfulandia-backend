package com.perfulandia.productservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Relation(collectionRelation = "productos", itemRelation = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nombre;
    private double precio;
    private int stock;
}