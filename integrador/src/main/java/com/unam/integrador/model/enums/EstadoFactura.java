package com.unam.integrador.model.enums;

/**
 * Enum que representa los posibles estados de una factura.
 */
public enum EstadoFactura {
    PENDIENTE("Pendiente"),
    PARCIALMENTE_PAGADA("Parcialmente Pagada"),
    PAGADA("Pagada"),
    VENCIDA("Vencida"),
    ANULADA("Anulada");
    
    private final String descripcion;
    
    EstadoFactura(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
