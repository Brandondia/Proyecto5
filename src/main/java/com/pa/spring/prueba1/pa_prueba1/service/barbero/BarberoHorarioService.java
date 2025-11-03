package com.pa.spring.prueba1.pa_prueba1.service.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BarberoHorarioService {

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private SolicitudAusenciaRepository solicitudAusenciaRepository;

    /**
     * Obtiene todas las reservas de un barbero para un mes específico.
     *
     * @param barbero           El barbero autenticado
     * @param fechaSeleccionada Fecha del mes deseado
     * @return Lista de reservas de ese mes
     */
    public List<Reserva> obtenerReservasPorMes(Barbero barbero, LocalDate fechaSeleccionada) {
        // Primer día del mes
        LocalDate inicioMes = fechaSeleccionada.withDayOfMonth(1);

        // Último día del mes
        LocalDate finMes = fechaSeleccionada.with(TemporalAdjusters.lastDayOfMonth());

        // Consulta al repositorio usando el rango de fechas
        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                barbero.getIdBarbero(),
                inicioMes.atStartOfDay(),
                finMes.atTime(23, 59, 59)
        );
    }
    
    /**
     * Obtiene las reservas de un barbero en un rango de fechas específico
     */
    public List<Reserva> obtenerReservasPorRango(Long idBarbero, LocalDateTime inicio, LocalDateTime fin) {
        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(idBarbero, inicio, fin);
    }

    /**
     * Calcula las horas trabajadas en una semana
     */
    public int calcularHorasSemanales(Barbero barbero) {
        if (barbero.getHoraInicio() == null || barbero.getHoraFin() == null) {
            return 0;
        }
        
        int horasDiarias = barbero.getHoraFin().getHour() - barbero.getHoraInicio().getHour();
        
        // Restar tiempo de almuerzo si existe
        if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
            horasDiarias -= (barbero.getHoraFinAlmuerzo().getHour() - barbero.getHoraInicioAlmuerzo().getHour());
        }
        
        // Multiplicar por 6 días (asumiendo que trabaja 6 días a la semana)
        return horasDiarias * 6;
    }

    /**
     * Calcula el porcentaje de disponibilidad basado en reservas
     */
    public double calcularDisponibilidad(Long idBarbero, LocalDate inicio, LocalDate fin, Barbero barbero) {
        LocalDateTime inicioMes = inicio.atStartOfDay();
        LocalDateTime finMes = fin.atTime(23, 59, 59);
        
        List<Reserva> reservas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                idBarbero, inicioMes, finMes);
        
        long reservasActivas = reservas.stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();
        
        if (reservasActivas == 0 || barbero.getHoraInicio() == null || barbero.getHoraFin() == null) {
            return 0.0;
        }
        
        long diasTrabajo = inicio.datesUntil(fin.plusDays(1))
                .filter(fecha -> fecha.getDayOfWeek() != barbero.getDiaLibre())
                .count();
        
        if (diasTrabajo == 0) {
            return 0.0;
        }
        
        int slotsDisponiblesPorDia = barbero.getHoraFin().getHour() - barbero.getHoraInicio().getHour();
        int totalSlotsDisponibles = (int) (slotsDisponiblesPorDia * diasTrabajo * 2); // 2 slots por hora
        
        if (totalSlotsDisponibles == 0) {
            return 0.0;
        }
        
        return Math.min(100.0, ((double) reservasActivas / totalSlotsDisponibles) * 100);
    }

    /**
     * Obtiene los días con ausencia aprobada en un rango de fechas
     */
    public List<LocalDate> obtenerDiasConAusencia(Long idBarbero, LocalDate inicio, LocalDate fin) {
        List<SolicitudAusencia> ausencias = solicitudAusenciaRepository
                .findByBarberoIdBarberoAndEstado(idBarbero, SolicitudAusencia.EstadoSolicitud.APROBADA);
        
        List<LocalDate> diasConAusencia = new ArrayList<>();
        
        for (SolicitudAusencia ausencia : ausencias) {
            if (ausencia.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
                LocalDate fechaAusencia = ausencia.getFechaInicio();
                while (!fechaAusencia.isAfter(ausencia.getFechaFin())) {
                    if (!fechaAusencia.isBefore(inicio) && !fechaAusencia.isAfter(fin)) {
                        diasConAusencia.add(fechaAusencia);
                    }
                    fechaAusencia = fechaAusencia.plusDays(1);
                }
            }
        }
        
        return diasConAusencia;
    }

    /**
     * Genera slots de tiempo disponibles para un día específico
     */
    public List<LocalTime> generarSlotsDisponibles(Barbero barbero, LocalDate fecha) {
        List<LocalTime> slots = new ArrayList<>();
        
        if (barbero.getHoraInicio() == null || barbero.getHoraFin() == null) {
            return slots;
        }
        
        // Verificar que no sea día libre
        if (fecha.getDayOfWeek() == barbero.getDiaLibre()) {
            return slots;
        }
        
        // Obtener reservas del día
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        
        List<Reserva> reservasDia = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                barbero.getIdBarbero(), inicioDia, finDia);
        
        LocalTime horaActual = barbero.getHoraInicio();
        int duracionSlot = barbero.getDuracionTurno() != null ? barbero.getDuracionTurno() : 30;
        
        while (horaActual.isBefore(barbero.getHoraFin())) {
            // Verificar si está en horario de almuerzo
            boolean esHoraAlmuerzo = false;
            if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
                esHoraAlmuerzo = !horaActual.isBefore(barbero.getHoraInicioAlmuerzo()) && 
                               horaActual.isBefore(barbero.getHoraFinAlmuerzo());
            }
            
            if (!esHoraAlmuerzo) {
                // Verificar si hay reserva en este horario
                LocalDateTime fechaHoraSlot = LocalDateTime.of(fecha, horaActual);
                boolean ocupado = reservasDia.stream()
                        .anyMatch(r -> r.getFechaHoraTurno().equals(fechaHoraSlot) && 
                                     r.getEstado() != Reserva.EstadoReserva.CANCELADA);
                
                if (!ocupado) {
                    slots.add(horaActual);
                }
            }
            
            horaActual = horaActual.plusMinutes(duracionSlot);
        }
        
        return slots;
    }
    
    /**
     * Verifica si un barbero está disponible en una fecha específica
     */
    public boolean estaBarberoDisponible(Barbero barbero, LocalDate fecha) {
        // Verificar día libre
        if (fecha.getDayOfWeek() == barbero.getDiaLibre()) {
            return false;
        }
        
        // Verificar ausencias aprobadas
        List<SolicitudAusencia> ausencias = solicitudAusenciaRepository
                .findByBarberoIdBarberoAndEstado(barbero.getIdBarbero(), 
                        SolicitudAusencia.EstadoSolicitud.APROBADA);
        
        for (SolicitudAusencia ausencia : ausencias) {
            if (ausencia.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
                if (!fecha.isBefore(ausencia.getFechaInicio()) && 
                    !fecha.isAfter(ausencia.getFechaFin())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Cuenta el total de reservas activas en un rango de fechas
     */
    public long contarReservasActivas(Long idBarbero, LocalDateTime inicio, LocalDateTime fin) {
        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(idBarbero, inicio, fin)
                .stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();
    }
    
    /**
     * Obtiene las reservas de un día específico
     */
    public List<Reserva> obtenerReservasPorDia(Long idBarbero, LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        
        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                idBarbero, inicioDia, finDia)
                .stream()
                .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
                .collect(Collectors.toList());
    }
}
