package com.perfulandia.productservice.controller;

import com.perfulandia.productservice.model.Usuario;
import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.service.ProductoService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

//Nuevas importaciones DTO conexión al MS usuario, Para hacer peticiones HTTP a otros microservicios.
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Producto Service", description = "API para la gestión de productos")
public class ProductoController {

    private final ProductoService servicio;

    private final RestTemplate restTemplate;

    public ProductoController(ProductoService servicio,  RestTemplate restTemplate){
        this.servicio = servicio;
        this.restTemplate = restTemplate;
    }

    // Listar productos
    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public List<Producto> listar(){
        return servicio.listar();
    }

    // Guardar producto
    @Operation(summary = "Guardar un nuevo producto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto guardado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping
    public Producto guardar(@RequestBody Producto producto){
        return servicio.guardar(producto);
    }

    // Buscar producto por id
    @Operation(summary = "Buscar un producto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public Producto buscar(@PathVariable long id){
        return servicio.bucarPorId(id);
    }

    // Eliminar producto
    @Operation(summary = "Eliminar un producto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable long id){
        servicio.eliminar(id);
    }

    // Nuevo método: Obtener usuario
    @Operation(summary = "Obtener un usuario desde el microservicio de usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/usuario/{id}")
    public Usuario obtenerUsuario(@PathVariable long id){
        return restTemplate.getForObject("http://localhost:8081/api/usuarios/"+id,Usuario.class);
    }
}
