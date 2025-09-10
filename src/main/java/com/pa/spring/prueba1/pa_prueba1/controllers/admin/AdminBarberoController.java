package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador encargado de manejar las peticiones relacionadas con los barberos en el panel de administración.
 * Permite listar, crear, editar y eliminar barberos, así como gestionar sus turnos.
 */
@Controller
@RequestMapping("/admin/barberos")
public class AdminBarberoController {

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private TurnoService turnoService;

    /**
     * Muestra la lista de barberos.
     *
     * @param model objeto para enviar los datos a la vista
     * @return vista para listar los barberos
     */
    @GetMapping
    public String listarBarberos(Model model) {
        List<Barbero> barberos = barberoService.obtenerTodos();
        model.addAttribute("barberos", barberos);
        return "admin/barberos/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo barbero.
     *
     * @param model objeto para enviar datos a la vista
     * @return vista para crear un nuevo barbero
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("barbero", new Barbero());
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        return "admin/barberos/formulario";
    }

    /**
     * Guarda un barbero creado o editado.
     *
     * @param barbero el objeto Barbero a guardar
     * @param redirectAttributes objeto para redirigir y mostrar mensajes de éxito o error
     * @return redirige a la lista de barberos
     */
    @PostMapping("/guardar")
    public String guardarBarbero(@ModelAttribute Barbero barbero, RedirectAttributes redirectAttributes) {
        try {
            barberoService.guardar(barbero);
            redirectAttributes.addFlashAttribute("mensaje", "Barbero guardado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el barbero: " + e.getMessage());
        }
        return "redirect:/admin/barberos";
    }

    /**
     * Muestra el formulario para editar un barbero existente.
     *
     * @param id el ID del barbero a editar
     * @param model objeto para enviar datos a la vista
     * @return vista para editar el barbero
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Barbero barbero = barberoService.obtenerPorId(id);
        if (barbero == null) {
            return "redirect:/admin/barberos";
        }
        model.addAttribute("barbero", barbero);
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        return "admin/barberos/formulario";
    }

    /**
     * Elimina un barbero.
     *
     * @param id el ID del barbero a eliminar
     * @param redirectAttributes objeto para redirigir y mostrar mensajes de éxito o error
     * @return redirige a la lista de barberos
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarBarbero(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            barberoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Barbero eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el barbero: " + e.getMessage());
        }
        return "redirect:/admin/barberos";
    }

    /**
     * Muestra los turnos del barbero y permite gestionarlos.
     *
     * @param id el ID del barbero
     * @param model objeto para enviar los datos a la vista
     * @return vista para gestionar los turnos del barbero
     */
    @GetMapping("/{id}/turnos")
    public String gestionarTurnos(@PathVariable Long id, Model model) {
        Barbero barbero = barberoService.obtenerPorId(id);
        if (barbero == null) {
            return "redirect:/admin/barberos";
        }

        model.addAttribute("barbero", barbero);

        // Obtener turnos disponibles y no disponibles
        List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorBarbero(id);
        List<Turno> turnosReservados = turnoService.obtenerTurnosNoDisponiblesPorBarbero(id);

        model.addAttribute("turnosDisponibles", turnosDisponibles);
        model.addAttribute("turnosReservados", turnosReservados);

        // Para el formulario de generación de turnos
        model.addAttribute("fechaInicio", LocalDate.now());
        model.addAttribute("fechaFin", LocalDate.now().plusDays(14));

        return "admin/barberos/turnos";
    }

    /**
     * Genera turnos disponibles para un barbero en un rango de fechas.
     *
     * @param id el ID del barbero
     * @param fechaInicio fecha de inicio para la generación de turnos
     * @param fechaFin fecha de fin para la generación de turnos
     * @param redirectAttributes objeto para redirigir y mostrar mensajes de éxito o error
     * @return redirige a la página de gestión de turnos
     */
    @PostMapping("/{id}/generar-turnos")
    public String generarTurnos(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            RedirectAttributes redirectAttributes) {

        try {
            Barbero barbero = barberoService.obtenerPorId(id);

            if (barbero != null) {
                List<Turno> turnosGenerados = turnoService.generarTurnosDisponibles(barbero, fechaInicio, fechaFin);
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Se han generado " + turnosGenerados.size() + " turnos disponibles");
            } else {
                redirectAttributes.addFlashAttribute("error", "Barbero no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al generar turnos: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/barberos/" + id + "/turnos";
    }
}
