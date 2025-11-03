package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/barbero")
public class BarberoController {

    private final BarberoService barberoService;

    public BarberoController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    // Helper para obtener el barbero autenticado
    private Barbero obtenerBarberoActual(Authentication authentication) {
        String email = authentication.getName();
        return barberoService.obtenerBarberoPorEmail(email);
    }

    /**
     * PANEL PRINCIPAL DEL BARBERO (HOME/DASHBOARD)
     */
    @GetMapping("/panel")
    public String mostrarPanel(Model model, Authentication authentication) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);

            if (barbero == null) {
                model.addAttribute("error", "No se encontró el barbero");
                return "barbero/panel";
            }

            // --- Estadísticas ---
            LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
            LocalDateTime finHoy = inicioHoy.plusDays(1);

            List<Reserva> reservasSemana = barberoService.obtenerReservasSemanaActual(barbero.getIdBarbero());

            long reservasHoy = reservasSemana.stream()
                    .filter(r -> r.getFechaHoraTurno().isAfter(inicioHoy)
                            && r.getFechaHoraTurno().isBefore(finHoy)
                            && r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();

            long reservasSemanaActivas = reservasSemana.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();

            String proximaReserva = reservasSemana.stream()
                    .filter(r -> r.getFechaHoraTurno().isAfter(LocalDateTime.now())
                            && r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
                    .findFirst()
                    .map(r -> r.getFechaHoraTurno().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .orElse("--:--");

            long solicitudesPendientes = barberoService.obtenerSolicitudesBarbero(barbero.getIdBarbero())
                    .stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.PENDIENTE)
                    .count();

            // --- Datos al modelo ---
            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            model.addAttribute("reservasHoy", reservasHoy);
            model.addAttribute("reservasSemana", reservasSemanaActivas);
            model.addAttribute("proximaReserva", proximaReserva);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el panel: " + e.getMessage());
        }
        return "barbero/panel";
    }
}
