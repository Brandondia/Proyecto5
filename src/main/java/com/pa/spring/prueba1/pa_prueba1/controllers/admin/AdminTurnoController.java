package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.ReservaService;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/turnos")
public class AdminTurnoController {

    @Autowired
    private TurnoService turnoService;
    
    @Autowired
    private BarberoService barberoService;
    
    @Autowired
    private ReservaService reservaService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listarTurnos(Model model) {
        try {
            List<Turno> turnos = turnoService.obtenerTodos();
            model.addAttribute("turnos", turnos);
            
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista";
    }

    @GetMapping("/disponibles")
    @Transactional(readOnly = true)
    public String listarTurnosDisponibles(Model model) {
        try {
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
            
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponibles();
            model.addAttribute("turnos", turnosDisponibles);
            model.addAttribute("filtroEstado", "DISPONIBLE");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista";
    }

    @GetMapping("/no-disponibles")
    @Transactional(readOnly = true)
    public String listarTurnosNoDisponibles(Model model) {
        try {
            List<Barbero> barberos = barberoService.obtenerTodos();
            model.addAttribute("barberos", barberos);
            
            List<Turno> turnosNoDisponibles = turnoService.obtenerTurnosNoDisponibles();
            model.addAttribute("turnos", turnosNoDisponibles);
            model.addAttribute("filtroEstado", "NO_DISPONIBLE");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos no disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista";
    }

    @GetMapping("/filtrar")
    @Transactional(readOnly = true)
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
                Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                turnos = turnoService.obtenerTurnosPorBarberoYEstado(barberoId, estadoEnum);
            } else if (barberoId != null) {
                turnos = turnoService.obtenerTurnosPorBarbero(barberoId);
            } else if (estado != null && !estado.isEmpty()) {
                Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                if (estadoEnum == Turno.EstadoTurno.DISPONIBLE) {
                    turnos = turnoService.obtenerTurnosDisponibles();
                } else {
                    turnos = turnoService.obtenerTurnosNoDisponibles();
                }
            } else {
                turnos = turnoService.obtenerTodos();
            }
            
            model.addAttribute("turnos", turnos);
        } catch (Exception e) {
            model.addAttribute("error", "Error al filtrar los turnos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "admin/turnos/lista";
    }

    @GetMapping("/marcar-disponible/{id}")
    public String marcarTurnoDisponible(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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
        
        return "redirect:/admin/turnos";
    }

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
        
        return "redirect:/admin/turnos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTurno(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (reservaService.existeReservaParaTurno(id)) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede eliminar un turno con reservas pendientes");
                return "redirect:/admin/turnos";
            }
            
            turnoService.eliminarTurno(id);
            redirectAttributes.addFlashAttribute("mensaje", "Turno eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el turno: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos";
    }
}
