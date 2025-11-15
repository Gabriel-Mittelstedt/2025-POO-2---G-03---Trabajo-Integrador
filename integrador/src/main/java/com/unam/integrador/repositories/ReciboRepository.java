package com.unam.integrador.repositories;

import com.unam.integrador.model.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Recibo.
 * Proporciona operaciones de acceso a datos para recibos.
 */
@Repository
public interface ReciboRepository extends JpaRepository<Recibo, Long> {
    
    /**
     * Busca un recibo por su número único.
     * 
     * @param numero Número del recibo
     * @return Recibo encontrado o vacío
     */
    Optional<Recibo> findByNumero(String numero);
    
    /**
     * Busca un recibo por el ID del pago asociado.
     * 
     * @param pagoId ID del pago
     * @return Recibo encontrado o vacío
     */
    Optional<Recibo> findByPagoIDPago(Long pagoId);
}
