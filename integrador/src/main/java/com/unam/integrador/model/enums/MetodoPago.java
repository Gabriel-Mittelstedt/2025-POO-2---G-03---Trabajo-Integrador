package com.unam.integrador.model.enums;

/**
 * Enum que define los métodos de pago disponibles en el sistema.
 */
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia Bancaria"),
    TARJETA("Tarjeta de Crédito/Débito");
    
    private final String descripcion;
    
    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
