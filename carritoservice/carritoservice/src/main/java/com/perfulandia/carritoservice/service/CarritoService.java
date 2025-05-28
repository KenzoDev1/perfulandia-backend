package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.model.CarritoItem;
import com.perfulandia.carritoservice.model.Producto;
import com.perfulandia.carritoservice.model.Usuario; // Importa la clase Usuario
import com.perfulandia.carritoservice.repository.CarritoRepository;
import com.perfulandia.carritoservice.repository.CarritoItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException; // Importa para manejar errores HTTP
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final RestTemplate restTemplate; // Para comunicarse con ProductoService y UsuarioService

    @Autowired
    public CarritoService(CarritoRepository carritoRepository, CarritoItemRepository carritoItemRepository, RestTemplate restTemplate) {
        this.carritoRepository = carritoRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.restTemplate = restTemplate;
    }

    public List<Carrito> listarTodosLosCarritos() {
        return carritoRepository.findAll();
    }

    public Optional<Carrito> buscarCarritoPorId(Long id) {
        return carritoRepository.findById(id);
    }

    @Transactional
    public Carrito guardarCarrito(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    @Transactional
    public void eliminarCarrito(Long id) {
        carritoRepository.deleteById(id);
    }

    /**
     * Crea un nuevo carrito para un usuario dado, validando que el usuario exista.
     * @param usuarioId ID del usuario.
     * @return El carrito recién creado.
     * @throws RuntimeException si el usuario no es encontrado.
     */
    @Transactional
    public Carrito crearNuevoCarrito(Long usuarioId) {
        // 1. Validar que el usuario exista en el microservicio de usuarios
        Usuario usuario = obtenerDetallesUsuarioDesdeMS(usuarioId); // Llama al nuevo método de validación
        if (usuario == null) {
            throw new RuntimeException("No se puede crear un carrito para el usuario con ID " + usuarioId + " porque no existe.");
        }

        // 2. Si el usuario existe, proceder a crear el carrito
        Carrito carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        return carritoRepository.save(carrito);
    }

    /**
     * Obtiene los detalles de un producto desde el ProductoService.
     * @param productId ID del producto.
     * @return Objeto Producto (DTO) con sus detalles actualizados.
     * @throws RuntimeException si el producto no es encontrado.
     */
    private Producto obtenerDetallesProductoDesdeMS(Long productId) {
        try {
            // Asegúrate de que la URL sea correcta para tu ProductoService
            return restTemplate.getForObject("http://localhost:8082/api/productos/" + productId, Producto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null; // Retorna null si el producto no se encuentra (HTTP 404)
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el producto con ID " + productId + " desde ProductoService. Error: " + e.getMessage());
        }
    }

    /**
     * Obtiene los detalles de un usuario desde el UsuarioService.
     * Este es el nuevo método para validar el usuario.
     * @param usuarioId ID del usuario.
     * @return Objeto Usuario (DTO) con sus detalles, o null si no existe.
     * @throws RuntimeException si hay un error de comunicación con el UsuarioService.
     */
    private Usuario obtenerDetallesUsuarioDesdeMS(Long usuarioId) {
        try {
            // Asegúrate de que la URL sea correcta para tu UsuarioService
            return restTemplate.getForObject("http://localhost:8081/api/usuarios/" + usuarioId, Usuario.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null; // Retorna null si el usuario no se encuentra (HTTP 404)
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el usuario con ID " + usuarioId + " desde UsuarioService. Error: " + e.getMessage());
        }
    }

    /**
     * Agrega un producto al carrito o actualiza su cantidad si ya existe.
     * El precio y nombre del producto se obtienen en tiempo real del ProductoService.
     * @param carritoId ID del carrito al que se agregará el producto.
     * @param productoId ID del producto a agregar.
     * @param cantidad Cantidad a agregar.
     * @return El carrito actualizado.
     * @throws RuntimeException si el carrito o producto no se encuentran, o si el stock es insuficiente.
     */
    @Transactional
    public Carrito agregarProductoAlCarrito(Long carritoId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor que cero.");
        }

        // Obtener detalles del producto para validar stock
        Producto productoDetalles = obtenerDetallesProductoDesdeMS(productoId);
        if (productoDetalles == null) {
            throw new RuntimeException("Producto con ID " + productoId + " no encontrado en ProductoService.");
        }

        if (productoDetalles.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + productoDetalles.getNombre() + ". Stock disponible: " + productoDetalles.getStock());
        }

        Optional<CarritoItem> existingItem = carrito.getItems().stream()
                .filter(item -> item.getProductId().equals(productoId))
                .findFirst();

        if (existingItem.isPresent()) {
            CarritoItem item = existingItem.get();
            item.setCantidad(item.getCantidad() + cantidad);
            carritoItemRepository.save(item);
        } else {
            CarritoItem nuevoItem = CarritoItem.builder()
                    .productId(productoId)
                    .cantidad(cantidad)
                    .carrito(carrito)
                    .build();
            carrito.getItems().add(nuevoItem);
            carritoItemRepository.save(nuevoItem);
        }
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito eliminarProductoDelCarrito(Long carritoId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a eliminar debe ser mayor que cero.");
        }

        Optional<CarritoItem> existingItem = carrito.getItems().stream()
                .filter(item -> item.getProductId().equals(productoId))
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
                .filter(item -> item.getProductId().equals(productId))
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