package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.exception.ResourceNotFoundException;
import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarritoServiceTest {
    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarritoService carritoService;

    private Carrito carrito;
    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "Carlos Bittner", "car.bittner@duocuc.cl", "CLIENTE");
        producto = new Producto(1L, "Halloween Man", 35000, 30);
        carrito = new Carrito(1L, usuario.getId(), new HashSet<>());
    }

    @Test
    @DisplayName("Test 1 - Debería lanzar excepción al buscar un carrito que NO existe")
    void buscarCarritoPorIdCuandoNoExisteLanzaExcepcion() {
        when(carritoRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carritoService.buscarCarritoPorId(99L);
        });

        assertEquals("Carrito no encontrado con ID: 99", exception.getMessage());
        verify(carritoRepository).findById(99L);
    }

    @Test
    @DisplayName("Test 2 - Debería lanzar excepción al eliminar un carrito que NO existe")
    void eliminarCarritoCuandoNoExisteLanzaExcepcion() {
        when(carritoRepository.existsById(anyLong())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carritoService.eliminarCarrito(99L);
        });

        assertEquals("No se puede eliminar. Carrito no encontrado con ID: 99", exception.getMessage());
        verify(carritoRepository).existsById(99L);
        verify(carritoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Test 3 - Debería lanzar excepción al crear carrito si el usuario NO existe")
    void crearNuevoCarritoCuandoUsuarioNoExisteLanzaExcepcion() {
        String url = "http://localhost:8081/api/usuarios/" + usuario.getId();
        when(restTemplate.getForObject(url, Usuario.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carritoService.crearNuevoCarrito(usuario.getId());
        });

        assertTrue(exception.getMessage().contains(
                "No se puede operar con el usuario ID " + usuario.getId() +
                        " porque no existe en el microservicio de usuarios."));
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    @DisplayName("Test 4 - Debería lanzar excepción al agregar producto a un carrito que NO existe")
    void agregarProductoAlCarritoCuandoCarritoNoExisteLanzaExcepcion() {
        when(carritoRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carritoService.agregarProductoAlCarrito(99L, producto.getId(), 1);
        });

        assertEquals("Carrito no encontrado con ID: 99", exception.getMessage());
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    @DisplayName("Test 5 - Debería lanzar excepción al agregar un producto que NO existe")
    void agregarProductoAlCarritoCuandoProductoNoExisteLanzaExcepcion() {
        String url = "http://localhost:8082/api/productos/" + producto.getId();
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(url, Producto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            carritoService.agregarProductoAlCarrito(carrito.getId(), producto.getId(), 1);
        });

        assertEquals("Producto no encontrado con ID: " + producto.getId(), exception.getMessage());
    }

    @Test
    @DisplayName("Test 6 - Debería lanzar excepción si la cantidad a agregar es negativa o cero")
    void agregarProductoAlCarritoCuandoCantidadEsInvalidaLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            carritoService.agregarProductoAlCarrito(carrito.getId(), producto.getId(), 0);
        });

        assertEquals("La cantidad debe ser un número positivo.", exception.getMessage());
    }
}