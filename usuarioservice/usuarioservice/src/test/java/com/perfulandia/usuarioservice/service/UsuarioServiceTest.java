package com.perfulandia.usuarioservice.service;

import com.perfulandia.usuarioservice.model.Usuario;
import com.perfulandia.usuarioservice.repository.UsuarioRepository;
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
public class UsuarioServiceTest{

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "Juan Perez", "juan.perez@example.com", "CLIENTE"); //
    }

    @Test
    @DisplayName("Test 1 - Listar todos los usuarios")
    void listarTodosLosUsuarios() {
        List<Usuario> usuarios = Arrays.asList(usuario, new Usuario(2L, "Maria Lopez", "maria.lopez@example.com", "ADMIN"));
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<Usuario> result = usuarioService.listar();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test 2 - Guardar nuevo usuario")
    void guardarNuevoUsuario() {
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.guardar(usuario);

        assertNotNull(result);
        assertEquals(usuario.getNombre(), result.getNombre());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Test 3 - Buscar usuario por id")
    void buscarUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.buscar(1L);

        assertNotNull(result);
        assertEquals(usuario.getId(), result.getId());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test 4 - Buscar usuario por id cuando usuario NO existe")
    void buscarCuandoUsuarioNoExiste() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        Usuario result = usuarioService.buscar(99L);

        assertNull(result);
        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Test 5 - Eliminar usuario por id")
    void eliminarUsuarioPorId() {
        doNothing().when(usuarioRepository).deleteById(1L); // Para métodos void

        usuarioService.eliminar(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }
}