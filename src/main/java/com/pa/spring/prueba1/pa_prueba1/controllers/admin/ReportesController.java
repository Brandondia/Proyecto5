package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/reportes")
public class ReportesController {

    private final ReservaRepository reservaRepository;

    public ReportesController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @GetMapping
public String reportesSemanal(Model model) {
    LocalDate hoy = LocalDate.now();
    LocalDate inicioSemana = hoy.with(DayOfWeek.MONDAY);
    LocalDate finSemana = hoy.with(DayOfWeek.SUNDAY);

    LocalDateTime inicioSemanaTime = inicioSemana.atStartOfDay();
    LocalDateTime finSemanaTime = finSemana.atTime(23, 59, 59);

    // DEBUG: Imprime las fechas que estás usando
    System.out.println("=== DEBUG REPORTES ===");
    System.out.println("Hoy: " + hoy);
    System.out.println("Inicio semana: " + inicioSemanaTime);
    System.out.println("Fin semana: " + finSemanaTime);

    // Obtén TODAS las reservas primero para verificar
    List<Reserva> todasLasReservas = reservaRepository.findAll();
    System.out.println("Total de reservas en BD: " + todasLasReservas.size());
    
    // Imprime las fechas de cada reserva SIN usar getId()
    todasLasReservas.forEach(r -> 
        System.out.println("Reserva - Fecha: " + r.getFechaHoraTurno() + " - Estado: " + r.getEstado())
    );

    List<Reserva> reservasSemana =
            reservaRepository.findByFechaHoraTurnoBetween(inicioSemanaTime, finSemanaTime);

    System.out.println("Reservas encontradas en el rango: " + reservasSemana.size());
    System.out.println("======================");

    long totalReservas = reservasSemana.size();
    long reservasCompletadas = reservasSemana.stream()
            .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
            .count();

    double ingresoTotal = reservasSemana.stream()
            .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA && r.getCorte() != null)
            .mapToDouble(r -> r.getCorte().getPrecio())
            .sum();

    long tiempoTotalMinutos = reservasCompletadas * 30;
    long horas = tiempoTotalMinutos / 60;
    long minutos = tiempoTotalMinutos % 60;

    model.addAttribute("inicioSemana", inicioSemana);
    model.addAttribute("finSemana", finSemana);
    model.addAttribute("totalReservas", totalReservas);
    model.addAttribute("reservasCompletadas", reservasCompletadas);
    model.addAttribute("ingresoTotal", ingresoTotal);
    model.addAttribute("tiempoHoras", horas);
    model.addAttribute("tiempoMinutos", minutos);
    model.addAttribute("reservas", reservasSemana);

    return "admin/reportes";
}
}
