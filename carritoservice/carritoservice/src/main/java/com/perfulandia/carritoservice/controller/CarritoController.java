package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.service.CarritoService;
import com.perfulandia.carritoservice.assemblers.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carritos")
@Tag(name = "Carrito Service", description = "API para la gestión de carritos de compra")
public class CarritoController {

    private final CarritoService carritoService;
    private final CarritoModelAssembler carritoAssembler;
    private final CarritoItemModelAssembler itemAssembler;

    public CarritoController(CarritoService carritoService, CarritoModelAssembler carritoAssembler, CarritoItemModelAssembler itemAssembler) {
        this.carritoService = carritoService;
        this.carritoAssembler = carritoAssembler;
        this.itemAssembler = itemAssembler;
    }

    @Operation(summary = "Listar todos los carritos existentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de carritos obtenida exitosamente")
    })
    @GetMapping
    public CollectionModel<EntityModel<Carrito>> listarTodosLosCarritos() {
        List<EntityModel<Carrito>> carritos = carritoService.listarTodosLosCarritos().stream()
                .map(carritoAssembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(carritos, linkTo(methodOn(CarritoController.class).listarTodosLosCarritos()).withSelfRel());
    }

    @Operation(summary = "Buscar un carrito por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public EntityModel<Carrito> buscarCarritoPorId(@PathVariable Long id) {
        Carrito carrito = carritoService.buscarCarritoPorId(id);
        return carritoAssembler.toModel(carrito);
    }

    @Operation(summary = "Crear un nuevo carrito para un usuario específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carrito creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario inválido o usuario no existente")
    })
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> crearCarritoParaUsuario(@PathVariable Long usuarioId) {
        EntityModel<Carrito> entityModel = carritoAssembler.toModel(carritoService.crearNuevoCarrito(usuarioId));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }


    @Operation(summary = "Agregar un producto a un carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto agregado/actualizado en el carrito"),
            @ApiResponse(responseCode = "400", description = "Datos de producto o cantidad inválidos"),
            @ApiResponse(responseCode = "404", description = "Carrito o Producto no encontrado")
    })
    @PostMapping("/{carritoId}/items")
    public ResponseEntity<EntityModel<CarritoItem>> agregarProductoAlCarrito(
            @PathVariable Long carritoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
        CarritoItem itemAgregado = carritoService.agregarProductoAlCarrito(carritoId, productoId, cantidad);
        return ResponseEntity.ok(itemAssembler.toModel(itemAgregado));
    }

    @Operation(summary = "Eliminar una cantidad específica de un producto del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad del producto eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de producto o cantidad inválidos"),
            @ApiResponse(responseCode = "404", description = "Carrito o producto en el carrito no encontrado")
    })
    @DeleteMapping("/{carritoId}/items")
    public ResponseEntity<EntityModel<Carrito>> eliminarProductoDelCarrito(
            @PathVariable Long carritoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
        Carrito carritoActualizado = carritoService.eliminarProductoDelCarrito(carritoId, productoId, cantidad);
        return ResponseEntity.ok(carritoAssembler.toModel(carritoActualizado));
    }

    @Operation(summary = "Eliminar completamente un producto del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado completamente del carrito"),
            @ApiResponse(responseCode = "404", description = "Carrito o producto no encontrado")
    })
    @DeleteMapping("/{carritoId}/productos/{productoId}")
    public ResponseEntity<EntityModel<Carrito>> eliminarProductoCompletoDelCarrito(
            @PathVariable Long carritoId,
            @PathVariable Long productoId) {
        Carrito carritoActualizado = carritoService.eliminarProductoCompletoDelCarrito(carritoId, productoId);
        return ResponseEntity.ok(carritoAssembler.toModel(carritoActualizado));
    }


    @Operation(summary = "Vaciar completamente un carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito vaciado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
    })
    @PutMapping("/{carritoId}/vaciar")
    public ResponseEntity<EntityModel<Carrito>> vaciarCarrito(@PathVariable Long carritoId) {
        Carrito carritoVacio = carritoService.vaciarCarrito(carritoId);
        return ResponseEntity.ok(carritoAssembler.toModel(carritoVacio));
    }

    @Operation(summary = "Eliminar un carrito completo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado con el ID proporcionado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCarrito(@PathVariable Long id) {
        carritoService.eliminarCarrito(id);
        return ResponseEntity.noContent().build();
    }
}

// Link para meterse a swagger "http://localhost:8083/swagger-ui/index.html"