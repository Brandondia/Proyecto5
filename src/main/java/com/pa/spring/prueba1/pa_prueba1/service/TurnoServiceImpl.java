package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class TurnoServiceImpl implements TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;
    
    @Autowired
    private ReservaService reservaService;

    @Override
    public List<Turno> obtenerTurnosPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarbero(idBarbero);
    }
    
    @Override
    public List<Turno> obtenerTurnosPorBarberoYEstado(Long idBarbero, Turno.EstadoTurno estado) {
        return turnoRepository.findByBarberoIdBarberoAndEstado(idBarbero, estado);
    }
    
    @Override
    public List<Turno> obtenerTurnosDisponiblesPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarberoAndEstado(idBarbero, Turno.EstadoTurno.DISPONIBLE);
    }
    
    @Override
    public List<Turno> obtenerTurnosNoDisponiblesPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarberoAndEstado(idBarbero, Turno.EstadoTurno.NO_DISPONIBLE);
    }
    
    @Override
    public List<Turno> obtenerTurnosDisponibles() {
        return turnoRepository.findByEstado(Turno.EstadoTurno.DISPONIBLE);
    }
    
    @Override
    public List<Turno> obtenerTurnosNoDisponibles() {
        return turnoRepository.findByEstado(Turno.EstadoTurno.NO_DISPONIBLE);
    }

    @Override
    public Turno guardarTurno(Turno turno) {
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional
    public void eliminarTurno(Long id) {
        // Verificar si hay reservas para este turno
        if (reservaService.existeReservaParaTurno(id)) {
            throw new IllegalStateException("No se puede eliminar un turno con reservas pendientes");
        }
        turnoRepository.deleteById(id);
    }
    
    @Override
    public Turno obtenerPorId(Long id) {
        return turnoRepository.findById(id).orElse(null);
    }
    
    @Override
    @Transactional
    public List<Turno> generarTurnosDisponibles(Barbero barbero, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Turno> turnosGenerados = new ArrayList<>();
        
        if (barbero == null) {
            System.out.println("Error: Barbero es null");
            return turnosGenerados;
        }
        
        System.out.println("Generando turnos para barbero: " + barbero.getNombre() + 
                          " desde " + fechaInicio + " hasta " + fechaFin);
        
        try {
            // Eliminar turnos disponibles existentes en ese rango de fechas
            List<Turno> turnosExistentes = turnoRepository.findByBarberoIdBarberoAndEstadoAndFechaHoraBetween(
                barbero.getIdBarbero(), 
                Turno.EstadoTurno.DISPONIBLE,
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59)
            );
        
            System.out.println("Turnos existentes encontrados: " + turnosExistentes.size());
        
            // Crear un mapa de fechas y horas existentes para no duplicar
            List<LocalDateTime> fechasHorasExistentes = turnosExistentes.stream()
                .map(Turno::getFechaHora)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
            // Generar nuevos turnos para cada día en el rango
            LocalDate fechaActual = fechaInicio;
            while (!fechaActual.isAfter(fechaFin)) {
                // Verificar si no es el día libre del barbero
                if (barbero.getDiaLibre() == null || fechaActual.getDayOfWeek() != barbero.getDiaLibre()) {
                    // Generar turnos para este día
                    LocalTime horaInicio = barbero.getHoraInicio();
                    LocalTime horaFin = barbero.getHoraFin();
                    LocalTime horaInicioAlmuerzo = barbero.getHoraInicioAlmuerzo();
                    LocalTime horaFinAlmuerzo = barbero.getHoraFinAlmuerzo();
                    Integer duracionTurno = barbero.getDuracionTurno();
                
                    // Verificar que todos los valores necesarios estén presentes
                    if (horaInicio == null || horaFin == null || duracionTurno == null) {
                        System.out.println("Error: Faltan datos de horario para el barbero " + barbero.getNombre());
                        fechaActual = fechaActual.plusDays(1);
                        continue;
                    }
                
                    LocalTime horaActual = horaInicio;
                
                    while (!horaActual.isAfter(horaFin.minusMinutes(duracionTurno))) {
                        // Verificar si no está en horario de almuerzo
                        if (horaInicioAlmuerzo == null || horaFinAlmuerzo == null ||
                            horaActual.isBefore(horaInicioAlmuerzo) || 
                            !horaActual.isBefore(horaFinAlmuerzo)) {
                        
                            LocalDateTime fechaHoraTurno = LocalDateTime.of(fechaActual, horaActual);
                        
                            // Verificar si no hay un turno ya existente en esta fecha y hora
                            if (!fechasHorasExistentes.contains(fechaHoraTurno) &&
                                turnoRepository.findByBarberoIdBarberoAndFechaHora(barbero.getIdBarbero(), fechaHoraTurno).isEmpty()) {
                                try {
                                    Turno nuevoTurno = new Turno();
                                    nuevoTurno.setFechaHora(fechaHoraTurno);
                                    nuevoTurno.setBarbero(barbero);
                                    nuevoTurno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                                
                                    Turno turnoGuardado = turnoRepository.save(nuevoTurno);
                                    turnosGenerados.add(turnoGuardado);
                                } catch (Exception e) {
                                    System.out.println("Error al guardar turno para " + fechaHoraTurno + ": " + e.getMessage());
                                }
                            }
                        }
                    
                        // Avanzar al siguiente horario
                        horaActual = horaActual.plusMinutes(duracionTurno);
                    }
                }
            
                // Avanzar al siguiente día
                fechaActual = fechaActual.plusDays(1);
            }
        
            System.out.println("Turnos generados exitosamente: " + turnosGenerados.size());
        
        } catch (Exception e) {
            System.out.println("Error general al generar turnos: " + e.getMessage());
            e.printStackTrace();
        }
    
        return turnosGenerados;
    }
    
    @Override
    public boolean esTurnoDisponible(Long idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(idTurno);
        return optTurno.isPresent() && optTurno.get().getEstado() == Turno.EstadoTurno.DISPONIBLE;
    }
    
    @Override
    @Transactional
    public Turno marcarTurnoNoDisponible(Long idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(idTurno);
        if (!optTurno.isPresent()) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(Turno.EstadoTurno.NO_DISPONIBLE);
        
        return turnoRepository.save(turno);
    }
    
    @Override
    @Transactional
    public Turno marcarTurnoDisponible(Long idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(idTurno);
        if (!optTurno.isPresent()) {
            return null;
        }
        
        // Verificar si hay reservas para este turno
        if (reservaService.existeReservaParaTurno(idTurno)) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
        
        return turnoRepository.save(turno);
    }
    
    @Override
    public List<Turno> obtenerTodos() {
        return turnoRepository.findAll();
    }
    
    @Override
    @Transactional
    public Turno completarTurno(Long idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(idTurno);
        if (!optTurno.isPresent()) {
            return null;
        }
        
        // No es necesario cambiar el estado del turno, ya que seguirá siendo NO_DISPONIBLE
        // Solo se cambia el estado de la reserva asociada
        
        return optTurno.get();
    }
    
    @Override
    @Transactional
    public Turno cancelarReserva(Long idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(idTurno);
        if (!optTurno.isPresent()) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
        
        return turnoRepository.save(turno);
    }
    
    @Override
    public List<Turno> obtenerTurnosDisponiblesPorBarberoYFecha(Long idBarbero, LocalDate fecha) {
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        
        return turnoRepository.findByBarberoIdBarberoAndEstadoAndFechaHoraBetween(
            idBarbero, 
            Turno.EstadoTurno.DISPONIBLE,
            inicioDelDia,
            finDelDia
        );
    }
}
