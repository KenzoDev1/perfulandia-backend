package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.assemblers.CarritoItemModelAssembler;
import com.perfulandia.carritoservice.assemblers.CarritoModelAssembler;
import com.perfulandia.carritoservice.exception.ResourceNotFoundException;
import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.service.CarritoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CarritoController.class, CarritoModelAssembler.class, CarritoItemModelAssembler.class})
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarritoService carritoService;

    @Test
    @DisplayName("Test 1 - GET /api/carritos/{id} debería devolver 404 si el carrito no se encuentra")
    void buscarCarritoPorId_CuandoNoExiste_DeberiaDevolverNotFound() throws Exception {
        given(carritoService.buscarCarritoPorId(anyLong()))
                .willThrow(new ResourceNotFoundException("Carrito no encontrado con ID: 99"));

        mockMvc.perform(get("/api/carritos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Carrito no encontrado con ID: 99"));
    }

    @Test
    @DisplayName("Test 2 - POST /api/carritos/usuario/{usuarioId} debería devolver 404 si el usuario no existe")
    void crearCarritoParaUsuario_CuandoUsuarioNoExiste_DeberiaDevolverNotFound() throws Exception {
        given(carritoService.crearNuevoCarrito(anyLong()))
                .willThrow(new ResourceNotFoundException("Usuario no encontrado con ID: 99"));

        mockMvc.perform(post("/api/carritos/usuario/{usuarioId}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuario no encontrado con ID: 99"));
    }

    @Test
    @DisplayName("Test 3 - POST /api/carritos/{carritoId}/items debería devolver 400 si la cantidad es inválida")
    void agregarProductoAlCarrito_CuandoCantidadEsInvalida_DeberiaDevolverBadRequest() throws Exception {
        given(carritoService.agregarProductoAlCarrito(anyLong(), anyLong(), anyInt()))
                .willThrow(new IllegalArgumentException("La cantidad debe ser un número positivo."));

        mockMvc.perform(post("/api/carritos/{carritoId}/items", 1L)
                        .param("productoId", "1")
                        .param("cantidad", "0")) // Cantidad inválida
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La cantidad debe ser un número positivo."));
    }

    @Test
    @DisplayName("Test 4 - DELETE /api/carritos/{id} debería devolver 404 si el carrito a eliminar no existe")
    void eliminarCarrito_CuandoNoExiste_DeberiaDevolverNotFound() throws Exception {
        // Simula que el método `void` del servicio lanza una excepción
        doThrow(new ResourceNotFoundException("No se puede eliminar. Carrito no encontrado con ID: 99"))
                .when(carritoService).eliminarCarrito(anyLong());

        mockMvc.perform(delete("/api/carritos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se puede eliminar. Carrito no encontrado con ID: 99"));
    }
}