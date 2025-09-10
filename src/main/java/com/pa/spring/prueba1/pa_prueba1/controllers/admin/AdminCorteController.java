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
     *
     * @param model objeto para enviar datos a la vista
     * @return vista con la lista de cortes
     */
    @GetMapping
    public String listarCortes(Model model) {
        // Obtener todos los cortes de cabello desde el servicio
        List<CorteDeCabello> cortes = corteService.obtenerTodos();
        model.addAttribute("cortes", cortes);
        return "admin/cortes/lista"; // Retorna la vista que muestra la lista de cortes
    }
    
    /**
     * Muestra el formulario para agregar un nuevo corte de cabello.
     *
     * @param model objeto para enviar datos a la vista
     * @return vista del formulario para crear un nuevo corte
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("corte", new CorteDeCabello()); // Se crea una nueva instancia de CorteDeCabello
        return "admin/cortes/formulario"; // Retorna la vista del formulario para ingresar los detalles del nuevo corte
    }
    
    /**
     * Guarda un nuevo corte de cabello en la base de datos.
     *
     * @param corte objeto CorteDeCabello que contiene los detalles del corte
     * @param redirectAttributes para enviar un mensaje de éxito
     * @return redirige a la lista de cortes
     */
    @PostMapping("/guardar")
    public String guardarCorte(@ModelAttribute CorteDeCabello corte, RedirectAttributes redirectAttributes) {
        corteService.guardar(corte); // Llama al servicio para guardar el corte en la base de datos
        redirectAttributes.addFlashAttribute("mensaje", "Corte guardado con éxito"); // Añade mensaje de éxito
        return "redirect:/admin/cortes"; // Redirige a la lista de cortes
    }
    
    /**
     * Muestra el formulario para editar un corte de cabello existente.
     *
     * @param id identificador del corte a editar
     * @param model objeto para enviar datos a la vista
     * @return vista del formulario con los datos del corte a editar
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        // Obtener el corte de cabello por su id desde el servicio
        CorteDeCabello corte = corteService.obtenerPorId(id);
        model.addAttribute("corte", corte); // Añadir el corte al modelo
        return "admin/cortes/formulario"; // Retorna la vista del formulario para editar el corte
    }
    
    /**
     * Elimina un corte de cabello de la base de datos.
     *
     * @param id identificador del corte a eliminar
     * @param redirectAttributes para enviar un mensaje de éxito
     * @return redirige a la lista de cortes
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarCorte(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        corteService.eliminar(id); // Llama al servicio para eliminar el corte por su id
        redirectAttributes.addFlashAttribute("mensaje", "Corte eliminado con éxito"); // Mensaje de éxito
        return "redirect:/admin/cortes"; // Redirige a la lista de cortes
    }
}
