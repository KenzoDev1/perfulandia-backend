package com.perfulandia.carritoservice.repository;

import com.perfulandia.carritoservice.model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository; // Importación específica

// Interfaz para operaciones CRUD básicas en la entidad CarritoItem
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    // Spring Data JPA ya proporciona los métodos findAll(), findById(), save(), deleteById(), etc.
}
