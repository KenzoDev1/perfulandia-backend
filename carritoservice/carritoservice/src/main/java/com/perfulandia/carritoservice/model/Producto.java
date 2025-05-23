package com.perfulandia.carritoservice.model;

import lombok.*;
//DTO Data Transfer Object= Objeto de transferencia de datos: para simular la respuesta de MS
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Producto {
    private long id;
    private String nombre;
    private double precio;
    private int stock;
}
