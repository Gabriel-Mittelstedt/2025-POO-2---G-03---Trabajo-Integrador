package com.unam.integrador.repositories;

import com.unam.integrador.model.Factura;
import com.unam.integrador.model.enums.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Factura.
 * Proporciona operaciones de acceso a datos para facturas.
 */
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    
    /**
     * Obtiene todas las facturas de un cliente específico.
     * 
     * @param clienteId ID del cliente
     * @return Lista de facturas del cliente
     */
    @Query("SELECT f FROM Factura f WHERE f.cliente.id = :clienteId ORDER BY f.fechaEmision DESC")
    List<Factura> findByClienteId(@Param("clienteId") Long clienteId);
    
    /**
     * Obtiene todas las facturas de un cliente ordenadas por fecha.
     * 
     * @param clienteId ID del cliente
     * @return Lista de facturas del cliente
     */
    List<Factura> findByClienteIdOrderByFechaEmisionDesc(Long clienteId);
    
    /**
     * Obtiene todas las facturas por estado.
     * 
     * @param estado Estado de la factura
     * @return Lista de facturas con ese estado
     */
    List<Factura> findByEstado(EstadoFactura estado);
    
    /**
     * Obtiene todas las facturas por estado ordenadas por fecha.
     * 
     * @param estado Estado de la factura
     * @return Lista de facturas ordenadas por fecha
     */
    List<Factura> findByEstadoOrderByFechaEmisionDesc(EstadoFactura estado);
    
    /**
     * Obtiene todas las facturas ordenadas por fecha de emisión descendente.
     * 
     * @return Lista de facturas ordenada por fecha
     */
    List<Factura> findAllByOrderByFechaEmisionDesc();
}
