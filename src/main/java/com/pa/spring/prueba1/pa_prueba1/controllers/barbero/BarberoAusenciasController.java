package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.SolicitudAusenciaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/barbero/ausencias")
public class BarberoAusenciasController {

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private SolicitudAusenciaService solicitudAusenciaService;

    private Barbero obtenerBarberoActual(Authentication authentication) {
        String email = authentication.getName();
        return barberoService.obtenerBarberoPorEmail(email);
    }

    /**
     * Muestra la página de gestión de ausencias
     */
    @GetMapping
    public String ausencias(Model model, Authentication authentication) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            List<SolicitudAusencia> solicitudes = solicitudAusenciaService.obtenerSolicitudesPorBarbero(barbero.getIdBarbero());
            
            long solicitudesPendientes = solicitudAusenciaService.contarPorEstado(barbero.getIdBarbero(), SolicitudAusencia.EstadoSolicitud.PENDIENTE);
            long solicitudesAprobadas = solicitudAusenciaService.contarPorEstado(barbero.getIdBarbero(), SolicitudAusencia.EstadoSolicitud.APROBADA);
            long solicitudesRechazadas = solicitudAusenciaService.contarPorEstado(barbero.getIdBarbero(), SolicitudAusencia.EstadoSolicitud.RECHAZADA);

            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            model.addAttribute("solicitudes", solicitudes);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesAprobadas", solicitudesAprobadas);
            model.addAttribute("solicitudesRechazadas", solicitudesRechazadas);
            model.addAttribute("diasLibresRestantes", 15); // Puedes calcular esto dinámicamente

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar ausencias: " + e.getMessage());
        }
        return "barbero/ausencias";
    }

    /**
     * Procesa una nueva solicitud de ausencia
     */
    @PostMapping("/solicitar")
    public String solicitarAusencia(
            @RequestParam("tipoAusencia") String tipoAusenciaStr,
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(value = "horaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam(value = "horaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
            @RequestParam("motivo") String motivo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            SolicitudAusencia solicitud = new SolicitudAusencia();
            solicitud.setBarbero(barbero);
            solicitud.setMotivo(motivo);
            
            // Determinar tipo de ausencia
            SolicitudAusencia.TipoAusencia tipoAusencia;
            if ("HORAS_ESPECIFICAS".equals(tipoAusenciaStr)) {
                tipoAusencia = SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS;
                solicitud.setFecha(fechaInicio);
                solicitud.setHoraInicio(horaInicio);
                solicitud.setHoraFin(horaFin);
            } else {
                tipoAusencia = SolicitudAusencia.TipoAusencia.DIA_COMPLETO;
                solicitud.setFechaInicio(fechaInicio);
                solicitud.setFechaFin(fechaFin != null ? fechaFin : fechaInicio);
            }
            solicitud.setTipoAusencia(tipoAusencia);
            
            // Validaciones
            if (fechaInicio.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "No puedes solicitar ausencias para fechas pasadas");
                return "redirect:/barbero/ausencias";
            }
            
            if (tipoAusencia == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
                if (horaInicio == null || horaFin == null) {
                    redirectAttributes.addFlashAttribute("error", "Debes especificar hora de inicio y fin");
                    return "redirect:/barbero/ausencias";
                }
                if (horaFin.isBefore(horaInicio)) {
                    redirectAttributes.addFlashAttribute("error", "La hora de fin debe ser posterior a la hora de inicio");
                    return "redirect:/barbero/ausencias";
                }
            }
            
            solicitudAusenciaService.crearSolicitud(solicitud);
            
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud de ausencia enviada correctamente. Espera la respuesta del administrador.");
            System.out.println("✓ Solicitud de ausencia creada para barbero: " + barbero.getNombre());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear solicitud: " + e.getMessage());
            System.err.println("✗ Error al crear solicitud de ausencia: " + e.getMessage());
        }
        
        return "redirect:/barbero/ausencias";
    }

    /**
     * Cancela una solicitud pendiente
     */
    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(
            @PathVariable("id") Long idSolicitud,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            solicitudAusenciaService.cancelarSolicitud(idSolicitud, barbero.getIdBarbero());
            
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud cancelada exitosamente");
            System.out.println("✓ Solicitud cancelada ID: " + idSolicitud);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar solicitud: " + e.getMessage());
            System.err.println("✗ Error al cancelar solicitud: " + e.getMessage());
        }
        
        return "redirect:/barbero/ausencias";
    }
}