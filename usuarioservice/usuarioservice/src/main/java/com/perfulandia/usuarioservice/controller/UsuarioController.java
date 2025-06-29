package com.perfulandia.usuarioservice.controller;

import com.perfulandia.usuarioservice.model.Usuario;
import com.perfulandia.usuarioservice.service.UsuarioService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuario Service", description = "API para la gestión de usuarios")
public class UsuarioController {
    private final UsuarioService service;
    //Constructor para poder consumir la interfaz
    public UsuarioController(UsuarioService service){
        this.service=service;
    }

    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    public List<Usuario> listar(){
        return service.listar();
    }

    @Operation(summary = "Guardar un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario guardado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping
    public Usuario guardar(@RequestBody Usuario usuario){
        return service.guardar(usuario);
    }

    @Operation(summary = "Buscar un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public Usuario buscar(@PathVariable long id){
        return service.buscar(id);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable long id){
        service.eliminar(id);
    }
}
