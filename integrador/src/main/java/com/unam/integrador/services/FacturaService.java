package com.unam.integrador.services;

import com.unam.integrador.model.Factura;
import com.unam.integrador.model.enums.EstadoFactura;
import com.unam.integrador.repositories.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para la gestión de facturas.
 * 
 * <p>Proporciona operaciones de negocio relacionadas con facturas.</p>
 */
@Service
@RequiredArgsConstructor
public class FacturaService {
    
    private final FacturaRepository facturaRepository;
    
    /**
     * Obtiene una factura por su ID.
     * 
     * @param id ID de la factura
     * @return La factura encontrada
     * @throws IllegalArgumentException si no se encuentra la factura
     */
    @Transactional(readOnly = true)
    public Factura obtenerPorId(Long id) {
        return facturaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + id));
    }
    
    /**
     * Obtiene todas las facturas.
     * 
     * @return Lista de todas las facturas
     */
    @Transactional(readOnly = true)
    public List<Factura> obtenerTodas() {
        return facturaRepository.findAllByOrderByFechaEmisionDesc();
    }
    
    /**
     * Obtiene todas las facturas de un cliente.
     * 
     * @param clienteId ID del cliente
     * @return Lista de facturas del cliente
     */
    @Transactional(readOnly = true)
    public List<Factura> obtenerPorCliente(Long clienteId) {
        return facturaRepository.findByClienteIdOrderByFechaEmisionDesc(clienteId);
    }
    
    /**
     * Obtiene todas las facturas por estado.
     * 
     * @param estado Estado de la factura
     * @return Lista de facturas con ese estado
     */
    @Transactional(readOnly = true)
    public List<Factura> obtenerPorEstado(EstadoFactura estado) {
        return facturaRepository.findByEstadoOrderByFechaEmisionDesc(estado);
    }
    
    /**
     * Guarda una factura.
     * 
     * @param factura Factura a guardar
     * @return Factura guardada
     */
    @Transactional
    public Factura guardar(Factura factura) {
        return facturaRepository.save(factura);
    }
}
