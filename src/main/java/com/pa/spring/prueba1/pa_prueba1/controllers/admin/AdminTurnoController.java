package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.service.ReservaService;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador que maneja las operaciones relacionadas con los turnos de los barberos.
 * Permite listar, filtrar, marcar como disponible/no disponible, y eliminar turnos.
 */
@Controller
@RequestMapping("/admin/turnos")
public class AdminTurnoController {

    @Autowired
    private TurnoService turnoService;
    
    @Autowired
    private BarberoService barberoService;
    
    @Autowired
    private ReservaService reservaService;

    /**
     * Lista todos los turnos y proporciona los barberos para el filtro.
     * 
     * @param model objeto para enviar los datos a la vista
     * @return vista de lista de turnos
     */
    @GetMapping
    public String listarTurnos(Model model) {
        try {
            List<Turno> turnos = turnoService.obtenerTodos();
            model.addAttribute("turnos", turnos);
            
            // Obtener lista de barberos para el filtro
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista"; // Vista con la lista de turnos
    }

    /**
     * Lista solo los turnos disponibles.
     * 
     * @param model objeto para enviar los datos a la vista
     * @return vista de lista de turnos disponibles
     */
    @GetMapping("/disponibles")
    public String listarTurnosDisponibles(Model model) {
        try {
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
            
            // Mostrar turnos disponibles
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponibles();
            model.addAttribute("turnos", turnosDisponibles);
            model.addAttribute("filtroEstado", "DISPONIBLE");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista"; // Vista con los turnos disponibles
    }

    /**
     * Lista solo los turnos no disponibles.
     * 
     * @param model objeto para enviar los datos a la vista
     * @return vista de lista de turnos no disponibles
     */
    @GetMapping("/no-disponibles")
    public String listarTurnosNoDisponibles(Model model) {
        try {
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
            
            // Mostrar turnos no disponibles
            List<Turno> turnosNoDisponibles = turnoService.obtenerTurnosNoDisponibles();
            model.addAttribute("turnos", turnosNoDisponibles);
            model.addAttribute("filtroEstado", "NO_DISPONIBLE");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos no disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista"; // Vista con los turnos no disponibles
    }

    /**
     * Filtra los turnos por barbero y/o estado (disponible/no disponible).
     * 
     * @param barberoId ID del barbero para filtrar
     * @param estado estado del turno para filtrar
     * @param model objeto para enviar los datos a la vista
     * @return vista de lista de turnos filtrados
     */
    @GetMapping("/filtrar")
    public String filtrarTurnos(
            @RequestParam(required = false) Long barberoId,
            @RequestParam(required = false) String estado,
            Model model) {
        
        try {
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
            model.addAttribute("filtroBarberoId", barberoId);
            model.addAttribute("filtroEstado", estado);
            
            List<Turno> turnos;
            
            if (barberoId != null && estado != null && !estado.isEmpty()) {
                // Filtrar por barbero y estado
                Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                turnos = turnoService.obtenerTurnosPorBarberoYEstado(barberoId, estadoEnum);
            } else if (barberoId != null) {
                // Filtrar solo por barbero
                turnos = turnoService.obtenerTurnosPorBarbero(barberoId);
            } else if (estado != null && !estado.isEmpty()) {
                // Filtrar solo por estado
                Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                if (estadoEnum == Turno.EstadoTurno.DISPONIBLE) {
                    turnos = turnoService.obtenerTurnosDisponibles();
                } else {
                    turnos = turnoService.obtenerTurnosNoDisponibles();
                }
            } else {
                // Sin filtros, mostrar todos los turnos
                turnos = turnoService.obtenerTodos();
            }
            
            model.addAttribute("turnos", turnos);
        } catch (Exception e) {
            model.addAttribute("error", "Error al filtrar los turnos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "admin/turnos/lista"; // Vista con los turnos filtrados
    }

    /**
     * Marca un turno como disponible.
     * 
     * @param id ID del turno a marcar como disponible
     * @param redirectAttributes objeto para enviar mensajes de redirección
     * @return redirige a la lista de turnos con mensaje de éxito o error
     */
    @GetMapping("/marcar-disponible/{id}")
    public String marcarTurnoDisponible(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si hay reservas para este turno
            if (reservaService.existeReservaParaTurno(id)) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede marcar como disponible un turno con reservas pendientes");
                return "redirect:/admin/turnos";
            }
            
            Turno turno = turnoService.marcarTurnoDisponible(id);
            
            if (turno != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Turno marcado como disponible");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo marcar el turno como disponible");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos"; // Redirige a la lista de turnos
    }

    /**
     * Marca un turno como no disponible.
     * 
     * @param id ID del turno a marcar como no disponible
     * @param redirectAttributes objeto para enviar mensajes de redirección
     * @return redirige a la lista de turnos con mensaje de éxito o error
     */
    @GetMapping("/marcar-no-disponible/{id}")
    public String marcarTurnoNoDisponible(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Turno turno = turnoService.marcarTurnoNoDisponible(id);
            
            if (turno != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Turno marcado como no disponible");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo marcar el turno como no disponible");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos"; // Redirige a la lista de turnos
    }

    /**
     * Elimina un turno.
     * 
     * @param id ID del turno a eliminar
     * @param redirectAttributes objeto para enviar mensajes de redirección
     * @return redirige a la lista de turnos con mensaje de éxito o error
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarTurno(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Verificar si hay reservas para este turno
            if (reservaService.existeReservaParaTurno(id)) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede eliminar un turno con reservas pendientes");
                return "redirect:/admin/turnos";
            }
            
            turnoService.eliminarTurno(id); // Eliminar el turno
            redirectAttributes.addFlashAttribute("mensaje", "Turno eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el turno: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos"; // Redirige a la lista de turnos
    }
}
