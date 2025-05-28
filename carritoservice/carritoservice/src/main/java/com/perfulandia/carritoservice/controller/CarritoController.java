package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.model.Carrito; // Importa la clase Carrito
import com.perfulandia.carritoservice.service.CarritoService; // Importa el CarritoService
import org.springframework.http.HttpStatus; // Importa HttpStatus para códigos de respuesta HTTP
import org.springframework.http.ResponseEntity; // Importa ResponseEntity para construir respuestas HTTP
import org.springframework.web.bind.annotation.*; // Importa todas las anotaciones de Spring Web

import java.util.List; // Importa List para colecciones

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/carritos") // Define la ruta base para todos los endpoints en este controlador
public class CarritoController {

    private final CarritoService carritoService; // Inyecta el servicio de carrito

    // Constructor para inyección de dependencias del CarritoService
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // Endpoint para listar todos los carritos (GET /api/carritos)
    @GetMapping
    public ResponseEntity<List<Carrito>> listarTodosLosCarritos() {
        List<Carrito> carritos = carritoService.listarTodosLosCarritos(); // Llama al servicio para obtener la lista
        return new ResponseEntity<>(carritos, HttpStatus.OK); // Retorna la lista con estado 200 OK
    }

    // Endpoint para buscar un carrito por ID (GET /api/carritos/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Carrito> buscarCarritoPorId(@PathVariable Long id) {
        // Llama al servicio para buscar el carrito. Usa .map para devolver OK si existe, o NOT_FOUND si no.
        return carritoService.buscarCarritoPorId(id)
                .map(carrito -> new ResponseEntity<>(carrito, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para crear un nuevo carrito para un usuario (POST /api/carritos/{usuarioId})
    @PostMapping("/{usuarioId}")
    public ResponseEntity<Carrito> crearCarritoParaUsuario(@PathVariable Long usuarioId) {
        Carrito nuevoCarrito = carritoService.crearNuevoCarrito(usuarioId); // Llama al servicio para crear
        return new ResponseEntity<>(nuevoCarrito, HttpStatus.CREATED); // Retorna el carrito creado con estado 201 CREATED
    }

    // Endpoint para agregar un producto al carrito (POST /api/carritos/{carritoId}/items)
    // Los parámetros productoId y cantidad se pasan como RequestParams
    @PostMapping("/{carritoId}/items")
    public ResponseEntity<Carrito> agregarProductoAlCarrito(
            @PathVariable Long carritoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
        try {
            Carrito carritoActualizado = carritoService.agregarProductoAlCarrito(carritoId, productoId, cantidad);
            System.out.println(carritoActualizado);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK); // Retorna el carrito actualizado
        } catch (RuntimeException e) {
            // Manejo de errores básico: si algo falla (carrito no encontrado, stock, etc.), devuelve 400 BAD_REQUEST
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint para eliminar una cantidad específica de un producto del carrito (DELETE /api/carritos/{carritoId}/items)
    // Los parámetros productoId y cantidad se pasan como RequestParams
    @DeleteMapping("/{carritoId}/items")
    public ResponseEntity<Carrito> eliminarProductoDelCarrito(
            @PathVariable Long carritoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
            Carrito carritoActualizado = carritoService.eliminarProductoDelCarrito(carritoId, productoId, cantidad);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK); // Retorna el carrito actualizado

    }

    // Endpoint para eliminar completamente un producto del carrito (DELETE /api/carritos/{carritoId}/productos/{productoId})
    // Se elimina el producto sin importar la cantidad
    @DeleteMapping("/{carritoId}/productos/{productoId}")
    public ResponseEntity<Carrito> eliminarProductoCompletoDelCarrito(
            @PathVariable Long carritoId,
            @PathVariable Long productoId) { // Renombrado de 'productId' a 'productoId'
        try {
            Carrito carritoActualizado = carritoService.eliminarProductoCompletoDelCarrito(carritoId, productoId);
            return new ResponseEntity<>(carritoActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint para vaciar completamente un carrito (PUT /api/carritos/{carritoId}/vaciar)
    @PutMapping("/{carritoId}/vaciar")
    public ResponseEntity<Carrito> vaciarCarrito(@PathVariable Long carritoId) {
        try {
            Carrito carritoVacio = carritoService.vaciarCarrito(carritoId);
            return new ResponseEntity<>(carritoVacio, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint para eliminar un carrito completo por ID (DELETE /api/carritos/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarrito(@PathVariable Long id) {
        carritoService.eliminarCarrito(id); // Llama al servicio para eliminar
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Retorna estado 204 NO_CONTENT (éxito sin contenido de respuesta)
    }
}