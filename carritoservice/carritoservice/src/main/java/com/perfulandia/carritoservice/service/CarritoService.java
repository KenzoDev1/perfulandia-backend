package com.perfulandia.carritoservice.service;

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
    private final RestTemplate restTemplate; // Para comunicarse con ProductoService y UsuarioService

    @Autowired
    public CarritoService(CarritoRepository carritoRepository, CarritoItemRepository carritoItemRepository, RestTemplate restTemplate) {
        this.carritoRepository = carritoRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.restTemplate = restTemplate;
    }

    // Listar todos los carritos
    public List<Carrito> listarTodosLosCarritos() {
        return carritoRepository.findAll();
    }

    // Buscar carrito por id
    public Optional<Carrito> buscarCarritoPorId(Long id) {
        return carritoRepository.findById(id);
    }

    /* "Guardar carrito" tuvo que crearse manualmente, en vez de utilizar su metodo
    * CRUD de JpaRepository porque necesitaba verificar en el atributo usuarioId exista */

    // Eliminar carrito
    @Transactional
    public void eliminarCarrito(Long id) {
        carritoRepository.deleteById(id);
    }

    /**
     * Obtiene los detalles de un usuario desde el UsuarioService.
     * Este es el nuevo método para validar el usuario.'
     * @param usuarioId ID del usuario.
     * @return Objeto Usuario (DTO) con sus detalles, o null si no existe.
     * @throws RuntimeException si hay un error de comunicación con el UsuarioService.
     */
    private Optional<Usuario> obtenerDetallesUsuarioDesdeMS(Long usuarioId) {
        try {
            Usuario usuario = restTemplate.getForObject("http://localhost:8081/api/usuarios/" + usuarioId, Usuario.class);
            return Optional.ofNullable(usuario);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty(); // Retorna null si el usuario no se encuentra (HTTP 404)
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el usuario con ID " + usuarioId + " desde UsuarioService. Error: " + e.getMessage());
        }
    }

    /**
     * Obtiene los detalles de un producto desde el ProductoService.
     * @param productoId ID del producto.
     * @return Objeto Producto (DTO) con sus detalles actualizados.
     * @throws RuntimeException si el producto no es encontrado.
     */
    private Optional<Producto> obtenerDetallesProductoDesdeMS(Long productoId) {
        try {
            Producto producto = restTemplate.getForObject("http://localhost:8082/api/productos/" + productoId, Producto.class);
            return Optional.ofNullable(producto);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty(); // Retorna null si el producto no se encuentra (HTTP 404)
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el producto con ID " + productoId + " desde ProductoService. Error: " + e.getMessage());
        }
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
        Optional<Usuario> usuario = obtenerDetallesUsuarioDesdeMS(usuarioId);
        if (usuario.isEmpty()) {
            throw new RuntimeException("No se puede crear un carrito para el usuario con ID " + usuarioId + " porque no existe.");
        }

        // 2. Si el usuario existe, proceder a crear el carrito
        Carrito carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        return carritoRepository.save(carrito);
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
    public CarritoItem agregarProductoAlCarrito(Long carritoId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con ID: " + carritoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor que cero.");
        }

        // Obtener detalles del producto para validar stock
        Optional<Producto> productoOptional = obtenerDetallesProductoDesdeMS(productoId);
        Producto productoDetalles = productoOptional.orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));
        if (productoOptional.isEmpty()) {
            throw new RuntimeException("Producto con ID " + productoId + " no encontrado en ProductoService.");
        }

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
            /*Se suma la cantidad ya que segun la regla de negocio si no agrega un
            carritoItem, entonces se actualizara su cantidad, segun lo ingresado en
            el parametro
            */
            carritoItem.setCantidad(cantidad);
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