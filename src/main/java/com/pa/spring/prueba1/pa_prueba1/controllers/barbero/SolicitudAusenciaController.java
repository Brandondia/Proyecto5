package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/barbero/solicitudes")
public class SolicitudAusenciaController {

    private final BarberoService barberoService;

    public SolicitudAusenciaController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable Long id,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            barberoService.cancelarSolicitud(id, barbero.getIdBarbero());
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud cancelada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }
        return "redirect:/barbero/ausencias";
    }
}
