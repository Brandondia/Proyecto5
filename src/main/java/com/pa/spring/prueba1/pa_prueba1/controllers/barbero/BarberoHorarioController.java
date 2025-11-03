package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador del módulo "Mi Horario" para el Barbero
 */
@Controller
@RequestMapping("/barbero")
public class BarberoHorarioController {

    private final BarberoService barberoService;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private SolicitudAusenciaRepository solicitudAusenciaRepository;

    public BarberoHorarioController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    /**
     * Obtiene el barbero autenticado según el email
     */
    private Barbero obtenerBarberoActual(Authentication authentication) {
        String email = authentication.getName();
        return barberoService.obtenerBarberoPorEmail(email);
    }

    /**
     * MI HORARIO - Vista principal del calendario
     * Permite filtrar por mes (formato: yyyy-MM)
     */
    @GetMapping("/horario")
    public String miHorario(Model model,
                            Authentication authentication,
                            @RequestParam(required = false) String mes,
                            @RequestParam(required = false, defaultValue = "calendario") String vista) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            // Determinar el mes a mostrar
            LocalDate fechaSeleccionada = (mes != null && !mes.isEmpty()) 
                    ? LocalDate.parse(mes + "-01") 
                    : LocalDate.now();
            
            LocalDate primerDiaMes = fechaSeleccionada.withDayOfMonth(1);
            LocalDate ultimoDiaMes = fechaSeleccionada.withDayOfMonth(fechaSeleccionada.lengthOfMonth());
            
            // Obtener todas las reservas del mes
            LocalDateTime inicioMes = primerDiaMes.atStartOfDay();
            LocalDateTime finMes = ultimoDiaMes.atTime(23, 59, 59);
            
