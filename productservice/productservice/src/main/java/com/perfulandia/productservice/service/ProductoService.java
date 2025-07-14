package com.perfulandia.productservice.service;

import com.perfulandia.productservice.exception.ResourceNotFoundException;
import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.repository.ProductoRepository;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductoService {

    public final ProductoRepository repo;
    public ProductoService(ProductoRepository repo){
        this.repo=repo;
    }

    //listar
    public List<Producto> listar(){
        return repo.findAll();
    }
    //Guardar
    public Producto guardar(Producto producto){
        return repo.save(producto);
    }
    //Buscar por id
    public Producto buscarPorId(long id){
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }
    //Eliminar por id
    public void eliminar(long id){
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar. Usuario no encontrado con ID: " + id);
        }
        repo.deleteById(id);
    }
}
