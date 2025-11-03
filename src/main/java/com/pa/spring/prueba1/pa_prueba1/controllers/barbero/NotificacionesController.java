package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/barbero/notificaciones")
public class NotificacionesController {

    private final BarberoService barberoService;

    public NotificacionesController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    @GetMapping
    public String notificaciones(Model model, Authentication auth,
                                 @RequestParam(required = false) String tipo) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            List<Notificacion> notificaciones;

            if (tipo != null && !tipo.isEmpty()) {
                if (tipo.equals("noLeidas")) {
                    notificaciones = barberoService.obtenerNotificacionesNoLeidas(barbero.getIdBarbero());
                } else {
                    try {
                        Notificacion.TipoNotificacion tipoEnum = Notificacion.TipoNotificacion.valueOf(tipo.toUpperCase());
                        notificaciones = barberoService.obtenerNotificacionesPorTipo(barbero.getIdBarbero(), tipoEnum);
                    } catch (IllegalArgumentException e) {
                        notificaciones = barberoService.obtenerNotificacionesBarbero(barbero.getIdBarbero());
                    }
                }
            } else {
                notificaciones = barberoService.obtenerNotificacionesBarbero(barbero.getIdBarbero());
            }

            long total = barberoService.obtenerNotificacionesBarbero(barbero.getIdBarbero()).size();
            long noLeidas = barberoService.contarNotificacionesNoLeidas(barbero.getIdBarbero());
            long notifReservas = notificaciones.stream().filter(n -> n.getTipo() == Notificacion.TipoNotificacion.NUEVA_RESERVA || n.getTipo() == Notificacion.TipoNotificacion.RESERVA_CANCELADA).count();
            long notifAusencias = notificaciones.stream().filter(n -> n.getTipo() == Notificacion.TipoNotificacion.AUSENCIA_APROBADA || n.getTipo() == Notificacion.TipoNotificacion.AUSENCIA_RECHAZADA).count();
            long notifSistema = notificaciones.stream().filter(n -> n.getTipo() == Notificacion.TipoNotificacion.SISTEMA).count();

            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("notificaciones", notificaciones);
            model.addAttribute("totalNotificaciones", total);
            model.addAttribute("noLeidas", noLeidas);
            model.addAttribute("notifReservas", notifReservas);
            model.addAttribute("notifAusencias", notifAusencias);
            model.addAttribute("notifSistema", notifSistema);
            model.addAttribute("tipoFiltro", tipo);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar notificaciones: " + e.getMessage());
        }
        return "barbero/notificaciones";
    }

    @PostMapping("/{id}/leer")
    @ResponseBody
    public Map<String, Object> marcarNotificacionLeida(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            barberoService.marcarNotificacionComoLeida(id);
            Barbero barbero = obtenerBarberoActual(auth);
            long noLeidas = barberoService.contarNotificacionesNoLeidas(barbero.getIdBarbero());

            response.put("success", true);
            response.put("noLeidas", noLeidas);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/leer-todas")
    @ResponseBody
    public Map<String, Object> marcarTodasLeidas(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            barberoService.marcarTodasComoLeidas(barbero.getIdBarbero());

            response.put("success", true);
            response.put("message", "Todas las notificaciones marcadas como leídas");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
