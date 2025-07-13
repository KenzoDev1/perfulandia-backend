package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.assemblers.CarritoItemModelAssembler;
import com.perfulandia.carritoservice.assemblers.CarritoModelAssembler;
import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.service.CarritoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Anotacion para solicitar una prueba unitaria enfocada en la capa WEB (MVC)
@WebMvcTest({CarritoController.class, CarritoModelAssembler.class, CarritoItemModelAssembler.class})
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Anotacion que convierte a usuarioService en un "mock" objeto simulado
    /*
    Motivo de porque se ocupa @MockBean en vez de @MockitoBean es por
    la version de Spring Boot de este microservicio
     */
    @MockBean

    private CarritoService carritoService;

    private Carrito carrito;
    private CarritoItem carritoItem;

    @BeforeEach
    void setUp() {
        carrito = new Carrito(1L, 1L, new HashSet<>());
        carritoItem = new CarritoItem(1L, carrito, 1L, 1);
        carrito.getItems().add(carritoItem);
    }

    @Test
    @DisplayName("Test 1 - Listar todos los carritos")
    void listarTodosLosCarritosTest() throws Exception {
        List<Carrito> carritos = Arrays.asList(carrito);
        given(carritoService.listarTodosLosCarritos()).willReturn(carritos);

        mockMvc.perform(get("/api/carritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carritos", hasSize(1)))
                .andExpect(jsonPath("$._embedded.carritos[0].id", is(1)));
    }

    @Test
    @DisplayName("Test 2 - Buscar un carrito por ID")
    void buscarCarritoPorIdTest() throws Exception {
        given(carritoService.buscarCarritoPorId(1L)).willReturn(Optional.of(carrito));

        mockMvc.perform(get("/api/carritos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Test 3 - Debería devolver Not Found si el carrito no existe")
    void buscarCarritoPorIdNoExistenteTest() throws Exception {
        given(carritoService.buscarCarritoPorId(anyLong())).willThrow(new RuntimeException());

        mockMvc.perform(get("/api/carritos/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test 4 - Crear un nuevo carrito para un usuario")
    void crearCarritoParaUsuarioTest() throws Exception {
        Carrito carritoNuevo = new Carrito(2L, 1L, new HashSet<>());
        given(carritoService.crearNuevoCarrito(1L)).willReturn(carritoNuevo);

        mockMvc.perform(post("/api/carritos/usuario/{usuarioId}", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.usuarioId", is(1)));
    }

    @Test
    @DisplayName("Test 5 - agregar un producto al carrito")
    void agregarProductoAlCarritoTest() throws Exception {
        given(carritoService.agregarProductoAlCarrito(1L, 1L, 1)).willReturn(carritoItem);

        mockMvc.perform(post("/api/carritos/{carritoId}/items", 1L)
                        .param("productoId", "1")
                        .param("cantidad", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId", is(1)));
    }

    @Test
    @DisplayName("Test 6 - Eliminar una cantidad de un producto del carrito")
    void eliminarProductoDelCarritoTest() throws Exception {
        given(carritoService.eliminarProductoDelCarrito(1L, 1L, 1)).willReturn(carrito);

        mockMvc.perform(delete("/api/carritos/{carritoId}/items", 1L)
                        .param("productoId", "1")
                        .param("cantidad", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Test 7 - Eliminar un producto completo del carrito")
    void eliminarProductoCompletoDelCarritoTest() throws Exception {
        given(carritoService.eliminarProductoCompletoDelCarrito(1L, 1L)).willReturn(carrito);

        mockMvc.perform(delete("/api/carritos/{carritoId}/productos/{productoId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Test 8 - Vaciar un carrito")
    void vaciarCarritoTest() throws Exception {
        given(carritoService.vaciarCarrito(1L)).willReturn(new Carrito(1L, 1L, new HashSet<>()));

        mockMvc.perform(put("/api/carritos/{carritoId}/vaciar", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @DisplayName("Test 9 - liminar un carrito por completo")
    void eliminarCarritoTest() throws Exception {
        doNothing().when(carritoService).eliminarCarrito(1L);

        mockMvc.perform(delete("/api/carritos/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}