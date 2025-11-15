package com.unam.integrador.controllers;

import com.unam.integrador.services.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para la gestión de facturas.
 * 
 * <p>Maneja las operaciones de consulta y visualización de facturas.</p>
 */
@Controller
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaController {
    
    private final FacturaService facturaService;
    
    /**
     * Muestra el listado de todas las facturas.
     * 
     * @param model Modelo de la vista
     * @return Vista de lista de facturas
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("facturas", facturaService.obtenerTodas());
        return "facturas/lista";
    }
    
    /**
     * Muestra el detalle de una factura.
     * 
     * @param id ID de la factura
     * @param model Modelo de la vista
     * @return Vista de detalle de la factura
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("factura", facturaService.obtenerPorId(id));
            return "facturas/detalle";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/facturas";
        }
    }
}
