package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.model.CarritoItem;
import com.perfulandia.carritoservice.model.Producto;
// IMPORTANTE: Eliminar clase del modelo Usuario, si no le doy uso
import com.perfulandia.carritoservice.repository.CarritoRepository;
import com.perfulandia.carritoservice.repository.CarritoItemRepository;

import org.springframework.beans.factory.annotation.Autowired; // Importación específica
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importación específica para transacciones
import org.springframework.web.client.RestTemplate; // Importación específica para comunicación HTTP

import java.util.List;
import java.util.Optional;
import java.util.Set; // Importación específica para Set
import java.util.stream.Collectors; // Importación específica para Collectors

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final RestTemplate restTemplate; // Para comunicarse con ProductoService

    // Inyección de dependencias a través del constructor
    @Autowired
    public CarritoService(CarritoRepository carritoRepository, CarritoItemRepository carritoItemRepository, RestTemplate restTemplate) {
        this.carritoRepository = carritoRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.restTemplate = restTemplate;
    }

    // --- Métodos CRUD básicos para Carrito (usando JpaRepository) ---
    public List<Carrito> listarTodosLosCarritos() {
        return carritoRepository.findAll();
    }

    public Optional<Carrito> buscarCarritoPorId(Long id) {
        return carritoRepository.findById(id);
    }

    // Asegura que las operaciones de base de datos se ejecuten como una sola transacción
    @Transactional
    public Carrito guardarCarrito(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    @Transactional
    public void eliminarCarrito(Long id) {
        carritoRepository.deleteById(id);
    }

    // --- Lógica de Negocio del Carrito ---

    /**
     * Crea un nuevo carrito para un usuario dado.
     * @param usuarioId ID del usuario.
     * @return El carrito recién creado.
     */

    @Transactional
    public Carrito crearNuevoCarrito(Long usuarioId) {
        Carrito carrito = new Carrito(); // Crea una nueva instancia de Carrito
        carrito.setUsuarioId(usuarioId); // Asigna el ID del usuario
        return carritoRepository.save(carrito);
    }

    /**
     * Obtiene los detalles de un producto desde el ProductoService.
     * @param productId ID del producto.
     * @return Objeto Producto (DTO) con sus detalles actualizados.
     * @throws RuntimeException si el producto no es encontrado.
     */
    /**
     * metodo auxiliar interno (Por eso es private y solo se puede invocar dentro de la misma clase)
     * que sirve de soporte a las operaciones principales
     * (como agregarProductoAlCarrito o eliminarProductoDelCarrito)
     */
    private Producto obtenerDetallesProductoDesdeMS(Long productId) {
        try {
            return restTemplate.getForObject("http://localhost:8082/api/productos/" + productId, Producto.class);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el producto con ID " + productId + " desde ProductoService. Error: " + e.getMessage());
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
        if (productoDetalles == null) { // RestTemplate puede devolver null si no encuentra el recurso
            throw new RuntimeException("Producto con ID " + productoId + " no encontrado en ProductoService.");
        }

        if (productoDetalles.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + productoDetalles.getNombre() + ". Stock disponible: " + productoDetalles.getStock());
        }

        // Buscar si el producto ya existe en el carrito
        Optional<CarritoItem> existingItem = carrito.getItems().stream()
                .filter(item -> item.getProductId().equals(productoId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Si el producto ya está, actualizar la cantidad
            CarritoItem item = existingItem.get();
            item.setCantidad(item.getCantidad() + cantidad);
            // No es necesario guardar item explícitamente si Carrito maneja el cascade
            // pero lo agregamos por claridad y para asegurar la persistencia.
            carritoItemRepository.save(item);
        } else {
            // Si el producto no está, crear un nuevo CarritoItem
            CarritoItem nuevoItem = CarritoItem.builder()
                    .productId(productoId)
                    .cantidad(cantidad)
                    .carrito(carrito) // Establecer la relación bidireccional
                    .build();
            carrito.getItems().add(nuevoItem); // Añadir al set del carrito
            carritoItemRepository.save(nuevoItem); // Guardar el nuevo ítem
        }

        // Actualizar la fecha de modificación del carrito (se maneja en @PreUpdate de Carrito)
        return carritoRepository.save(carrito);
    }

    /**
     * Elimina una cantidad específica de un producto del carrito, o el producto completo si la cantidad
     * a eliminar es mayor o igual a la existente.
     * @param carritoId ID del carrito.
     * @param productoId ID del producto a eliminar.
     * @param cantidad Cantidad a eliminar.
     * @return El carrito actualizado.
     * @throws RuntimeException si el carrito o el producto no se encuentran en el carrito.
     */
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
                // Si la cantidad a eliminar es mayor o igual, eliminar el ítem completo
                carrito.getItems().remove(item); // Se activa orphanRemoval=true
                carritoItemRepository.delete(item); // Eliminar el CarritoItem de la base de datos
            } else {
                // Si la cantidad a eliminar es menor, reducir la cantidad
                item.setCantidad(item.getCantidad() - cantidad);
                carritoItemRepository.save(item); // Guardar el ítem actualizado
            }
            // Actualizar la fecha de modificación del carrito (se maneja en @PreUpdate de Carrito)
            return carritoRepository.save(carrito);
        } else {
            throw new RuntimeException("El producto con ID " + productoId + " no se encontró en el carrito.");
        }
    }

    /**
     * Elimina completamente un producto del carrito, independientemente de la cantidad.
     * @param carritoId ID del carrito.
     * @param productId ID del producto a eliminar.
     * @return El carrito actualizado.
     * @throws RuntimeException si el carrito o el producto no se encuentran en el carrito.
     */
    @Transactional
    public Carrito eliminarProductoCompletoDelCarrito(Long carritoId, Long productId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        CarritoItem itemToRemove = carrito.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto con ID " + productId + " no encontrado en el carrito."));

        carrito.getItems().remove(itemToRemove); // Se activa orphanRemoval=true
        carritoItemRepository.delete(itemToRemove); // Eliminar el CarritoItem de la base de datos

        // Actualizar la fecha de modificación del carrito (se maneja en @PreUpdate de Carrito)
        return carritoRepository.save(carrito);
    }

    /**
     * Vacía completamente un carrito, eliminando todos sus ítems.
     * @param carritoId ID del carrito.
     * @return El carrito vacío.
     * @throws RuntimeException si el carrito no se encuentra.
     */
    @Transactional
    public Carrito vaciarCarrito(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        carrito.getItems().clear(); // Esto activa orphanRemoval=true para eliminar los items de la BD
        // Actualizar la fecha de modificación del carrito (se maneja en @PreUpdate de Carrito)
        return carritoRepository.save(carrito);
    }
}