package com.unam.integrador.config;

import com.unam.integrador.model.CuentaCliente;
import com.unam.integrador.model.Factura;
import com.unam.integrador.model.enums.EstadoCuenta;
import com.unam.integrador.model.enums.EstadoFactura;
import com.unam.integrador.model.enums.TipoCondicionIVA;
import com.unam.integrador.model.enums.TipoFactura;
import com.unam.integrador.repositories.CuentaClienteRepositorie;
import com.unam.integrador.repositories.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Configuración para cargar datos de prueba al iniciar la aplicación.
 * Solo carga datos si la base de datos está vacía.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatosPruebaConfig {
    
    @Bean
    CommandLineRunner initDatosPrueba(CuentaClienteRepositorie clienteRepository, 
                                      FacturaRepository facturaRepository) {
        return args -> {
            // Solo crear datos si no existen clientes
            if (clienteRepository.count() == 0) {
                log.info("🔄 Cargando datos de prueba...");
                
                // Crear cliente de prueba 1
                CuentaCliente cliente1 = new CuentaCliente();
                cliente1.setNombre("Empresa Test");
                cliente1.setRazonSocial("Empresa Test S.A.");
                cliente1.setCuitDni("20123456789");
                cliente1.setDomicilio("Av. Siempre Viva 123, CABA");
                cliente1.setTelefono("011-4444-5555");
                cliente1.setEmail("contacto@empresatest.com");
                cliente1.setCondicionIva(TipoCondicionIVA.RESPONSABLE_INSCRIPTO);
                cliente1.setEstado(EstadoCuenta.ACTIVA);
                cliente1 = clienteRepository.save(cliente1);
                
                // Crear cliente de prueba 2
                CuentaCliente cliente2 = new CuentaCliente();
                cliente2.setNombre("Comercio ABC");
                cliente2.setRazonSocial("Comercio ABC S.R.L.");
                cliente2.setCuitDni("30987654321");
                cliente2.setDomicilio("Calle Falsa 456, Buenos Aires");
                cliente2.setTelefono("011-6666-7777");
                cliente2.setEmail("admin@comercioabc.com");
                cliente2.setCondicionIva(TipoCondicionIVA.RESPONSABLE_INSCRIPTO);
                cliente2.setEstado(EstadoCuenta.ACTIVA);
                cliente2 = clienteRepository.save(cliente2);
                
                // Crear facturas de prueba - Cliente 1
                Factura factura1 = new Factura(1, 1, cliente1, 
                    LocalDate.now().minusDays(10), 
                    LocalDate.now().plusDays(20), 
                    "Noviembre 2025", 
                    TipoFactura.A);
                factura1.setTotal(new BigDecimal("15000.00"));
                factura1.setSaldoPendiente(new BigDecimal("15000.00"));
                factura1.setEstado(EstadoFactura.PENDIENTE);
                facturaRepository.save(factura1);
                
                Factura factura2 = new Factura(1, 2, cliente1, 
                    LocalDate.now().minusDays(5), 
                    LocalDate.now().plusDays(25), 
                    "Noviembre 2025", 
                    TipoFactura.A);
                factura2.setTotal(new BigDecimal("8500.50"));
                factura2.setSaldoPendiente(new BigDecimal("8500.50"));
                factura2.setEstado(EstadoFactura.PENDIENTE);
                facturaRepository.save(factura2);
                
                // Crear facturas de prueba - Cliente 2
                Factura factura3 = new Factura(1, 3, cliente2, 
                    LocalDate.now().minusDays(15), 
                    LocalDate.now().plusDays(15), 
                    "Noviembre 2025", 
                    TipoFactura.B);
                factura3.setTotal(new BigDecimal("12750.00"));
                factura3.setSaldoPendiente(new BigDecimal("12750.00"));
                factura3.setEstado(EstadoFactura.PENDIENTE);
                facturaRepository.save(factura3);
                
                Factura factura4 = new Factura(1, 4, cliente2, 
                    LocalDate.now().minusDays(3), 
                    LocalDate.now().plusDays(27), 
                    "Diciembre 2025", 
                    TipoFactura.B);
                factura4.setTotal(new BigDecimal("5200.00"));
                factura4.setSaldoPendiente(new BigDecimal("5200.00"));
                factura4.setEstado(EstadoFactura.PENDIENTE);
                facturaRepository.save(factura4);
                
                log.info("✅ Datos de prueba cargados exitosamente:");
                log.info("   📌 Clientes creados: 2");
                log.info("   📌 Cliente 1: {} (CUIT: {})", cliente1.getRazonSocial(), cliente1.getCuitDni());
                log.info("   📌 Cliente 2: {} (CUIT: {})", cliente2.getRazonSocial(), cliente2.getCuitDni());
                log.info("   📌 Facturas creadas: 4");
                log.info("   📄 Factura 0001-00000001: $15,000.00 (Cliente 1)");
                log.info("   📄 Factura 0001-00000002: $8,500.50 (Cliente 1)");
                log.info("   📄 Factura 0001-00000003: $12,750.00 (Cliente 2)");
                log.info("   📄 Factura 0001-00000004: $5,200.00 (Cliente 2)");
                log.info("   🎯 Ahora puedes probar los pagos en http://localhost:8080/facturas");
            } else {
                log.info("ℹ️  Los datos de prueba ya existen en la base de datos");
            }
        };
    }
}
