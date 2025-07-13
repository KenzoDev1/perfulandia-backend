package com.perfulandia.carritoservice.assemblers;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.controller.CarritoController;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, EntityModel<Carrito>> {
    /*
    * Con esta interfaz: RepresentationModelAssembler<Carrito, EntityModel<Carrito>>
    * le decimos a CarritoModelAssembler que transforme Carrito a EntityModel<Carrito>
    * que incluyen los datos del objeto y los enlaces HATEOAS.
    * */
    // @Override: Indica que este metodo está sobrescribiendo un metodo de la interfaz RepresentationModelAssembler.
    @Override
    public EntityModel<Carrito> toModel(Carrito carrito) {
        // Self link
        /*
        * methodOn(CarritoController.class): Esto simula una llamada a un metodo en CarritoController.
        * */
        EntityModel<Carrito> carritoModel = EntityModel.of(carrito,
                linkTo(methodOn(CarritoController.class).buscarCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarTodosLosCarritos()).withRel("carritos"));
        return carritoModel;
    }
}