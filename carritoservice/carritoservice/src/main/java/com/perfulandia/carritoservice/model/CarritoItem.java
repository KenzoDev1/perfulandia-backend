package com.perfulandia.carritoservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación ManyToOne con Carrito: Muchos CarritoItems pertenecen a un Carrito
    // FetchType.LAZY: El objeto Carrito asociado se carga solo cuando se accede a él por primera vez.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}