package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.DomainEvents;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Inicializa los mocks y @InjectMocks
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
    private CarritoItem carritoItem;
    private Producto producto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Inicializar objetos para las pruebas
        usuario = new Usuario(1L, "Ignacio", "car.bittner@duocuc.cl", "USUARIO");
        producto = new Producto(1L, "Tomate", 1000, 10);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuarioId(usuario.getId());
        carrito.setItems(new HashSet<>());

        carritoItem = CarritoItem.builder()
                .id(1L)
                .carrito(carrito)
                .productoId(producto.getId())
                .cantidad(2)
                .build();
    }

    @Test
    @DisplayName("Test 1 - Listar todos los carritos")
    void listarTodosLosCarritos() {
        // Se crea una lista de carritos ya inicializada con 2 elementos dentro, que seria carrito y un nuevo carrito vacio
        List<Carrito> carritos = Arrays.asList(carrito, new Carrito());

        // Cuando (when(...)) se ocupe el metodo .findAll(),
        // retornara (.thenReturn(...)) el resultado definido en el parametro (carritos)
        when(carritoRepository.findAll()).thenReturn(carritos);

        // Se genera un resultado del metodo utilizado en service segun la simulacion
        List<Carrito> resultado = carritoService.listarTodosLosCarritos();

        // Se asegura que resultado no sea nulo
        assertNotNull(resultado);

        // Se asegura que el resultado (.thenReturn(carritos) = 2) sea igual a lo esperado (Parametro 1 = 2)
        assertEquals(2, resultado.size());

        // Se asegura que el metodo .findAll() de carritoRepository fue invocado un numero esperado de veces (1)
        verify(carritoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 2 - Buscar carrito por id cuando carrito existe")
    void buscarCarritoPorIdExistente() {
        // Cuando (when(...)) se ocupe el metodo .findById(1L),
        // retornara (.thenReturn(...)) el resultado definido en el parametro (Optional.of(carrito))
        // Optional.of(carrito) : Envuelve el objeto carrito en un Optional, osea que carrito esta dentro de un Optional
        // con esto podemos usar los metodos de Optional como .isPresent()
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        // Se genera un resultado del metodo utilizado en service segun la simulacion
        Optional<Carrito> result = carritoService.buscarCarritoPorId(1L);

        // Se asegura que el resultado sea True, .isPresent() verifica que el resultado no sea null
        assertTrue(result.isPresent());

        // Se asegura que el resultado (.thenReturn(carritos) = 1L) sea igual a lo esperado (Parametro 1 = 1L)
        // se escribe .get().getId() ya que carrito esta envuelto en Optional y se tiene que indexar como en una List
        assertEquals(carrito.getId(), result.get().getId());

        // Se asegura que el metodo .findById(1L) de carritoRepository fue invocado un numero esperado de veces (1)
        verify(carritoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test 3 - Buscar carrito por id cuando carrito NO existe") // Excepcion!!
    void buscarCarritoPorIdNoExistente() {
        // Cuando (when(...)) se ocupe el metodo .findById(anyLong()),
        // retornara (.thenReturn(...)) el resultado definido en el parametro (Optional.empty())
        when(carritoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Se genera un resultado del metodo utilizado en service segun la simulacion
        Optional<Carrito> result = carritoService.buscarCarritoPorId(99L);

        // Se asegura que el resultado sea False, .isPresent() verifica que el resultado no sea null
        assertFalse(result.isPresent());

        // Se asegura que el metodo .findById(99L) de carritoRepository fue invocado un numero esperado de veces (1)
        verify(carritoRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Test 4 - Eliminar carrito")
    void eliminarCarrito() {
        // No se necesita simular un retorno para un método void

        // Se llama al método del servicio
        carritoService.eliminarCarrito(1L);

        // Se asegura que el metodo .deleteById(1L) de carritoRepository fue invocado un numero esperado de veces (1)
        verify(carritoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Test 5 - Crear nuevo carrito cuando usuario existe")
    void crearNuevoCarritoUsuarioExistente() {
        // Simular que el usuario existe en UsuarioService
        /*
            Como funciona restTemplate y getForObject:

            - RestTemplate: es parte de la dependencia Spring Web, es una clase que contiene
            metodos para comunicarse atraves de las APIs

            - .getForObject(argumento1(URL), argumento2(Tipo de clase)): indica que obtendras un objeto Java
            atraves de un GET desde una URL de una API indicada en el argumento 1,
            que esta se mapeara a un objeto Java que indiques en el argumento 2
         */
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/" + usuario.getId(), Usuario.class))
                .thenReturn(usuario);

        // Simular que el carrito se guarda exitosamente
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        // Se genera un resultado del metodo utilizado en service segun la simulacion
        Carrito nuevoCarrito = carritoService.crearNuevoCarrito(usuario.getId());

        // Se asegura que resultado no sea nulo
        assertNotNull(nuevoCarrito);

        // Se asegura que el resultado (nuevoCarrrito.getUsuarioId() = 1L) sea igual a lo esperado (Parametro 1 = 1L)
        assertEquals(usuario.getId(), nuevoCarrito.getUsuarioId());

        // Se asegura que el metodo .getForObject(anyString(), eq(Usuario.class))
        // de restTemplate fue invocado un numero esperado de veces (1)
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Usuario.class));

        // Se asegura que el metodo .save(any(Carrito.class))
        // de restTemplate fue invocado un numero esperado de veces (1)
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    @DisplayName("Test 6 - Crear nuevo carrito cuando usuario NO existe") // Excepcion!!
    void crearNuevoCarritoUsuarioNoExistente() {
        // Simular que el usuario NO existe (lanza excepcion 404 Not Found)
        when(restTemplate.getForObject("http://localhost:8081/api/usuarios/" + usuario.getId(), Usuario.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        /*
        assertThrows(RuntimeException.class, () -> {...}); Es una asercion de JUnit 5
        que se utiliza especificamente para probar si un bloque de codigo lanza una excepcion esperada

        1er argumento: Es el tipo de excepcion que se espera que se lance (RuntimeException.class)

        2do argumento: Es una expresion Lambda ( () -> {...} ) que ejecuta el codigo dentro de su bloque,
        de esta manera verefica si lanza una excepcion
        */
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.crearNuevoCarrito(usuario.getId());
        });

        // Se asegura que el la excepcion sea el mismo mensaje que esta en .contains()
        assertTrue(exception.getMessage().contains(
                "No se puede crear un carrito para el usuario con ID " + usuario.getId() + " porque no existe."
                ));

        // Se asegura que que el metodo.getForObject()
        // de restTemplate se invoco una sola vez y se mapeo como una clase Usuario
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Usuario.class));

        //Se asegura que el metodo .save(any(Carrito.class) de carritoRepository nunca fue llamado
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    /*
    * Agrega producto al carrito, se escribio "cuando producto NO existente",
    * porque segun la logica de negocio en el service, cuando producto ya existe
    * se actualiza la cantidad segun la cantidad ingresada, por eso esta aclaracion
    * */

    @Test
    @DisplayName("Test 7 - Agregar producto carrito cuando producto NO es existente")
    void agregarProductoNoExistenteAlCarrito() {

        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));

        when(restTemplate.getForObject("http://localhost:8082/api/productos/" + producto.getId(), Producto.class))
                .thenReturn(producto);

        when(carritoItemRepository.save(any(CarritoItem.class))).thenReturn(carritoItem);

        CarritoItem resultItem = carritoService.agregarProductoAlCarrito(carrito.getId(), producto.getId(), 2);

        assertNotNull(resultItem);

        assertEquals(producto.getId(), resultItem.getProductoId());

        assertEquals(2, resultItem.getCantidad());

        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Producto.class));
        verify(carritoItemRepository, times(1)).save(any(CarritoItem.class));
    }

    /*
        Desde aqui decidi no poner comentarios ya que desde aqui, para no generar codigo boilerplate (repetitivo),
        se repite la misma estructura :
        1.- Preparacion 2.- Ejecucion y 3.- Asercion
     */


    @Test
    @DisplayName("Test 8 - Actualizar cantidad del producto porque existe")
    void agregarProductoExistenteAlCarritoActualizarCantidad() {

        carrito.getItems().add(carritoItem);

        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));

        when(restTemplate.getForObject("http://localhost:8082/api/productos/" + producto.getId(), Producto.class))
                .thenReturn(producto);

        when(carritoItemRepository.save(any(CarritoItem.class))).thenReturn(carritoItem);

        CarritoItem resultItem = carritoService.agregarProductoAlCarrito(carrito.getId(), producto.getId(), 3);

        assertNotNull(resultItem);
        assertEquals(producto.getId(), resultItem.getProductoId());
        assertEquals(5, resultItem.getCantidad()); // Cantidad inicial (2) + nueva cantidad (3) = 5
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Producto.class));
        verify(carritoItemRepository, times(1)).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Test 9 - Agregar producto al carrito NO existente") // Excepcion!!
    void agregarProductoAlCarritoNoExistente() {
        when(carritoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.agregarProductoAlCarrito(99L, producto.getId(), 1);
        });

        assertTrue(exception.getMessage().contains("Carrito no encontrado"));
        verify(carritoRepository, times(1)).findById(anyLong());
        verify(restTemplate, never()).getForObject(anyString(), any(Class.class));
        verify(carritoItemRepository, never()).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Test 10 - Agregar producto al carrito cuando producto NO existe") // Agrega un producto al carrito
    void agregarProductoAlCarritoCuandoProductoNoExiste() {
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject("http://localhost:8082/api/productos/" + producto.getId(), Producto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.agregarProductoAlCarrito(carrito.getId(), producto.getId(), 1);
        });

        assertTrue(exception.getMessage().contains("Producto no encontrado con ID: " + producto.getId()));
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Producto.class));
        verify(carritoItemRepository, never()).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Test 11 - Agregar producto al carrito cuando la cantidad del producto supera al stock") // Excepcion!!
    void agregarProductoAlCarritoStockInsuficiente() {
        // Simular que el producto tiene menos stock que la cantidad solicitada
        producto.setStock(1);
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject("http://localhost:8082/api/productos/" + producto.getId(), Producto.class))
                .thenReturn(producto);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.agregarProductoAlCarrito(carrito.getId(), producto.getId(), 2);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Producto.class));
        verify(carritoItemRepository, never()).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Test 12 - Eliminar producto del carrito, si la cantidad al eliminar es mayor o igual")
    void eliminarProductoDelCarritoEliminarItemCompletoSiCantidadEsMayorOIgual() {
        carrito.getItems().add(carritoItem);
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Carrito resultCarrito = carritoService.eliminarProductoDelCarrito(carrito.getId(), producto.getId(), 2);

        assertNotNull(resultCarrito);
        assertTrue(resultCarrito.getItems().isEmpty());
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(carritoItemRepository, times(1)).delete(carritoItem);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    @DisplayName("Test 13 - Eliminar producto del carrito, deberia reducir la cantidad unicamente si la cantidad es menor")
    void eliminarProductoDelCarrito_deberiaReducirCantidadSiEsMenor() {
        carrito.getItems().add(carritoItem);
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(carritoItemRepository.save(any(CarritoItem.class))).thenReturn(carritoItem);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Carrito resultCarrito = carritoService.eliminarProductoDelCarrito(carrito.getId(), producto.getId(), 1);

        assertNotNull(resultCarrito);
        assertEquals(1, resultCarrito.getItems().size());
        assertTrue(resultCarrito.getItems().contains(carritoItem));
        assertEquals(1, carritoItem.getCantidad());
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(carritoItemRepository, times(1)).save(carritoItem);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
        verify(carritoItemRepository, never()).delete(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Test 14 - Eliminar producto del carrito por completo")
    void eliminarProductoCompletoDelCarrito() {
        carrito.getItems().add(carritoItem);
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Carrito resultCarrito = carritoService.eliminarProductoCompletoDelCarrito(carrito.getId(), producto.getId());

        assertNotNull(resultCarrito);
        assertTrue(resultCarrito.getItems().isEmpty());
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(carritoItemRepository, times(1)).delete(carritoItem);
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    @DisplayName("Test 15 - Eliminar producto del carrito por completo, cuando el producto no fue encontrado")
    void eliminarProductoCompletoDelCarritoProductoNoEncontradoEnCarrito() {
        when
                (carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.eliminarProductoCompletoDelCarrito(carrito.getId(), 999L);
        });

        assertTrue(exception.getMessage().contains("Producto con ID 999 no encontrado en el carrito."));
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(carritoItemRepository, never()).delete(any(CarritoItem.class));
        verify(carritoRepository, never()).save(any(Carrito.class));
    }


    @Test
    @DisplayName("Test 16 - Vaciar carrito")
    void vaciarCarrito() {
        carrito.getItems().add(carritoItem);
        when(carritoRepository.findById(carrito.getId())).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        Carrito resultCarrito = carritoService.vaciarCarrito(carrito.getId());

        assertNotNull(resultCarrito);
        assertTrue(resultCarrito.getItems().isEmpty());
        verify(carritoRepository, times(1)).findById(carrito.getId());
        verify(carritoRepository, times(1)).save(any(Carrito.class));

        // No se verifica carritoItemRepository.delete() aquí porque clear() opera en la colección,
        // y orphanRemoval=true en @OneToMany es lo que se encarga de las eliminaciones en cascada
        // cuando persistes el carrito vacío.
    }

    @Test
    @DisplayName("Test 17 - Vaciar carrito, cuando el carrito no ha sido encontrado")
    void vaciarCarrito_deberiaLanzarExcepcionSiCarritoNoEncontrado() {
        when(carritoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.vaciarCarrito(99L);
        });

        assertTrue(exception.getMessage().contains("Carrito no encontrado con ID: 99"));
        verify(carritoRepository, times(1)).findById(anyLong());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }
}