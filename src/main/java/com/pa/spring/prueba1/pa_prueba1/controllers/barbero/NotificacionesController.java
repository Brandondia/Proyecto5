package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/barbero/notificaciones")
public class NotificacionesController {

    @Autowired
    private BarberoService barberoService;

    /**
     * Obtiene el barbero actual desde la autenticación
     */
    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    /**
     * Vista principal de notificaciones con filtros y paginación
     */
    @GetMapping
    public String notificaciones(Model model, Authentication auth,
                                 @RequestParam(required = false) String tipo,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String buscar) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            
            // Crear Pageable con ordenamiento por fecha descendente
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            
            Page<Notificacion> notificacionesPage;

            // Filtrar según tipo o búsqueda
            if (buscar != null && !buscar.trim().isEmpty()) {
                notificacionesPage = barberoService.buscarNotificaciones(barbero.getIdBarbero(), buscar, pageable);
            } else if (tipo != null && !tipo.isEmpty()) {
                if (tipo.equals("noLeidas")) {
                    notificacionesPage = barberoService.obtenerNotificacionesNoLeidasPaginadas(barbero.getIdBarbero(), pageable);
                } else {
                    try {
                        Notificacion.TipoNotificacion tipoEnum = Notificacion.TipoNotificacion.valueOf(tipo.toUpperCase());
                        notificacionesPage = barberoService.obtenerNotificacionesPorTipoPaginadas(barbero.getIdBarbero(), tipoEnum, pageable);
                    } catch (IllegalArgumentException e) {
                        notificacionesPage = barberoService.obtenerNotificacionesBarberoP(barbero.getIdBarbero(), pageable);
                    }
                }
            } else {
                notificacionesPage = barberoService.obtenerNotificacionesBarberoP(barbero.getIdBarbero(), pageable);
            }

            // Contadores para los badges
            long total = barberoService.obtenerNotificacionesBarbero(barbero.getIdBarbero()).size();
            long noLeidas = barberoService.contarNotificacionesNoLeidas(barbero.getIdBarbero());
            long notifReservas = barberoService.contarNotificacionesPorTipos(barbero.getIdBarbero(), 
                List.of(Notificacion.TipoNotificacion.NUEVA_RESERVA, Notificacion.TipoNotificacion.RESERVA_CANCELADA));
            long notifAusencias = barberoService.contarNotificacionesPorTipos(barbero.getIdBarbero(),
                List.of(Notificacion.TipoNotificacion.AUSENCIA_APROBADA, Notificacion.TipoNotificacion.AUSENCIA_RECHAZADA));
            long notifSistema = barberoService.contarNotificacionesPorTipo(barbero.getIdBarbero(), 
                Notificacion.TipoNotificacion.SISTEMA);

            // Agregar atributos al modelo
            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("notificaciones", notificacionesPage.getContent());
            model.addAttribute("totalNotificaciones", total);
            model.addAttribute("noLeidas", noLeidas);
            model.addAttribute("notifReservas", notifReservas);
            model.addAttribute("notifAusencias", notifAusencias);
            model.addAttribute("notifSistema", notifSistema);
            model.addAttribute("tipoFiltro", tipo);
            model.addAttribute("busqueda", buscar);
            
            // Información de paginación
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", notificacionesPage.getTotalPages());
            model.addAttribute("totalItems", notificacionesPage.getTotalElements());
            model.addAttribute("hasNext", notificacionesPage.hasNext());
            model.addAttribute("hasPrevious", notificacionesPage.hasPrevious());

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
        return "barbero/notificaciones";
    }

    /**
     * Marca una notificación como leída (AJAX)
     */
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

    /**
     * Marca todas las notificaciones como leídas (AJAX)
     */
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

    /**
     * Elimina una notificación (AJAX)
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public Map<String, Object> eliminarNotificacion(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            barberoService.eliminarNotificacion(id, barbero.getIdBarbero());
            
            response.put("success", true);
            response.put("message", "Notificación eliminada correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Obtiene notificaciones recientes para actualizaciones en tiempo real (AJAX)
     */
    @GetMapping("/recientes")
    @ResponseBody
    public Map<String, Object> obtenerNotificacionesRecientes(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            List<Notificacion> recientes = barberoService.obtenerUltimasNotificaciones(barbero.getIdBarbero(), 5);
            long noLeidas = barberoService.contarNotificacionesNoLeidas(barbero.getIdBarbero());
            
            response.put("success", true);
            response.put("notificaciones", recientes);
            response.put("noLeidas", noLeidas);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Guarda la configuración de notificaciones del barbero (AJAX)
     */
    @PostMapping("/configuracion")
    @ResponseBody
    public Map<String, Object> guardarConfiguracion(@RequestBody Map<String, Boolean> configuracion, 
                                                     Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            barberoService.guardarConfiguracionNotificaciones(barbero.getIdBarbero(), configuracion);
            
            response.put("success", true);
            response.put("message", "Configuración guardada correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ==================== MÉTODOS HELPER PARA LA VISTA ====================

    /**
     * Obtiene el título de una notificación según su tipo
     */
    @ModelAttribute("getTituloNotificacion")
    public String getTituloNotificacion(Notificacion.TipoNotificacion tipo) {
        if (tipo == null) return "Notificación";
        return switch (tipo) {
            case NUEVA_RESERVA -> "Nueva Reserva";
            case RESERVA_CANCELADA -> "Reserva Cancelada";
            case AUSENCIA_APROBADA -> "Ausencia Aprobada";
            case AUSENCIA_RECHAZADA -> "Ausencia Rechazada";
            case RECORDATORIO -> "Recordatorio de Cita";
            case VALORACION -> "Nueva Valoración";
            case SISTEMA -> "Actualización del Sistema";
        };
    }

    /**
     * Formatea el tiempo transcurrido desde la creación de la notificación
     */
    @ModelAttribute("getFormatoTiempoTranscurrido")
    public String getFormatoTiempoTranscurrido(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            return "Fecha desconocida";
        }
        
        Duration duration = Duration.between(fechaCreacion, LocalDateTime.now());
        
        long segundos = duration.getSeconds();
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;
        long semanas = dias / 7;
        long meses = dias / 30;
        
        if (segundos < 60) {
            return "Hace un momento";
        } else if (minutos < 60) {
            return "Hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        } else if (horas < 24) {
            return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (dias < 7) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        } else if (semanas < 4) {
            return "Hace " + semanas + (semanas == 1 ? " semana" : " semanas");
        } else if (meses < 12) {
            return "Hace " + meses + (meses == 1 ? " mes" : " meses");
        } else {
            long anios = dias / 365;
            return "Hace " + anios + (anios == 1 ? " año" : " años");
        }
    }
}