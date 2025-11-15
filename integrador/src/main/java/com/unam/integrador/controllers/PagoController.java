package com.unam.integrador.controllers;

import com.unam.integrador.model.enums.MetodoPago;
import com.unam.integrador.model.enums.TipoPago;
import com.unam.integrador.model.Pago;
import com.unam.integrador.services.FacturaService;
import com.unam.integrador.services.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Controlador para la gestión de pagos.
 * 
 * <p>Implementa las interfaces web para:</p>
 * <ul>
 *   <li>HU-11: Registrar pago total de factura</li>
 *   <li>HU-12: Registrar pago parcial de factura</li>
 * </ul>
 */
@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {
    
    private final PagoService pagoService;
    private final FacturaService facturaService;
    
    /**
     * Muestra el listado de todos los pagos.
     * 
     * @param model Modelo de la vista
     * @return Vista de lista de pagos
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagos", pagoService.obtenerTodos());
        return "pagos/lista";
    }
    
    /**
     * Muestra el formulario unificado para registrar un pago (total o parcial).
     * 
     * @param facturaId ID de la factura a pagar
     * @param model Modelo de la vista
     * @return Vista del formulario de pago
     */
    @GetMapping("/nuevo/{facturaId}")
    public String formularioPago(@PathVariable Long facturaId, Model model) {
        try {
            model.addAttribute("factura", facturaService.obtenerPorId(facturaId));
            model.addAttribute("metodosPago", MetodoPago.values());
            return "pagos/formulario";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/facturas";
        }
    }
    
    /**
     * Procesa el registro de un pago (total o parcial).
     * 
     * <p>Decide si es pago total o parcial según el parámetro tipoPago.</p>
     * 
     * @param facturaId ID de la factura a pagar
     * @param tipoPago Tipo de pago (TOTAL o PARCIAL)
     * @param monto Monto del pago (requerido solo para pago parcial)
     * @param metodoPago Método de pago utilizado
     * @param referencia Referencia opcional del pago
     * @param redirectAttributes Atributos para redirección
     * @return Redirección al detalle del pago o al formulario en caso de error
     */
    @PostMapping("/registrar")
    public String registrarPago(@RequestParam Long facturaId,
                                @RequestParam TipoPago tipoPago,
                                @RequestParam(required = false) BigDecimal monto,
                                @RequestParam MetodoPago metodoPago,
                                @RequestParam(required = false) String referencia,
                                RedirectAttributes redirectAttributes) {
        try {
            Pago pago;
            
            if (tipoPago == TipoPago.TOTAL) {
                pago = pagoService.registrarPagoTotal(facturaId, metodoPago, referencia);
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Pago total registrado exitosamente. Recibo: " + 
                    (pago.getRecibo() != null ? pago.getRecibo().getNumero() : "N/A"));
            } else {
                if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Debe ingresar un monto válido para el pago parcial");
                    return "redirect:/pagos/nuevo/" + facturaId;
                }
                pago = pagoService.registrarPagoParcial(facturaId, monto, metodoPago, referencia);
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Pago parcial registrado exitosamente. Recibo: " + 
                    (pago.getRecibo() != null ? pago.getRecibo().getNumero() : "N/A"));
            }
            
            return "redirect:/pagos/" + pago.getIDPago();
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pagos/nuevo/" + facturaId;
        }
    }
    
    /**
     * Muestra el detalle de un pago.
     * 
     * @param id ID del pago
     * @param model Modelo de la vista
     * @return Vista de detalle del pago
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        try {
            Pago pago = pagoService.obtenerPorId(id);
            model.addAttribute("pago", pago);
            return "pagos/detalle";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/pagos";
        }
    }
}
