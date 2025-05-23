package com.perfulandia.carritoservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder//Generar constructores de manera mas flexible
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private List<Producto> productos;

    public Carrito() {
        productos = new List<>();
    }
}
