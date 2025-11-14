package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import com.pa.spring.prueba1.pa_prueba1.service.CorteDeCabelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador que maneja las peticiones relacionadas con los cortes de cabello en el panel administrativo.
 * Permite listar, crear, editar y eliminar cortes de cabello.
 */
@Controller
@RequestMapping("/admin/cortes")
public class AdminCorteController {

    @Autowired
    private CorteDeCabelloService corteService;

    /**
     * Lista todos los cortes de cabello disponibles en el sistema.
     */
    @GetMapping
    public String listarCortes(Model model) {
        try {
            List<CorteDeCabello> cortes = corteService.obtenerTodos();
            model.addAttribute("cortes", cortes);
            System.out.println("✓ Servicios cargados: " + cortes.size());
            return "admin/cortes/lista";
        } catch (Exception e) {
            System.err.println("Error al listar cortes: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar servicios: " + e.getMessage());
            model.addAttribute("cortes", List.of());
            return "admin/cortes/lista";
        }
    }
    
    /**
     * Muestra el formulario para agregar un nuevo corte de cabello.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        try {
            CorteDeCabello corte = new CorteDeCabello();
            // Valores por defecto
            corte.setPrecio(0.0);
            corte.setDuracion(30); // 30 minutos por defecto
            
            model.addAttribute("corte", corte);
            System.out.println("✓ Formulario nuevo cargado");
            return "admin/cortes/formulario";
        } catch (Exception e) {
            System.err.println("Error al mostrar formulario nuevo: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "redirect:/admin/cortes";
        }
    }
    
    /**
     * Guarda un nuevo corte de cabello o actualiza uno existente.
     */
    @PostMapping("/guardar")
    public String guardarCorte(@ModelAttribute CorteDeCabello corte, 
                               RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== GUARDANDO SERVICIO ===");
            System.out.println("Nombre: " + corte.getNombre());
            System.out.println("Precio: " + corte.getPrecio());
            System.out.println("Duración: " + corte.getDuracion() + " minutos");
            
            // Validaciones
            if (corte.getNombre() == null || corte.getNombre().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre del servicio es obligatorio");
                return "redirect:/admin/cortes/nuevo";
            }
            
            if (corte.getPrecio() <= 0) {
                redirectAttributes.addFlashAttribute("error", "El precio debe ser mayor a 0");
                return "redirect:/admin/cortes/nuevo";
            }
            
            if (corte.getDuracion() <= 0) {
                redirectAttributes.addFlashAttribute("error", "La duración debe ser mayor a 0");
                return "redirect:/admin/cortes/nuevo";
            }
            
            // Guardar
            CorteDeCabello guardado = corteService.guardar(corte);
            
            System.out.println("✓ Servicio guardado con ID: " + guardado.getId());
            System.out.println("==========================");
            
            String mensaje = (corte.getId() != null) 
                ? "Servicio actualizado con éxito" 
                : "Servicio creado con éxito";
            
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            return "redirect:/admin/cortes";
            
        } catch (Exception e) {
            System.err.println("Error al guardar corte: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar servicio: " + e.getMessage());
            return "redirect:/admin/cortes";
        }
    }
    
    /**
     * Muestra el formulario para editar un corte de cabello existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        try {
            System.out.println("Editando servicio ID: " + id);
            
            CorteDeCabello corte = corteService.obtenerPorId(id);
            
            if (corte == null) {
                model.addAttribute("error", "Servicio no encontrado con ID: " + id);
                return "redirect:/admin/cortes";
            }
            
            model.addAttribute("corte", corte);
            System.out.println("✓ Cargado para editar: " + corte.getNombre());
            return "admin/cortes/formulario";
            
        } catch (Exception e) {
            System.err.println("Error al cargar servicio para editar: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar servicio: " + e.getMessage());
            return "redirect:/admin/cortes";
        }
    }
    
    /**
     * Elimina un corte de cabello de la base de datos.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarCorte(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Eliminando servicio ID: " + id);
            
            CorteDeCabello corte = corteService.obtenerPorId(id);
            
            if (corte == null) {
                redirectAttributes.addFlashAttribute("error", "Servicio no encontrado");
                return "redirect:/admin/cortes";
            }
            
            corteService.eliminar(id);
            
            System.out.println("✓ Servicio eliminado: " + corte.getNombre());
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado con éxito");
            return "redirect:/admin/cortes";
            
        } catch (Exception e) {
            System.err.println("Error al eliminar servicio: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar servicio: " + e.getMessage());
            return "redirect:/admin/cortes";
        }
    }
}