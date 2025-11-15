package com.unam.integrador.model.enums;

/**
 * Enum que define los tipos de pago disponibles en el sistema.
 * 
 * <p>Utilizado para diferenciar entre:</p>
 * <ul>
 *   <li>TOTAL: Pago que cubre el monto completo del saldo pendiente (HU-11)</li>
 *   <li>PARCIAL: Pago que cubre parte del saldo pendiente (HU-12)</li>
 * </ul>
 */
public enum TipoPago {
    TOTAL("Pago Total"),
    PARCIAL("Pago Parcial");
    
    private final String descripcion;
    
    TipoPago(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
