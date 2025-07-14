package com.perfulandia.productservice.service;

import com.perfulandia.productservice.exception.ResourceNotFoundException;
import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Halloween Man", 35000, 50);
    }

    @Test
    @DisplayName("Test 1 - Listar los productos")
    void listarTodosLosProductos() {
        List<Producto> productos = Arrays.asList(producto, new Producto(2L, "Halloween Man X", 40000, 30));
        when(productoRepository.findAll()).thenReturn(productos);

        List<Producto> resultado = productoService.listar();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 2 - Guardar nuevo producto")
    void guardarNuevoProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto resultado = productoService.guardar(producto);

        assertNotNull(resultado);
        assertEquals(producto.getNombre(), resultado.getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Test 3 - Buscar producto por id")
    void buscarPorId() {
        when(productoRepository.findById(producto.getId())).thenReturn(Optional.of(producto));

        Producto resultado = productoService.buscarPorId(producto.getId());

        assertNotNull(resultado);
        assertEquals(producto.getId(), resultado.getId());
        verify(productoRepository, times(1)).findById(producto.getId());
    }

    @Test
    @DisplayName("Test 4 - Buscar producto por id cuando producto NO existe")
    void buscarPorIdDeberiaRetornarNullCuandoNoExiste() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> productoService.buscarPorId(producto.getId()));

        assertTrue(exception.getMessage().contains("Producto no encontrado con ID: " + producto.getId()));
        verify(productoRepository, times(1)).findById(producto.getId());
    }

    @Test
    @DisplayName("Test 5 - Eliminar producto por id")
    void eliminarProductoPorId() {
        when(productoRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }
}