package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/barbero/agenda")
public class AgendaController {

    private final BarberoService barberoService;

    public AgendaController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    @GetMapping
    public String agenda(Model model, Authentication auth,
                         @RequestParam(required = false) String semana) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);

            LocalDate inicioSemana = (semana != null && !semana.isEmpty())
                    ? LocalDate.parse(semana)
                    : LocalDate.now().with(java.time.DayOfWeek.MONDAY);

            LocalDate finSemana = inicioSemana.plusDays(6);

            List<Reserva> reservasSemana = barberoService.obtenerReservasSemanaActual(barbero.getIdBarbero())
                    .stream()
                    .filter(r -> !r.getFechaHoraTurno().isBefore(inicioSemana.atStartOfDay())
                              && !r.getFechaHoraTurno().isAfter(finSemana.atTime(23, 59)))
                    .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
                    .toList();

            Map<LocalDate, List<Reserva>> reservasPorDia = reservasSemana.stream()
                    .collect(Collectors.groupingBy(r -> r.getFechaHoraTurno().toLocalDate()));

            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            model.addAttribute("inicioSemana", inicioSemana);
            model.addAttribute("finSemana", finSemana);
            model.addAttribute("reservasPorDia", reservasPorDia);
            model.addAttribute("semanaAnterior", inicioSemana.minusWeeks(1));
            model.addAttribute("semanaSiguiente", inicioSemana.plusWeeks(1));

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar agenda: " + e.getMessage());
        }
        return "barbero/agenda";
    }
}
