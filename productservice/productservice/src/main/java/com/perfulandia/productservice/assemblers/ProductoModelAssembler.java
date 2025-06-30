package com.perfulandia.productservice.assemblers;

import com.perfulandia.productservice.model.Producto;
import com.perfulandia.productservice.controller.ProductoController;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).buscar(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listar()).withRel("productos"));
    }
}