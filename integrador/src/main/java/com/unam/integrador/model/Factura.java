package com.unam.integrador.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.unam.integrador.model.enums.EstadoFactura;
import com.unam.integrador.model.enums.MetodoPago;
import com.unam.integrador.model.enums.TipoFactura;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "factura")
public class Factura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactura;

    @Column(nullable = false)
    private int serie;

    @Column(name = "numero", nullable = false)
    private int nroFactura;
    
    // Campo adicional para compatibilidad con BD que tiene ambas columnas
    @Column(name = "nro_factura", nullable = false)
    private Integer nroFacturaDuplicado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private CuentaCliente cliente;

    // --- Atributos de la Factura ---
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String periodo;

    @Enumerated(EnumType.STRING)
    private TipoFactura tipo;
    
    @Enumerated(EnumType.STRING)
    private EstadoFactura estado;

    // --- Campos Calculados y Opcionales ---
    // Se inicializan en 0 o null y se calculan con un método.
    private BigDecimal subtotal;
    private double descuento;
    private String motivoDescuento;
    
    @Column(nullable = false)
    private double iva = 21.0; // Alícuota de IVA en porcentaje
    
    private BigDecimal totalIva;
    private BigDecimal saldoPendiente;
    private BigDecimal total;

    //--Relaciones--
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id") 
    private List<ItemFactura> detalleFactura = new ArrayList<>();

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotaCredito> notasCredito = new ArrayList<>();

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Pago> pagos = new ArrayList<>();

    //CONSTRUCTOR
    public Factura(int serie, int nroFactura, CuentaCliente cliente, LocalDate fechaEmision, 
                   LocalDate fechaVencimiento, String periodo, TipoFactura tipo) {
        
        this.serie = serie;
        this.nroFactura = nroFactura;
        this.cliente = cliente;
        this.fechaEmision = fechaEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.periodo = periodo;
        this.tipo = tipo;

        // --- Valores por defecto al crear una factura ---
        this.estado = EstadoFactura.PENDIENTE;
        this.subtotal = BigDecimal.ZERO;
        this.descuento = 0.0;
        this.totalIva = BigDecimal.ZERO;
        this.saldoPendiente = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.motivoDescuento = null;
    }

    // ==================== LÓGICA DE NEGOCIO (MODELO RICO) ====================

    /**
     * Registra un pago total que cubre el monto completo de la factura.
     * 
     * <p>Implementa HU-11: Registrar pago total de factura</p>
     * 
     * @param monto Monto del pago (debe ser igual al saldo pendiente)
     * @param metodoPago Método de pago utilizado
     * @param referencia Referencia opcional del pago
     * @return El pago registrado
     * @throws IllegalStateException si la factura no puede recibir pagos
     * @throws IllegalArgumentException si el monto no coincide con el saldo pendiente
     */
    public Pago registrarPagoTotal(BigDecimal monto, MetodoPago metodoPago, String referencia) {
        validarPuedeRecibirPago();
        
        BigDecimal saldoActual = calcularSaldoPendiente();
        
        if (monto.compareTo(saldoActual) != 0) {
            throw new IllegalArgumentException(
                String.format("El monto del pago total (%.2f) debe ser igual al saldo pendiente (%.2f)", 
                    monto, saldoActual)
            );
        }
        
        Pago pago = crearPago(monto, metodoPago, referencia);
        actualizarEstadoSegunPagos();
        
        return pago;
    }

    /**
     * Registra un pago parcial que cubre parte del monto de la factura.
     * 
     * <p>Implementa HU-12: Registrar pago parcial de factura</p>
     * 
     * @param monto Monto del pago (debe ser menor al saldo pendiente)
     * @param metodoPago Método de pago utilizado
     * @param referencia Referencia opcional del pago
     * @return El pago registrado
     * @throws IllegalStateException si la factura no puede recibir pagos
     * @throws IllegalArgumentException si el monto es inválido
     */
    public Pago registrarPagoParcial(BigDecimal monto, MetodoPago metodoPago, String referencia) {
        validarPuedeRecibirPago();
        
        BigDecimal saldoActual = calcularSaldoPendiente();
        
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        
        if (monto.compareTo(saldoActual) >= 0) {
            throw new IllegalArgumentException(
                String.format("Para un pago parcial, el monto (%.2f) debe ser menor al saldo pendiente (%.2f)", 
                    monto, saldoActual)
            );
        }
        
        Pago pago = crearPago(monto, metodoPago, referencia);
        actualizarEstadoSegunPagos();
        
        return pago;
    }

    /**
     * Calcula el saldo pendiente de pago de la factura.
     * 
     * @return Saldo pendiente (total - suma de pagos realizados)
     */
    public BigDecimal calcularSaldoPendiente() {
        if (this.total == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalPagado = pagos.stream()
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal saldo = this.total.subtract(totalPagado);
        
        // Actualizar el campo saldoPendiente
        this.saldoPendiente = saldo;
        
        return saldo;
    }

    /**
     * Valida si la factura puede recibir pagos.
     * 
     * @throws IllegalStateException si la factura no puede recibir pagos
     */
    private void validarPuedeRecibirPago() {
        if (this.estado == EstadoFactura.ANULADA) {
            throw new IllegalStateException("No se puede registrar un pago en una factura anulada");
        }
        
        if (this.estado == EstadoFactura.PAGADA) {
            throw new IllegalStateException("La factura ya está completamente pagada");
        }
        
        BigDecimal saldo = calcularSaldoPendiente();
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("La factura no tiene saldo pendiente");
        }
    }

    /**
     * Crea un nuevo pago y lo asocia a esta factura.
     * 
     * @param monto Monto del pago
     * @param metodoPago Método de pago
     * @param referencia Referencia del pago
     * @return El pago creado
     */
    private Pago crearPago(BigDecimal monto, MetodoPago metodoPago, String referencia) {
        Pago pago = new Pago();
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setReferencia(referencia);
        pago.setFechaPago(LocalDate.now());
        pago.setFechaHoraRegistro(LocalDateTime.now());
        pago.setFactura(this);
        
        this.pagos.add(pago);
        
        return pago;
    }

    /**
     * Actualiza el estado de la factura según los pagos realizados.
     * 
     * <p>Estados posibles:</p>
     * <ul>
     *   <li>PAGADA: Si el saldo pendiente es cero</li>
     *   <li>PARCIALMENTE_PAGADA: Si hay pagos pero aún queda saldo</li>
     *   <li>PENDIENTE o VENCIDA: Mantiene el estado actual si no hay cambios</li>
     * </ul>
     */
    private void actualizarEstadoSegunPagos() {
        BigDecimal saldo = calcularSaldoPendiente();
        
        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            this.estado = EstadoFactura.PAGADA;
        } else if (!pagos.isEmpty()) {
            this.estado = EstadoFactura.PARCIALMENTE_PAGADA;
        }
        // Si no hay pagos, mantiene el estado actual (PENDIENTE o VENCIDA)
    }

    /**
     * Verifica si la factura está completamente pagada.
     * 
     * @return true si el saldo pendiente es cero
     */
    public boolean estaPagada() {
        return calcularSaldoPendiente().compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Verifica si la factura tiene pagos parciales.
     * 
     * @return true si hay pagos pero aún queda saldo pendiente
     */
    public boolean tienePagosParciales() {
        return !pagos.isEmpty() && calcularSaldoPendiente().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Obtiene el total pagado hasta el momento.
     * 
     * @return Suma de todos los pagos realizados
     */
    public BigDecimal calcularTotalPagado() {
        return pagos.stream()
            .map(Pago::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Verifica si la factura puede ser anulada.
     * Según HU-05, solo se pueden anular facturas no pagadas o con saldo completo.
     * 
     * @return true si la factura puede anularse
     */
    public boolean puedeAnularse() {
        return this.estado != EstadoFactura.ANULADA && 
               (this.estado == EstadoFactura.PENDIENTE || 
                this.estado == EstadoFactura.VENCIDA || 
                calcularSaldoPendiente().compareTo(this.total) == 0);
    }
    
    /**
     * Sincroniza el valor de nroFactura con nroFacturaDuplicado antes de persistir.
     * Necesario para compatibilidad con BD que tiene ambas columnas: numero y nro_factura.
     */
    @PrePersist
    @PreUpdate
    private void sincronizarNumeroFactura() {
        if (nroFacturaDuplicado == null || nroFacturaDuplicado == 0) {
            nroFacturaDuplicado = nroFactura;
        }
    }
    
    /**
     * Genera el número de factura completo en formato legible.
     * 
     * @return Número de factura en formato "Serie-Número"
     */
    public String getNumeroFactura() {
        return String.format("%04d-%08d", serie, nroFactura);
    }
    
    /**
     * Getter para saldoPendiente (necesario para Thymeleaf).
     * Retorna el saldo persistido en la BD para evitar LazyInitializationException.
     * 
     * @return El saldo pendiente de la factura
     */
    public BigDecimal getSaldoPendiente() {
        // Usar el valor persistido en lugar de calcularlo para evitar
        // LazyInitializationException cuando se accede fuera de una transacción
        return saldoPendiente != null ? saldoPendiente : BigDecimal.ZERO;
    }
}
