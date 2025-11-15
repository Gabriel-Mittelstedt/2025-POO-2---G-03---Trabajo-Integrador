package com.unam.integrador.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.unam.integrador.model.enums.MetodoPago;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostPersist;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un pago realizado sobre una factura.
 * 
 * <p>Esta clase forma parte del agregado Factura y gestiona la lógica de negocio
 * relacionada con los pagos, tanto totales como parciales (HU-11 y HU-12).</p>
 * 
 * <p>Cada pago genera automáticamente un recibo asociado.</p>
 */
@Data
@Entity
@NoArgsConstructor
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IDPago;
    
    @Column(nullable = false)
    private LocalDate fechaPago;
    
    @Column(nullable = false)
    private LocalDateTime fechaHoraRegistro;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;
    
    @Column(length = 500)
    private String referencia;
    
    // --- Relaciones ---
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;
    
    @OneToOne(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    private Recibo recibo;
    
    // ==================== LÓGICA DE NEGOCIO (MODELO RICO) ====================
    
    /**
     * Genera automáticamente el recibo después de persistir el pago.
     * 
     * <p>Este método es invocado automáticamente por JPA después del INSERT.</p>
     */
    @PostPersist
    public void generarRecibo() {
        if (this.recibo == null && this.factura != null) {
            Recibo nuevoRecibo = new Recibo();
            nuevoRecibo.setNumero(generarNumeroRecibo());
            nuevoRecibo.setFecha(this.fechaPago);
            nuevoRecibo.setMonto(this.monto);
            nuevoRecibo.setMetodoPago(this.metodoPago);
            nuevoRecibo.setReferencia(this.referencia);
            nuevoRecibo.setFacturasAsociadas(construirDescripcionFacturas());
            nuevoRecibo.setPago(this);
            
            this.recibo = nuevoRecibo;
        }
    }
    
    /**
     * Determina si este pago cubre el total de la factura.
     * 
     * @return true si el pago cubre el saldo pendiente completo al momento del pago
     */
    public boolean esPagoTotal() {
        if (factura == null) {
            return false;
        }
        
        BigDecimal saldoPendiente = factura.calcularSaldoPendiente();
        return this.monto.compareTo(saldoPendiente) == 0;
    }
    
    /**
     * Determina si este pago es parcial.
     * 
     * @return true si el pago no cubre el total de la factura
     */
    public boolean esPagoParcial() {
        return !esPagoTotal();
    }
    
    /**
     * Genera un número único para el recibo.
     * 
     * @return Número de recibo en formato "REC-{timestamp}-{facturaId}"
     */
    private String generarNumeroRecibo() {
        long timestamp = System.currentTimeMillis();
        String facturaId = factura != null ? factura.getIdFactura().toString() : "0";
        return String.format("REC-%d-%s", timestamp, facturaId);
    }
    
    /**
     * Construye la descripción de las facturas asociadas al pago.
     * 
     * @return Descripción con el número de factura y período
     */
    private String construirDescripcionFacturas() {
        if (factura == null) {
            return "Sin factura asociada";
        }
        
        return String.format("Factura %d-%08d - Período: %s", 
            factura.getSerie(), 
            factura.getNroFactura(), 
            factura.getPeriodo());
    }
    
    /**
     * Valida que el pago tenga datos consistentes.
     * 
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public void validar() {
        if (this.monto == null || this.monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        
        if (this.metodoPago == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        
        if (this.factura == null) {
            throw new IllegalArgumentException("El pago debe estar asociado a una factura");
        }
    }
}
