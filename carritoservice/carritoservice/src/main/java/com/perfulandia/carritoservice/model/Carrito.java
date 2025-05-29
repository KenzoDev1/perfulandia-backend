package com.perfulandia.carritoservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set; // Usar Set para la colección de ítems para evitar duplicados

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // Relación OneToMany con CarritoItem: Un Carrito puede tener muchos CarritoItems
    // mappedBy="carrito": Indica que el campo 'carrito' en CarritoItem es el propietario de la relación.
    // cascade=CascadeType.ALL: Las operaciones (persist, remove, etc.) se propagan a los CarritoItems.
    // orphanRemoval=true: Si un CarritoItem se desvincula de un Carrito, se elimina de la BD.
    // fetch = FetchType.LAZY: Los CarritoItems se cargan solo cuando se accede a ellos.
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CarritoItem> items = new HashSet<>(); //Es igual a una List pero sin duplicados y sin orden
    //HashSet es la opcion mas comun y eficiente para escenarios con un Set<>
}