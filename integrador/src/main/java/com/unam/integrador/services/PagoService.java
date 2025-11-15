package com.unam.integrador.services;

import com.unam.integrador.model.Factura;
import com.unam.integrador.model.Pago;
import com.unam.integrador.model.Recibo;
import com.unam.integrador.model.enums.MetodoPago;
import com.unam.integrador.repositories.FacturaRepository;
import com.unam.integrador.repositories.PagoRepository;
import com.unam.integrador.repositories.ReciboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para la gestión de pagos.
 * 
 * <p>Implementa la lógica de negocio para:</p>
 * <ul>
 *   <li>HU-11: Registrar pago total de factura</li>
 *   <li>HU-12: Registrar pago parcial de factura</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PagoService {
    
    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final ReciboRepository reciboRepository;
    
    /**
     * Registra un pago total de una factura.
     * 
     * <p>Implementa HU-11: El pago debe cubrir el saldo pendiente completo.</p>
     * 
     * @param facturaId ID de la factura
     * @param metodoPago Método de pago utilizado
     * @param referencia Referencia opcional del pago
     * @return El pago registrado con su recibo generado
     * @throws IllegalArgumentException si la factura no existe
     * @throws IllegalStateException si la factura no puede ser pagada
     */
    @Transactional
    public Pago registrarPagoTotal(Long facturaId, MetodoPago metodoPago, String referencia) {
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + facturaId));
        
        // La validación y creación del pago se delega al modelo rico (Factura)
        BigDecimal saldoPendiente = factura.calcularSaldoPendiente();
        Pago pago = factura.registrarPagoTotal(saldoPendiente, metodoPago, referencia);
        
        // Persistir la factura con el pago
        facturaRepository.save(factura);
        
        // El pago ya está asociado a la factura, pero lo obtenemos con su ID generado
        Pago pagoGuardado = pagoRepository.findById(pago.getIDPago())
            .orElseThrow(() -> new IllegalStateException("Error al guardar el pago"));
        
        // Generar y persistir el recibo
        if (pagoGuardado.getRecibo() != null) {
            reciboRepository.save(pagoGuardado.getRecibo());
        }
        
        return pagoGuardado;
    }
    
    /**
     * Registra un pago parcial de una factura.
     * 
     * <p>Implementa HU-12: El pago debe ser menor al saldo pendiente.</p>
     * 
     * @param facturaId ID de la factura
     * @param monto Monto del pago parcial
     * @param metodoPago Método de pago utilizado
     * @param referencia Referencia opcional del pago
     * @return El pago registrado con su recibo generado
     * @throws IllegalArgumentException si la factura no existe o el monto es inválido
     * @throws IllegalStateException si la factura no puede ser pagada
     */
    @Transactional
    public Pago registrarPagoParcial(Long facturaId, BigDecimal monto, 
                                     MetodoPago metodoPago, String referencia) {
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + facturaId));
        
        // La validación y creación del pago se delega al modelo rico (Factura)
        Pago pago = factura.registrarPagoParcial(monto, metodoPago, referencia);
        
        // Persistir la factura con el pago
        facturaRepository.save(factura);
        
        // El pago ya está asociado a la factura, pero lo obtenemos con su ID generado
        Pago pagoGuardado = pagoRepository.findById(pago.getIDPago())
            .orElseThrow(() -> new IllegalStateException("Error al guardar el pago"));
        
        // Generar y persistir el recibo
        if (pagoGuardado.getRecibo() != null) {
            reciboRepository.save(pagoGuardado.getRecibo());
        }
        
        return pagoGuardado;
    }
    
    /**
     * Obtiene un pago por su ID.
     * 
     * @param id ID del pago
     * @return El pago encontrado
     * @throws IllegalArgumentException si no se encuentra el pago
     */
    @Transactional(readOnly = true)
    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + id));
    }
    
    /**
     * Obtiene todos los pagos.
     * 
     * @return Lista de todos los pagos ordenados por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAllByOrderByFechaPagoDesc();
    }
    
    /**
     * Obtiene todos los pagos de una factura.
     * 
     * @param facturaId ID de la factura
     * @return Lista de pagos de la factura
     */
    @Transactional(readOnly = true)
    public List<Pago> obtenerPorFactura(Long facturaId) {
        return pagoRepository.findByFacturaId(facturaId);
    }
    
    /**
     * Obtiene el recibo asociado a un pago.
     * 
     * @param pagoId ID del pago
     * @return El recibo encontrado o null si no existe
     */
    @Transactional(readOnly = true)
    public Recibo obtenerReciboPorPago(Long pagoId) {
        return reciboRepository.findByPagoIDPago(pagoId).orElse(null);
    }
}
