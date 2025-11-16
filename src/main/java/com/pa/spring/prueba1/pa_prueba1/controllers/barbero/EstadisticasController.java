package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

@Controller
@RequestMapping("/barbero/estadisticas")
public class EstadisticasController {

    private final BarberoService barberoService;

    public EstadisticasController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    @GetMapping
    public String misEstadisticas(Model model, Authentication auth,
                                  @RequestParam(required = false, defaultValue = "mes") String periodo) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            
            // Calcular fechas según el período
            LocalDateTime fechaInicio = calcularFechaInicio(periodo);
            LocalDateTime fechaFin = LocalDateTime.now();
            
            // Obtener reservas del período
            List<Reserva> reservas = barberoService.obtenerReservasPorRangoFechas(
                barbero.getIdBarbero(), fechaInicio, fechaFin);

            // ========== MÉTRICAS PRINCIPALES ==========
            long totalCortes = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();

            double ingresosGenerados = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .mapToDouble(r -> r.getCorte() != null ? r.getCorte().getPrecio() : 0.0)
                    .sum();

            long clientesAtendidos = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .map(r -> r.getCliente() != null ? r.getCliente().getIdCliente() : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            // Calcular porcentaje de cambio vs período anterior
            LocalDateTime fechaInicioAnterior = calcularFechaInicioAnterior(periodo);
            List<Reserva> reservasAnterior = barberoService.obtenerReservasPorRangoFechas(
                barbero.getIdBarbero(), fechaInicioAnterior, fechaInicio);
            
            long cortesAnterior = reservasAnterior.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();
            
            double cambioCortes = cortesAnterior > 0 ? 
                ((double)(totalCortes - cortesAnterior) / cortesAnterior) * 100 : 0;

            double ingresosAnterior = reservasAnterior.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .mapToDouble(r -> r.getCorte() != null ? r.getCorte().getPrecio() : 0.0)
                    .sum();
            
            double cambioIngresos = ingresosAnterior > 0 ? 
                ((ingresosGenerados - ingresosAnterior) / ingresosAnterior) * 100 : 0;

            // ========== CORTES POR DÍA ==========
            Map<String, Long> cortesPorDia = calcularCortesPorDia(reservas, periodo);

            // ========== SERVICIOS MÁS SOLICITADOS ==========
            Map<String, Long> serviciosSolicitados = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .filter(r -> r.getCorte() != null)
                    .collect(Collectors.groupingBy(
                        r -> r.getCorte().getNombre(),
                        Collectors.counting()
                    ));

            // ========== TOP SERVICIOS CON INGRESOS ==========
            Map<String, Map<String, Object>> topServicios = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .filter(r -> r.getCorte() != null)
                    .collect(Collectors.groupingBy(
                        r -> r.getCorte().getNombre(),
                        Collectors.collectingAndThen(
                            Collectors.toList(),
                            lista -> {
                                Map<String, Object> stats = new HashMap<>();
                                stats.put("cantidad", lista.size());
                                stats.put("ingresos", lista.stream()
                                    .mapToDouble(r -> r.getCorte().getPrecio())
                                    .sum());
                                return stats;
                            }
                        )
                    ))
                    .entrySet().stream()
                    .sorted((e1, e2) -> {
                        int cant1 = (Integer) e1.getValue().get("cantidad");
                        int cant2 = (Integer) e2.getValue().get("cantidad");
                        return Integer.compare(cant2, cant1);
                    })
                    .limit(4)
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                    ));

            // ========== HORARIOS MÁS OCUPADOS ==========
            Map<String, Long> horariosMasOcupados = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA || 
                                 r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                    .collect(Collectors.groupingBy(
                        r -> obtenerRangoHorario(r.getFechaHoraTurno()),
                        Collectors.counting()
                    ));

            // ========== INGRESOS MENSUALES (últimos 6 meses) ==========
            Map<String, Double> ingresosMensuales = calcularIngresosMensuales(barbero.getIdBarbero());

            // ========== OBJETIVOS DEL MES ==========
            int objetivoCortes = 60;
            double objetivoIngresos = 1500000.0;
            double objetivoValoracion = 4.5;
            double valoracionActual = 4.8;

            double progresoCortes = totalCortes > 0 ? ((double) totalCortes / objetivoCortes * 100) : 0;
            double progresoIngresos = ingresosGenerados > 0 ? (ingresosGenerados / objetivoIngresos * 100) : 0;
            double progresoValoracion = (valoracionActual / objetivoValoracion * 100);

            // Limitar progreso a 100% máximo para la visualización
            progresoCortes = Math.min(progresoCortes, 100);
            progresoIngresos = Math.min(progresoIngresos, 100);

            // ========== AGREGAR TODO AL MODEL ==========
            model.addAttribute("nombreBarbero", barbero.getNombre() + " " + barbero.getApellido());
            model.addAttribute("barbero", barbero);
            model.addAttribute("periodo", periodo);
            
            // Métricas principales
            model.addAttribute("totalCortes", totalCortes);
            model.addAttribute("ingresosGenerados", String.format("%.0f", ingresosGenerados));
            model.addAttribute("clientesAtendidos", clientesAtendidos);
            model.addAttribute("valoracionPromedio", valoracionActual);
            
            // Cambios porcentuales
            model.addAttribute("cambioCortes", cambioCortes);
            model.addAttribute("cambioIngresos", cambioIngresos);
            model.addAttribute("cambioCortesPorcentaje", String.format("%.1f%%", Math.abs(cambioCortes)));
            model.addAttribute("cambioIngresosPorcentaje", String.format("%.1f%%", Math.abs(cambioIngresos)));
            
            // Datos para gráficos
            model.addAttribute("cortesPorDiaLabels", new ArrayList<>(cortesPorDia.keySet()));
            model.addAttribute("cortesPorDiaData", new ArrayList<>(cortesPorDia.values()));
            
            model.addAttribute("serviciosLabels", new ArrayList<>(serviciosSolicitados.keySet()));
            model.addAttribute("serviciosData", new ArrayList<>(serviciosSolicitados.values()));
            
            model.addAttribute("horariosLabels", new ArrayList<>(horariosMasOcupados.keySet()));
            model.addAttribute("horariosData", new ArrayList<>(horariosMasOcupados.values()));
            
            model.addAttribute("ingresosMensualesLabels", new ArrayList<>(ingresosMensuales.keySet()));
            model.addAttribute("ingresosMensualesData", new ArrayList<>(ingresosMensuales.values()));
            
            // Top servicios
            model.addAttribute("topServicios", topServicios);
            
            // Objetivos - CORREGIDO: pasar como números, no como Strings
            model.addAttribute("objetivoCortes", objetivoCortes);
            model.addAttribute("objetivoIngresos", objetivoIngresos);
            model.addAttribute("progresoCortes", progresoCortes);  // Ahora es double
            model.addAttribute("progresoIngresos", progresoIngresos);  // Ahora es double
            model.addAttribute("progresoValoracion", progresoValoracion);  // Ahora es double
            model.addAttribute("valoracionActual", valoracionActual);
            model.addAttribute("objetivoValoracion", objetivoValoracion);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
        return "barbero/estadisticas";
    }

    private LocalDateTime calcularFechaInicio(String periodo) {
        LocalDateTime ahora = LocalDateTime.now();
        switch (periodo.toLowerCase()) {
            case "semana":
                return ahora.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            case "mes":
                return ahora.withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "trimestre":
                int mesActual = ahora.getMonthValue();
                int mesInicio = ((mesActual - 1) / 3) * 3 + 1;
                return ahora.withMonth(mesInicio).withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "anio":
                return ahora.withDayOfYear(1).toLocalDate().atStartOfDay();
            default:
                return ahora.withDayOfMonth(1).toLocalDate().atStartOfDay();
        }
    }

    private LocalDateTime calcularFechaInicioAnterior(String periodo) {
        LocalDateTime fechaInicio = calcularFechaInicio(periodo);
        switch (periodo.toLowerCase()) {
            case "semana":
                return fechaInicio.minusWeeks(1);
            case "mes":
                return fechaInicio.minusMonths(1);
            case "trimestre":
                return fechaInicio.minusMonths(3);
            case "anio":
                return fechaInicio.minusYears(1);
            default:
                return fechaInicio.minusMonths(1);
        }
    }

    private Map<String, Long> calcularCortesPorDia(List<Reserva> reservas, String periodo) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        
        if (periodo.equals("semana")) {
            DayOfWeek[] dias = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
            
            for (DayOfWeek dia : dias) {
                String nombreDia = dia.getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                long count = reservas.stream()
                        .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                        .filter(r -> r.getFechaHoraTurno().getDayOfWeek() == dia)
                        .count();
                resultado.put(nombreDia, count);
            }
        } else {
            resultado = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .collect(Collectors.groupingBy(
                        r -> r.getFechaHoraTurno().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()
                    ));
        }
        
        return resultado;
    }

    private String obtenerRangoHorario(LocalDateTime fechaHora) {
        int hora = fechaHora.getHour();
        if (hora >= 8 && hora < 10) return "8-10am";
        if (hora >= 10 && hora < 12) return "10-12pm";
        if (hora >= 12 && hora < 14) return "12-2pm";
        if (hora >= 14 && hora < 16) return "2-4pm";
        if (hora >= 16 && hora < 18) return "4-6pm";
        if (hora >= 18 && hora < 20) return "6-8pm";
        return "Otro";
    }

    private Map<String, Double> calcularIngresosMensuales(Long idBarbero) {
        Map<String, Double> ingresos = new LinkedHashMap<>();
        LocalDate ahora = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = ahora.minusMonths(i);
            LocalDateTime inicio = mes.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fin = mes.withDayOfMonth(mes.lengthOfMonth()).atTime(23, 59, 59);
            
            List<Reserva> reservasMes = barberoService.obtenerReservasPorRangoFechas(idBarbero, inicio, fin);
            
            double ingresoMes = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .filter(r -> r.getCorte() != null)
                    .mapToDouble(r -> r.getCorte().getPrecio())
                    .sum();
            
            String nombreMes = mes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            ingresos.put(nombreMes, ingresoMes);
        }
        
        return ingresos;
    }
}