            List<Reserva> reservasMes = reservaRepository
                    .findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                            barbero.getIdBarbero(), inicioMes, finMes);
            
            // Agrupar reservas por día
            Map<LocalDate, List<Reserva>> reservasPorDia = reservasMes.stream()
                    .collect(Collectors.groupingBy(r -> r.getFechaHoraTurno().toLocalDate()));
            
            // Calcular estadísticas del mes
            long totalReservasMes = reservasMes.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();
            
            // Calcular días de trabajo en el mes (excluyendo día libre)
            long diasTrabajo = primerDiaMes.datesUntil(ultimoDiaMes.plusDays(1))
                    .filter(fecha -> fecha.getDayOfWeek() != barbero.getDiaLibre())
                    .count();
            
            // Calcular horas semanales
            int horasSemanales = 0;
            if (barbero.getHoraInicio() != null && barbero.getHoraFin() != null) {
                int horasDiarias = barbero.getHoraFin().getHour() - barbero.getHoraInicio().getHour();
                if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
                    horasDiarias -= (barbero.getHoraFinAlmuerzo().getHour() - barbero.getHoraInicioAlmuerzo().getHour());
                }
                horasSemanales = horasDiarias * 6; // 6 días a la semana
            }
            
            // Calcular disponibilidad (slots ocupados vs disponibles)
            double disponibilidad = 0;
            if (barbero.getHoraInicio() != null && barbero.getHoraFin() != null && diasTrabajo > 0) {
                int slotsDisponiblesPorDia = barbero.getHoraFin().getHour() - barbero.getHoraInicio().getHour();
                int totalSlotsDisponibles = (int) (slotsDisponiblesPorDia * diasTrabajo * 2); // 2 slots por hora
                if (totalSlotsDisponibles > 0) {
                    disponibilidad = Math.min(100, ((double) totalReservasMes / totalSlotsDisponibles) * 100);
                }
            }
            
            // Contar días libres/ausencias en el mes
            List<SolicitudAusencia> ausenciasAprobadas = solicitudAusenciaRepository
                    .findByBarberoIdBarberoAndEstado(barbero.getIdBarbero(), 
                            SolicitudAusencia.EstadoSolicitud.APROBADA)
                    .stream()
                    .filter(a -> {
                        if (a.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
                            return !a.getFechaFin().isBefore(primerDiaMes) && 
                                   !a.getFechaInicio().isAfter(ultimoDiaMes);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            
            long diasLibresMes = ausenciasAprobadas.stream()
                    .mapToLong(a -> java.time.temporal.ChronoUnit.DAYS.between(
                            a.getFechaInicio(), a.getFechaFin()) + 1)
                    .sum();
            
            // Crear lista de días del mes para el calendario
            List<Map<String, Object>> diasCalendario = new ArrayList<>();
            
            // Días del mes anterior para completar la primera semana
            LocalDate primerDiaSemana = primerDiaMes.with(java.time.DayOfWeek.MONDAY);
            while (primerDiaSemana.isBefore(primerDiaMes)) {
                Map<String, Object> dia = new HashMap<>();
                dia.put("numero", primerDiaSemana.getDayOfMonth());
                dia.put("fecha", primerDiaSemana);
                dia.put("mesActual", false);
                dia.put("hoy", false);
                dia.put("diaLibre", false);
                dia.put("ausencia", false);
                dia.put("reservas", 0L);
                dia.put("horaInicio", "");
                dia.put("horaFin", "");
                diasCalendario.add(dia);
                primerDiaSemana = primerDiaSemana.plusDays(1);
            }
            
            // Días del mes actual
            LocalDate diaActual = primerDiaMes;
            while (!diaActual.isAfter(ultimoDiaMes)) {
                Map<String, Object> dia = new HashMap<>();
                dia.put("numero", diaActual.getDayOfMonth());
                dia.put("fecha", diaActual);
                dia.put("mesActual", true);
                dia.put("hoy", diaActual.equals(LocalDate.now()));
                
                // Verificar día libre - IMPORTANTE: verificar null primero
                boolean esDiaLibre = false;
                if (barbero.getDiaLibre() != null) {
                    esDiaLibre = diaActual.getDayOfWeek() == barbero.getDiaLibre();
                }
                dia.put("diaLibre", esDiaLibre);
                
                // Verificar si hay ausencia aprobada
                final LocalDate fechaFinal = diaActual;
                boolean tieneAusencia = ausenciasAprobadas.stream()
                        .anyMatch(a -> !fechaFinal.isBefore(a.getFechaInicio()) && 
                                       !fechaFinal.isAfter(a.getFechaFin()));
                dia.put("ausencia", tieneAusencia);
                
                // Contar reservas del día
                List<Reserva> reservasDia = reservasPorDia.getOrDefault(diaActual, new ArrayList<>());
                long reservasActivas = reservasDia.stream()
                        .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                        .count();
                dia.put("reservas", reservasActivas);
                
                // Horario de trabajo - Siempre agregar estas claves
                if (!esDiaLibre && !tieneAusencia) {
                    dia.put("horaInicio", barbero.getHoraInicio() != null ? 
                            barbero.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")) : "08:00");
                    dia.put("horaFin", barbero.getHoraFin() != null ? 
                            barbero.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")) : "18:00");
                } else {
                    // Agregar valores por defecto para evitar errores
                    dia.put("horaInicio", "");
                    dia.put("horaFin", "");
                }
                
                diasCalendario.add(dia);
                diaActual = diaActual.plusDays(1);
            }
            
            // Obtener reservas de la semana actual para vista semanal
            LocalDate inicioSemana = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate finSemana = inicioSemana.plusDays(6);
            
            List<Reserva> reservasSemana = reservaRepository
                    .findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                            barbero.getIdBarbero(), 
                            inicioSemana.atStartOfDay(), 
                            finSemana.atTime(23, 59, 59));
            
            Map<LocalDate, List<Reserva>> reservasPorDiaSemana = reservasSemana.stream()
                    .collect(Collectors.groupingBy(r -> r.getFechaHoraTurno().toLocalDate()));
            
            // Configuración de horario por día de la semana
            List<Map<String, Object>> horarioPorDia = new ArrayList<>();
            for (java.time.DayOfWeek diaSemana : java.time.DayOfWeek.values()) {
                Map<String, Object> config = new HashMap<>();
                config.put("dia", diaSemana.getDisplayName(TextStyle.FULL, 
                        Locale.forLanguageTag("es")));
                
                if (diaSemana == barbero.getDiaLibre()) {
                    config.put("activo", false);
                    config.put("horaInicio", "-");
                    config.put("horaFin", "-");
                    config.put("descanso", "-");
                } else {
                    config.put("activo", true);
                    config.put("horaInicio", barbero.getHoraInicio() != null ? 
                            barbero.getHoraInicio().format(DateTimeFormatter.ofPattern("h:mm a")) : "8:00 AM");
                    config.put("horaFin", barbero.getHoraFin() != null ? 
                            barbero.getHoraFin().format(DateTimeFormatter.ofPattern("h:mm a")) : "6:00 PM");
                    
                    if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
                        config.put("descanso", 
                                barbero.getHoraInicioAlmuerzo().format(DateTimeFormatter.ofPattern("h:mm a")) + 
                                " - " + 
                                barbero.getHoraFinAlmuerzo().format(DateTimeFormatter.ofPattern("h:mm a")));
                    } else {
                        config.put("descanso", "-");
                    }
                }
                
                horarioPorDia.add(config);
            }
            
            // Días de la semana actual para vista detallada
            List<Map<String, Object>> diasSemana = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate fecha = inicioSemana.plusDays(i);
                Map<String, Object> diaInfo = new HashMap<>();
                diaInfo.put("fecha", fecha);
                diaInfo.put("dia", fecha.format(DateTimeFormatter.ofPattern("EEEE", 
                        Locale.forLanguageTag("es"))));
                diaInfo.put("diaNumero", fecha.getDayOfMonth());
                diaInfo.put("mes", fecha.format(DateTimeFormatter.ofPattern("MMMM", 
                        Locale.forLanguageTag("es"))));
                diaInfo.put("reservas", reservasPorDiaSemana.getOrDefault(fecha, new ArrayList<>()));
                diaInfo.put("diaLibre", fecha.getDayOfWeek() == barbero.getDiaLibre());
                diasSemana.add(diaInfo);
            }
            
            // Añadir datos al modelo
            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            model.addAttribute("mesActual", fechaSeleccionada.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es"))));
            model.addAttribute("mesAnterior", fechaSeleccionada.minusMonths(1).format(
                    DateTimeFormatter.ofPattern("yyyy-MM")));
            model.addAttribute("mesSiguiente", fechaSeleccionada.plusMonths(1).format(
                    DateTimeFormatter.ofPattern("yyyy-MM")));
            model.addAttribute("diasCalendario", diasCalendario);
            model.addAttribute("reservasPorDiaSemana", reservasPorDiaSemana);
            model.addAttribute("horarioPorDia", horarioPorDia);
            model.addAttribute("diasSemana", diasSemana);
            
            // Estadísticas
            model.addAttribute("horasSemanales", horasSemanales);
            model.addAttribute("disponibilidad", Math.round(disponibilidad));
            model.addAttribute("diasLibresMes", diasLibresMes);
            model.addAttribute("totalReservasMes", totalReservasMes);
            model.addAttribute("vistaActual", vista);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar horario: " + e.getMessage());
            e.printStackTrace();
        }
        return "barbero/horario";
    }
}