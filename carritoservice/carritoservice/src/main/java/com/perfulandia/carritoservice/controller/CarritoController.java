package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.model.CarritoItem;
import com.perfulandia.carritoservice.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // Endpoint para listar todos los carritos
    @GetMapping
    public ResponseEntity<List<Carrito>> listarTodosLosCarritos() {
        List<Carrito> carritos = carritoService.listarTodosLosCarritos();
        return new ResponseEntity<>(carritos, HttpStatus.OK);
    }

    // Endpoint para buscar un carrito por ID
    @GetMapping("/{id}")
    public ResponseEntity<Carrito> buscarCarritoPorId(@PathVariable Long id) {
        return carritoService.buscarCarritoPorId(id)
                .map(carrito -> new ResponseEntity<>(carrito, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para crear un nuevo carrito para un usuario
    @PostMapping("/usuario/{usuarioId}") // Nombre del método original: crearCarritoParaUsuario
    public ResponseEntity<Carrito> crearCarritoParaUsuario(@PathVariable Long usuarioId) {
        Carrito nuevoCarrito = carritoService.crearNuevoCarrito(usuarioId);
        return new ResponseEntity<>(nuevoCarrito, HttpStatus.CREATED);
    }

    // Endpoint para agregar un producto al carrito
    @PostMapping("/{carritoId}/items/{productoId}/{cantidad}")
    public CarritoItem agregarProductoAlCarrito(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            @PathVariable Integer cantidad) {
            CarritoItem itemAgregado = carritoService.agregarProductoAlCarrito(carritoId, productoId, cantidad);
            return itemAgregado;
    }

    // Endpoint para eliminar una cantidad específica de un producto del carrito
    @DeleteMapping("/{carritoId}/items")
    public ResponseEntity<Carrito> eliminarProductoDelCarrito(
            @PathVariable Long carritoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
        // Asumiendo que carritoService.eliminarProductoDelCarrito devuelve Carrito
        Carrito carritoActualizado = carritoService.eliminarProductoDelCarrito(carritoId, productoId, cantidad);
        return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
    }

    // Endpoint para eliminar completamente un producto del carrito
    @DeleteMapping("/{carritoId}/productos/{productoId}")
    public ResponseEntity<Carrito> eliminarProductoCompletoDelCarrito(
            @PathVariable Long carritoId,
            @PathVariable Long productoId) {
        try {
            // Asumiendo que carritoService.eliminarProductoCompletoDelCarrito devuelve Carrito
            Carrito carritoActualizado = carritoService.eliminarProductoCompletoDelCarrito(carritoId, productoId);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (RuntimeException e) { // Por ejemplo, si el item o el carrito no se encuentran
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // O NOT_FOUND
        }
    }

    // Endpoint para vaciar completamente un carrito
    @PutMapping("/{carritoId}/vaciar")
    public ResponseEntity<Carrito> vaciarCarrito(@PathVariable Long carritoId) {
        try {
            // Asumiendo que carritoService.vaciarCarrito devuelve Carrito
            Carrito carritoVacio = carritoService.vaciarCarrito(carritoId);
            return new ResponseEntity<>(carritoVacio, HttpStatus.OK);
        } catch (RuntimeException e) { // Carrito no encontrado
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint para eliminar un carrito completo por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarrito(@PathVariable Long id) {
        // Asumiendo que carritoService.eliminarCarrito no devuelve nada o no nos importa el resultado aquí
        // y que lanza una excepción si el carrito no se encuentra (que no estás capturando aquí).
        // Si el servicio no puede encontrar el carrito, podría ser bueno manejar esa excepción.
        try {
            carritoService.eliminarCarrito(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) { // Ejemplo: org.springframework.dao.EmptyResultDataAccessException si usas deleteById y no existe
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}