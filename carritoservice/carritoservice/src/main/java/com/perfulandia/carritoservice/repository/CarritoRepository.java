package com.perfulandia.carritoservice.repository;

import com.perfulandia.carritoservice.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

// Interfaz para operaciones CRUD básicas en la entidad Carrito
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    // Spring Data JPA ya proporciona los métodos findAll(), findById(), save(), deleteById(), etc.
}