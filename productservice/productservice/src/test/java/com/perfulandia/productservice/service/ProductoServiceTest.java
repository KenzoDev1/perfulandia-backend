package com.perfulandia.productservice.service;

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
        producto = new Producto(1L, "Perfume Prueba", 100, 50);
    }

    @Test
    @DisplayName("Test 1 - Listar los productos")
    void listarTodosLosProductos() {
        List<Producto> productos = Arrays.asList(producto, new Producto(2L, "Perfume B", 75, 30));
        when(productoRepository.findAll()).thenReturn(productos);

        List<Producto> result = productoService.listar();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 2 - Guardar nuevo producto")
    void guardarNuevoProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        Producto result = productoService.guardar(producto);

        assertNotNull(result);
        assertEquals(producto.getNombre(), result.getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Test 3 - Buscar producto por id")
    void bucarPorId() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto result = productoService.bucarPorId(1L);

        assertNotNull(result);
        assertEquals(producto.getId(), result.getId());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test 4 - Buscar producto por id, deberia retornar null cuando NO existe")
    void bucarPorIdDeberiaRetornarNullCuandoNoExiste() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Producto result = productoService.bucarPorId(99L);

        assertNull(result);
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Test 5 - Eliminar producto por id")
    void eliminarProductoPorId() {
        doNothing().when(productoRepository).deleteById(1L); // Para métodos void

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }
}