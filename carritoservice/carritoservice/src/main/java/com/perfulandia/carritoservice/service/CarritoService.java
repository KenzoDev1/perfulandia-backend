package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.exception.ResourceNotFoundException;
import com.perfulandia.carritoservice.model.*;
import com.perfulandia.carritoservice.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        return carritoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado con ID: " + id));
    }

    @Transactional
    public void eliminarCarrito(Long id) {
        if (!carritoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Carrito no encontrado con ID: " + id);
        }
        carritoRepository.deleteById(id);
    }

    private Usuario obtenerDetallesUsuarioDesdeMS(Long usuarioId) {
        try {
            return restTemplate.getForObject("http://localhost:8081/api/usuarios/" + usuarioId, Usuario.class);
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND){
                throw new ResourceNotFoundException("No se puede operar con el usuario ID " + usuarioId + " porque no existe en el microservicio de usuarios.");
            }
            throw new RuntimeException("Error al comunicarse con el servicio de usuarios: " + e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio de usuarios: " + e.getMessage());
        }
    }

    private Producto obtenerDetallesProductoDesdeMS(Long productoId) {
        try {
            return restTemplate.getForObject("http://localhost:8082/api/productos/" + productoId, Producto.class);
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode() == HttpStatus.NOT_FOUND){
                throw new ResourceNotFoundException("Producto no encontrado con ID: " + productoId);
            }
            throw new RuntimeException("Error al comunicarse con el servicio de productos: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio de productos: " + e.getMessage());
        }
    }

    @Transactional
    public Carrito crearNuevoCarrito(Long usuarioId) {
        obtenerDetallesUsuarioDesdeMS(usuarioId);

        Carrito carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        return carritoRepository.save(carrito);
    }

    @Transactional
    public CarritoItem agregarProductoAlCarrito(Long carritoId, Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser un número positivo.");
        }

        Carrito carrito = buscarCarritoPorId(carritoId);
        Producto productoDetalles = obtenerDetallesProductoDesdeMS(productoId);

        if (productoDetalles.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + productoDetalles.getNombre() + ". Stock disponible: " + productoDetalles.getStock());
        }

        Optional<CarritoItem> carritoItemOptional = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        CarritoItem carritoItemResultado;
        if (carritoItemOptional.isPresent()) {
            CarritoItem itemExistente = carritoItemOptional.get();
            itemExistente.setCantidad(itemExistente.getCantidad() + cantidad);
            carritoItemResultado = carritoItemRepository.save(itemExistente);
        } else {
            CarritoItem nuevoItem = CarritoItem.builder()
                    .productoId(productoId)
                    .cantidad(cantidad)
                    .carrito(carrito)
                    .build();
            carrito.getItems().add(nuevoItem);
            carritoItemResultado = carritoItemRepository.save(nuevoItem);
        }
        return carritoItemResultado;
    }

    @Transactional
    public Carrito eliminarProductoDelCarrito(Long carritoId, Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a eliminar debe ser mayor que cero.");
        }

        Carrito carrito = buscarCarritoPorId(carritoId);

        CarritoItem item = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado en el carrito."));

        if (item.getCantidad() <= cantidad) {
            carrito.getItems().remove(item);
            carritoItemRepository.delete(item);
        } else {
            item.setCantidad(item.getCantidad() - cantidad);
            carritoItemRepository.save(item);
        }
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito eliminarProductoCompletoDelCarrito(Long carritoId, Long productoId) {
        Carrito carrito = buscarCarritoPorId(carritoId);

        CarritoItem itemToRemove = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado en el carrito."));

        carrito.getItems().remove(itemToRemove);
        carritoItemRepository.delete(itemToRemove);

        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito vaciarCarrito(Long carritoId) {
        Carrito carrito = buscarCarritoPorId(carritoId);
        carrito.getItems().clear();
        return carritoRepository.save(carrito);
    }
}