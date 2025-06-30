package com.perfulandia.carritoservice.assemblers;

import com.perfulandia.carritoservice.model.CarritoItem;
import com.perfulandia.carritoservice.controller.CarritoController;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CarritoItemModelAssembler implements RepresentationModelAssembler<CarritoItem, EntityModel<CarritoItem>> {
    @Override
    public EntityModel<CarritoItem> toModel(CarritoItem item) {
        // En este caso, un "self" link para un CarritoItem individual no tiene un endpoint directo,
        // por lo que el enlace más relevante es al carrito al que pertenece.
        try {
            return EntityModel.of(item,
                    linkTo(methodOn(CarritoController.class).buscarCarritoPorId(item.getCarrito().getId())).withRel("carrito"));
        } catch (Exception e) {
            // Manejar la excepción, aunque con la lógica actual no debería ocurrir si el item tiene un carrito.
            throw new RuntimeException("Error al crear el modelo para CarritoItem", e);
        }
    }
}