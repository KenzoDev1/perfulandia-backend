package com.perfulandia.productservice.controller;

import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.model.Usuario;
import com.perfulandia.productservice.service.ProductoService;
import com.perfulandia.productservice.assemblers.ProductoModelAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Producto Service", description = "API para la gestión de productos")
public class ProductoController {

    private final ProductoService servicio;
    private final ProductoModelAssembler assembler;
    private final RestTemplate restTemplate;

    public ProductoController(ProductoService servicio, ProductoModelAssembler assembler, RestTemplate restTemplate) {
        this.servicio = servicio;
        this.assembler = assembler;
        this.restTemplate = restTemplate;
    }

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public CollectionModel<EntityModel<Producto>> listar() {
        List<EntityModel<Producto>> productos = servicio.listar().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(productos, linkTo(methodOn(ProductoController.class).listar()).withSelfRel());
    }

    @Operation(summary = "Guardar un nuevo producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto guardado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Producto producto) {
        EntityModel<Producto> entityModel = assembler.toModel(servicio.guardar(producto));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @Operation(summary = "Buscar un producto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public EntityModel<Producto> buscar(@PathVariable long id) {
        Producto producto = servicio.bucarPorId(id);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        return assembler.toModel(producto);
    }

    @Operation(summary = "Eliminar un producto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Este endpoint es un cliente de otro servicio, por lo que no se le aplica HATEOAS directamente.
    // Devuelve el DTO tal cual lo recibe.
    @Operation(summary = "Obtener un usuario desde el microservicio de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/usuario/{id}")
    public Usuario obtenerUsuario(@PathVariable long id) {
        return restTemplate.getForObject("http://localhost:8081/api/usuarios/" + id, Usuario.class);
    }
}