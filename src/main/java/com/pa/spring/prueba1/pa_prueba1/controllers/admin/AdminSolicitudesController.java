package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/solicitudes")
public class AdminSolicitudesController {

    private final BarberoService barberoService;

    public AdminSolicitudesController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    @GetMapping
    public String listarSolicitudes(Model model, 
                                   @RequestParam(required = false) String filtro) {
        List<SolicitudAusencia> solicitudes;
        
        if ("todas".equals(filtro)) {
            solicitudes = barberoService.obtenerTodasLasSolicitudes();
        } else {
            // Por defecto, mostrar solo pendientes
            solicitudes = barberoService.obtenerSolicitudesPendientes();
        }
        
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("filtroActual", filtro != null ? filtro : "pendientes");
        
        // Contador de pendientes para el badge
        long pendientes = barberoService.contarSolicitudesPendientes();
        model.addAttribute("totalPendientes", pendientes);
        
        return "admin/solicitudes-barberos";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        // Aquí podrías agregar una vista detallada si lo necesitas
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobarSolicitud(@PathVariable Long id,
                                  @RequestParam(required = false) String comentario,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String emailAdmin = authentication.getName();
            barberoService.aprobarSolicitud(id, emailAdmin, comentario);
            redirectAttributes.addFlashAttribute("success", "Solicitud aprobada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar: " + e.getMessage());
        }
        
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id,
                                   @RequestParam(required = false) String comentario,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            String emailAdmin = authentication.getName();
            
            if (comentario == null || comentario.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe proporcionar un motivo de rechazo");
                return "redirect:/admin/solicitudes";
            }
            
            barberoService.rechazarSolicitud(id, emailAdmin, comentario);
            redirectAttributes.addFlashAttribute("success", "Solicitud rechazada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar: " + e.getMessage());
        }
        
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/{id}/reservas-afectadas")
    @ResponseBody
    public long obtenerReservasAfectadas(@PathVariable Long id) {
        try {
            return barberoService.obtenerReservasAfectadas(id);
        } catch (Exception e) {
            return 0;
        }
    }
}