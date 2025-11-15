package com.unam.integrador.repositories;

import com.unam.integrador.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Pago.
 * Proporciona operaciones de acceso a datos para pagos.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    /**
     * Obtiene todos los pagos de una factura específica.
     * 
     * @param facturaId ID de la factura
     * @return Lista de pagos de la factura
     */
    @Query("SELECT p FROM Pago p WHERE p.factura.idFactura = :facturaId ORDER BY p.fechaPago DESC")
    List<Pago> findByFacturaId(@Param("facturaId") Long facturaId);
    
    /**
     * Obtiene todos los pagos ordenados por fecha descendente.
     * 
     * @return Lista de pagos ordenada por fecha
     */
    List<Pago> findAllByOrderByFechaPagoDesc();
}
