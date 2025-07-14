package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.exception.ResourceNotFoundException;
import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public CarritoService(CarritoRepository carritoRepository, CarritoItemRepository carritoItemRepository, RestTemplate restTemplate) {
        this.carritoRepository = carritoRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.restTemplate = restTemplate;
    }

    public List<Carrito> listarTodosLosCarritos() {
        return carritoRepository.findAll();
    }

    public Carrito buscarCarritoPorId(Long id) {
        return carritoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminarCarrito(Long id) {
        carritoRepository.deleteById(id);
    }
    private Optional<Usuario> obtenerDetallesUsuarioDesdeMS(Long usuarioId) {
        try {
            Usuario usuario = restTemplate.getForObject("http://localhost:8081/api/usuarios/" + usuarioId, Usuario.class);
            return Optional.ofNullable(usuario);
        } catch (HttpClientErrorException e) {
            // Verifica si el código de estado es 404 (Not Found)
            if (e.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el usuario con ID " + usuarioId + " desde UsuarioService. Error: " + e.getMessage());
        }
    }

    private Optional<Producto> obtenerDetallesProductoDesdeMS(Long productoId) {
            try {
                Producto producto = restTemplate.getForObject("http://localhost:8082/api/productos/" + productoId, Producto.class);
                return Optional.ofNullable(producto);
            } catch (HttpClientErrorException e) {
                // Verifica si el código de estado es 404 (Not Found)
                if (e.getStatusCode().is4xxClientError()) {
                    return Optional.empty();
                }
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("No se pudo obtener el producto con ID " + productoId + " desde ProductoService. Error: " + e.getMessage());
            }
    }

    @Transactional
    public Carrito crearNuevoCarrito(Long usuarioId) {
        // 1. Validar que el usuario exista en el microservicio de usuarios
        Optional<Usuario> usuario = obtenerDetallesUsuarioDesdeMS(usuarioId);
        if (usuario.isEmpty()) {
            throw new RuntimeException("No se puede crear un carrito para el usuario con ID " + usuarioId + " porque no existe.");
        }

        // 2. Si el usuario existe, proceder a crear el carrito
        Carrito carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        return carritoRepository.save(carrito);
    }

    @Transactional
    public CarritoItem agregarProductoAlCarrito(Long carritoId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor que cero.");
        }

        // Obtener detalles del producto para validar stock
        Optional<Producto> productoOptional = obtenerDetallesProductoDesdeMS(productoId);
        Producto productoDetalles = productoOptional.orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        //Verifica que la cantidad sea menor al stock del producto
        if (productoDetalles.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + productoDetalles.getNombre() + ". Stock disponible: " + productoDetalles.getStock());
        }

        //.getItems: Obtiene todos los objetos de tipo carrito
        //.stream: es la tuberia donde se procesaran ciertas restricciones antes de dar un resultado
        //.filter(): es el filtro que se realizara en la tuberia, segun la restriccion dentro del parentesis
        //.findFirst(): la funcion se detendra apenas encuentre el primer elemento
        Optional<CarritoItem> CarritoItemOptional = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        CarritoItem carritoItemResultado;

        if (CarritoItemOptional.isPresent()) {
            CarritoItem carritoItem = CarritoItemOptional.get();
            carritoItem.setCantidad(carritoItem.getCantidad()+cantidad);
            carritoItemResultado = carritoItemRepository.save(carritoItem);
            carrito.getItems().add(carritoItemResultado);
        } else {
            CarritoItem carritoItem = CarritoItem.builder()
                    .productoId(productoId)
                    .cantidad(cantidad)
                    .carrito(carrito)
                    .build();
            carrito.getItems().add(carritoItem);
            carritoItemResultado = carritoItemRepository.save(carritoItem);
            carrito.getItems().add(carritoItemResultado);
        }

        return carritoItemResultado;
    }

    @Transactional
    public Carrito eliminarProductoDelCarrito(Long carritoId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a eliminar debe ser mayor que cero.");
        }

        Optional<CarritoItem> existingItem = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (existingItem.isPresent()) {
            CarritoItem item = existingItem.get();
            if (item.getCantidad() <= cantidad) {
                carrito.getItems().remove(item);
                carritoItemRepository.delete(item);
            } else {
                item.setCantidad(item.getCantidad() - cantidad);
                carritoItemRepository.save(item);
            }
            return carritoRepository.save(carrito);
        } else {
            throw new RuntimeException("El producto con ID " + productoId + " no se encontró en el carrito.");
        }
    }

    @Transactional
    public Carrito eliminarProductoCompletoDelCarrito(Long carritoId, Long productId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        CarritoItem itemToRemove = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto con ID " + productId + " no encontrado en el carrito."));

        carrito.getItems().remove(itemToRemove);
        carritoItemRepository.delete(itemToRemove);

        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito vaciarCarrito(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        carrito.getItems().clear();
        return carritoRepository.save(carrito);
    }
}