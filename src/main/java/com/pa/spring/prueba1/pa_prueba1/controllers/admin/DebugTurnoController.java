package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugTurnoController {

    @Autowired
    private TurnoService turnoService;

    @GetMapping("/turnos-info")
    public Map<String, Object> getTurnosInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Fecha de hoy
        LocalDate hoy = LocalDate.now();
        info.put("fechaHoy", hoy.toString());
        
        // Calcular inicio y fin de semana
        LocalDate fechaInicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fechaFin = fechaInicio.plusDays(6);
        info.put("inicioSemana", fechaInicio.toString());
        info.put("finSemana", fechaFin.toString());
        
        // Total de turnos en BD
        List<Turno> todosTurnos = turnoService.obtenerTodos();
        info.put("totalTurnosEnBD", todosTurnos.size());
        
        // Turnos de la semana
        List<Turno> turnosSemana = turnoService.obtenerTurnosPorRangoFechas(fechaInicio, fechaFin);
        info.put("turnosSemana", turnosSemana.size());
        
        // Mostrar algunos ejemplos de fechas de turnos
        info.put("ejemplosFechas", todosTurnos.stream()
            .limit(10)
            .map(t -> Map.of(
                "id", t.getIdTurno(),
                "fechaHora", t.getFechaHora().toString(),
                "fecha", t.getFechaHora().toLocalDate().toString(),
                "barbero", t.getBarbero().getNombre(),
                "estado", t.getEstado().toString()
            ))
            .toList());
        
        return info;
    }
    
    @GetMapping("/test-query")
    public Map<String, Object> testQuery() {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fechaFin = fechaInicio.plusDays(6);
        
        try {
            List<Turno> turnos = turnoService.obtenerTurnosPorRangoFechas(fechaInicio, fechaFin);
            result.put("success", true);
            result.put("cantidad", turnos.size());
            result.put("fechaInicio", fechaInicio.toString());
            result.put("fechaFin", fechaFin.toString());
            result.put("turnos", turnos.stream()
                .map(t -> Map.of(
                    "id", t.getIdTurno(),
                    "fechaHora", t.getFechaHora().toString(),
                    "barbero", t.getBarbero().getNombre(),
                    "estado", t.getEstado().toString()
                ))
                .toList());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getClass().getName());
        }
        
        return result;
    }
}