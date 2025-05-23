package com.perfulandia.carritoservice.repository;

import com.perfulandia.carritoservice.model.*;
//2 Importar JPA Repository para trabajar con CRUD
import org.springframework.data.jpa.repository.JpaRepository;
//Interfaz hereda de JPA y gestiona la entidad usuario con ID Long
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

}
