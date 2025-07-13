package com.perfulandia.carritoservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "carritos", itemRelation = "carrito")
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonManagedReference
    private Set<CarritoItem> items = new HashSet<>();
}
/*
 * Explicacion detallada de .Exclude :
 * El método hashCode() de Carrito intenta calcular el hash code de su colección items.
 * Para hacer esto, llama al método hashCode() de cada CarritoItem en la colección.
 * El método hashCode() de CarritoItem intenta calcular el hash code de su campo carrito.
 * Esto vuelve a llamar al método hashCode() de Carrito, creando un ciclo infinito (Carrito.hashCode() -> CarritoItem.hashCode() -> Carrito.hashCode() y así sucesivamente).
 * Esta recursión infinita consume toda la memoria de la pila (stack) y resulta en un StackOverflowError. */