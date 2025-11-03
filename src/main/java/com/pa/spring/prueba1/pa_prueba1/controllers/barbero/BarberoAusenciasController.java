package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/barbero/ausencias")
public class BarberoAusenciasController {

    private final BarberoService barberoService;

    public BarberoAusenciasController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    private Barbero obtenerBarberoActual(Authentication authentication) {
        String email = authentication.getName();
        return barberoService.obtenerBarberoPorEmail(email);
    }

    @GetMapping
    public String ausencias(Model model, Authentication authentication) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            List<SolicitudAusencia> solicitudes = barberoService.obtenerSolicitudesBarbero(barbero.getIdBarbero());
            
            long solicitudesPendientes = solicitudes.stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.PENDIENTE).count();
            long solicitudesAprobadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.APROBADA).count();
            long solicitudesRechazadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.RECHAZADA).count();

            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            model.addAttribute("solicitudes", solicitudes);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesAprobadas", solicitudesAprobadas);
            model.addAttribute("solicitudesRechazadas", solicitudesRechazadas);
            model.addAttribute("diasLibresRestantes", 10);
            model.addAttribute("nuevaSolicitud", new SolicitudAusencia());

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar ausencias: " + e.getMessage());
        }
        return "barbero/ausencias";
    }
}
