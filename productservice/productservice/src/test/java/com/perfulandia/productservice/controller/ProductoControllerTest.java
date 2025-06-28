package com.perfulandia.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.model.Usuario;
import com.perfulandia.productservice.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Producto producto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Perfume Chanel", 150.00, 20);
        usuario = new Usuario(1L, "Test User", "test@user.com", "ADMIN");
    }

    @Test
    @DisplayName("Debería guardar un nuevo producto")
    void guardarProductoTest() throws Exception {
        given(productoService.guardar(any(Producto.class))).willReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is(producto.getNombre())));
    }

    @Test
    @DisplayName("Test 1 - obtener un usuario desde el microservicio de usuarios")
    void obtenerUsuarioTest() throws Exception {
        String url = "http://localhost:8081/api/usuarios/" + usuario.getId();
        given(restTemplate.getForObject(url, Usuario.class)).willReturn(usuario);

        mockMvc.perform(get("/api/productos/usuario/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is(usuario.getNombre())));
    }
}