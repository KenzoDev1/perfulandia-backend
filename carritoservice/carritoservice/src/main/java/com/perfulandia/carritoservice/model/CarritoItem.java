package com.perfulandia.carritoservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    @EqualsAndHashCode.Exclude //Se excluyen estos metodos de esta columna por un asunto de recursion infinita de hashCode()
    @ToString.Exclude
    @JsonBackReference // Indicar que este es el lado "de atrás" de la relación
    private Carrito carrito;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @EqualsAndHashCode.Exclude
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
/*
 * Explicacion detallada de .Exclude :
 * El método hashCode() de Carrito intenta calcular el hash code de su colección items.
 * Para hacer esto, llama al método hashCode() de cada CarritoItem en la colección.
 * El método hashCode() de CarritoItem intenta calcular el hash code de su campo carrito.
 * Esto vuelve a llamar al método hashCode() de Carrito, creando un ciclo infinito (Carrito.hashCode() -> CarritoItem.hashCode() -> Carrito.hashCode() y así sucesivamente).
 * Esta recursión infinita consume toda la memoria de la pila (stack) y resulta en un StackOverflowError. */