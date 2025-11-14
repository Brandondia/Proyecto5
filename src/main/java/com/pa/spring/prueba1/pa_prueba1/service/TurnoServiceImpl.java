package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.TurnoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TurnoServiceImpl implements TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private BarberoRepository barberoRepository;

    // ===== MÉTODOS BÁSICOS =====

    @Override
    public List<Turno> obtenerTodos() {
        return turnoRepository.findAll();
    }

    @Override
    public Turno obtenerPorId(Long id) {
        return turnoRepository.findById(id).orElse(null);
    }

    @Override
    public Turno guardarTurno(Turno turno) {
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional
    public void eliminarTurno(Long id) {
        turnoRepository.deleteById(id);
    }

    // ===== MÉTODOS POR BARBERO =====

    @Override
    public List<Turno> obtenerTurnosPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarbero(idBarbero);
    }

    @Override
    public List<Turno> obtenerPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarbero(idBarbero);
    }

    @Override
    public List<Turno> obtenerTurnosDisponiblesPorBarbero(Long idBarbero) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusMonths(1);
        
        return turnoRepository.findByBarberoIdBarberoAndEstadoAndFechaHoraBetween(
            idBarbero, 
            Turno.EstadoTurno.DISPONIBLE, 
            ahora, 
            limite
        );
    }

    @Override
    public List<Turno> obtenerTurnosNoDisponiblesPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarberoAndEstado(
            idBarbero, 
            Turno.EstadoTurno.NO_DISPONIBLE
        );
    }

    @Override
    public List<Turno> obtenerTurnosPorBarberoYEstado(Long idBarbero, Turno.EstadoTurno estado) {
        return turnoRepository.findByBarberoIdBarberoAndEstado(idBarbero, estado);
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

    // ===== MÉTODO PARA MANEJAR DIFERENTES DURACIONES =====

    @Override
    public List<Turno> obtenerTurnosDisponiblesPorDuracionYBarbero(Long barberoId, Integer duracionMinutos) {
        Barbero barbero = barberoRepository.findById(barberoId).orElse(null);
        if (barbero == null) {
            System.out.println("❌ Barbero no encontrado");
            return new ArrayList<>();
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusMonths(1);
        
        List<Turno> todosLosTurnos = turnoRepository
            .findByBarberoIdBarberoAndFechaHoraBetweenOrderByFechaHoraAsc(barberoId, ahora, limite);
        
        List<Turno> turnosDisponibles = todosLosTurnos.stream()
            .filter(t -> t.getEstado() == Turno.EstadoTurno.DISPONIBLE)
            .collect(Collectors.toList());
        
        System.out.println("=== FILTRANDO TURNOS POR DURACIÓN ===");
        System.out.println("Barbero: " + barbero.getNombre());
        System.out.println("Hora fin barbero: " + barbero.getHoraFin());
        if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
            System.out.println("Almuerzo: " + barbero.getHoraInicioAlmuerzo() + " - " + barbero.getHoraFinAlmuerzo());
        }
        System.out.println("Duración solicitada: " + duracionMinutos + " minutos");
        System.out.println("Total turnos disponibles: " + turnosDisponibles.size());
        
        // Para servicios de 15 minutos o menos
        if (duracionMinutos <= 15) {
            List<Turno> turnosValidos = turnosDisponibles.stream()
                .filter(t -> {
                    LocalTime horaInicio = t.getFechaHora().toLocalTime();
                    LocalTime horaFinServicio = horaInicio.plusMinutes(duracionMinutos);
                    
                    // Validar que no exceda el horario de fin
                    if (horaFinServicio.isAfter(barbero.getHoraFin())) {
                        return false;
                    }
                    
                    // Validar que no cruce con el almuerzo
                    if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
                        boolean cruzaConAlmuerzo = 
                            (horaInicio.isBefore(barbero.getHoraFinAlmuerzo()) && 
                             horaFinServicio.isAfter(barbero.getHoraInicioAlmuerzo()));
                        
                        if (cruzaConAlmuerzo) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
            System.out.println("Turnos válidos (dentro del horario): " + turnosValidos.size());
            System.out.println("=====================================");
            return turnosValidos;
        }
        
        // Para servicios de más de 15 minutos
        List<Turno> turnosValidos = new ArrayList<>();
        int turnosNecesarios = (int) Math.ceil(duracionMinutos / 15.0);
        
        System.out.println("Turnos consecutivos necesarios: " + turnosNecesarios);
        
        for (int i = 0; i <= turnosDisponibles.size() - turnosNecesarios; i++) {
            Turno turnoInicial = turnosDisponibles.get(i);
            LocalTime horaInicio = turnoInicial.getFechaHora().toLocalTime();
            LocalTime horaFinServicio = horaInicio.plusMinutes(duracionMinutos);
            
            // ✅ VALIDACIÓN 1: No debe exceder el horario de fin del barbero
            if (horaFinServicio.isAfter(barbero.getHoraFin())) {
                System.out.println("   ⏰ Turno " + turnoInicial.getFechaHora() + 
                    " descartado: terminaría a las " + horaFinServicio + 
                    " (después de " + barbero.getHoraFin() + ")");
                continue;
            }
            
            // ✅ VALIDACIÓN 2: No debe cruzarse con el almuerzo
            if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
                boolean cruzaConAlmuerzo = 
                    (horaInicio.isBefore(barbero.getHoraFinAlmuerzo()) && 
                     horaFinServicio.isAfter(barbero.getHoraInicioAlmuerzo()));
                
                if (cruzaConAlmuerzo) {
                    System.out.println("   🍽️ Turno " + turnoInicial.getFechaHora() + 
                        " descartado: cruza con horario de almuerzo (" + 
                        barbero.getHoraInicioAlmuerzo() + " - " + 
                        barbero.getHoraFinAlmuerzo() + ")");
                    continue;
                }
            }
            
            // ✅ VALIDACIÓN 3: Verificar que todos los turnos consecutivos estén disponibles
            boolean tieneEspacioConsecutivo = true;
            
            for (int j = 1; j < turnosNecesarios; j++) {
                if (i + j >= turnosDisponibles.size()) {
                    tieneEspacioConsecutivo = false;
                    break;
                }
                
                Turno turnoSiguiente = turnosDisponibles.get(i + j);
                LocalDateTime horaEsperada = turnoInicial.getFechaHora().plusMinutes(15 * j);
                
                if (!turnoSiguiente.getFechaHora().equals(horaEsperada)) {
                    tieneEspacioConsecutivo = false;
                    break;
                }
            }
            
            if (tieneEspacioConsecutivo) {
                turnosValidos.add(turnoInicial);
                System.out.println("   ✓ Turno válido: " + turnoInicial.getFechaHora() + 
                    " (termina a las " + horaFinServicio + ")");
            }
        }
        
        System.out.println("Total turnos válidos: " + turnosValidos.size());
        System.out.println("=====================================");
        
        return turnosValidos;
    }

    // ===== MÉTODOS POR ESTADO =====

    @Override
    public List<Turno> obtenerTurnosDisponibles() {
        return turnoRepository.findByEstado(Turno.EstadoTurno.DISPONIBLE);
    }

    @Override
    public List<Turno> obtenerTurnosNoDisponibles() {
        return turnoRepository.findByEstado(Turno.EstadoTurno.NO_DISPONIBLE);
    }

    // ===== MÉTODOS POR RANGO DE FECHAS =====

    @Override
    public List<Turno> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return turnoRepository.findByFechaHoraBetween(inicio, fin);
    }

    @Override
    public List<Turno> obtenerTurnosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return turnoRepository.findByFechaHoraBetweenDates(inicio, fin);
    }

    @Override
    public List<Turno> obtenerTurnosPorBarberoYRangoFechas(Long barberoId, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return turnoRepository.findByBarberoAndFechaHoraBetweenDates(barberoId, inicio, fin);
    }

    @Override
    public List<Turno> obtenerTurnosPorEstadoYRangoFechas(Turno.EstadoTurno estado, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return turnoRepository.findByEstadoAndFechaHoraBetweenDates(estado, inicio, fin);
    }

    @Override
    public List<Turno> obtenerTurnosPorBarberoYEstadoYRangoFechas(Long barberoId, Turno.EstadoTurno estado, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return turnoRepository.findByBarberoAndEstadoAndFechaHoraBetweenDates(barberoId, estado, inicio, fin);
    }

    // ===== MÉTODOS PARA CAMBIAR ESTADO =====

    @Override
    public boolean esTurnoDisponible(Long idTurno) {
        Optional<Turno> turno = turnoRepository.findById(idTurno);
        return turno.isPresent() && turno.get().getEstado() == Turno.EstadoTurno.DISPONIBLE;
    }

    @Override
    @Transactional
    public Turno cambiarEstadoTurno(Long idTurno, Turno.EstadoTurno nuevoEstado) {
        Optional<Turno> optTurno = turnoRepository.findById(idTurno);
        if (!optTurno.isPresent()) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(nuevoEstado);
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional
    public Turno marcarTurnoNoDisponible(Long idTurno) {
        return cambiarEstadoTurno(idTurno, Turno.EstadoTurno.NO_DISPONIBLE);
    }

    @Override
    @Transactional
    public Turno marcarTurnoDisponible(Long idTurno) {
        return cambiarEstadoTurno(idTurno, Turno.EstadoTurno.DISPONIBLE);
    }

    @Override
    @Transactional
    public Turno completarTurno(Long idTurno) {
        return cambiarEstadoTurno(idTurno, Turno.EstadoTurno.NO_DISPONIBLE);
    }

    @Override
    @Transactional
    public Turno cancelarReserva(Long idTurno) {
        return cambiarEstadoTurno(idTurno, Turno.EstadoTurno.DISPONIBLE);
    }

    // ===== MÉTODOS PARA CREAR Y ACTUALIZAR =====

    @Override
    @Transactional
    public Turno crearTurno(Turno turno) {
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional
    public Turno actualizarTurno(Turno turno) {
        if (!turnoRepository.existsById(turno.getIdTurno())) {
            return null;
        }
        return turnoRepository.save(turno);
    }

    // ===== GENERACIÓN AUTOMÁTICA DE TURNOS =====

    @Override
    @Transactional
    public List<Turno> generarTurnosDisponibles(Barbero barbero, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicioTime = fechaInicio.atStartOfDay();
        LocalDateTime finTime = fechaFin.atTime(23, 59, 59);
        return generarTurnosAutomaticos(barbero.getIdBarbero(), inicioTime, finTime);
    }

    @Override
    @Transactional
    public List<Turno> generarTurnosAutomaticos(Long idBarbero, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Optional<Barbero> optBarbero = barberoRepository.findById(idBarbero);
        if (!optBarbero.isPresent()) {
            System.out.println("Barbero no encontrado con ID: " + idBarbero);
            return new ArrayList<>();
        }
        
        Barbero barbero = optBarbero.get();
        List<Turno> turnosGenerados = new ArrayList<>();
        
        LocalDate fechaActual = fechaInicio.toLocalDate();
        LocalDate fechaLimite = fechaFin.toLocalDate();
        
        System.out.println("=== GENERANDO TURNOS AUTOMÁTICOS (15 MIN) ===");
        System.out.println("Barbero: " + barbero.getNombre() + " (ID: " + idBarbero + ")");
        System.out.println("Periodo: " + fechaActual + " a " + fechaLimite);
        System.out.println("Horario: " + barbero.getHoraInicio() + " - " + barbero.getHoraFin());
        if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
            System.out.println("Almuerzo: " + barbero.getHoraInicioAlmuerzo() + " - " + barbero.getHoraFinAlmuerzo());
        }
        
        while (!fechaActual.isAfter(fechaLimite)) {
            if (barbero.getDiaLibre() != null && 
                barbero.getDiaLibre().equals(fechaActual.getDayOfWeek())) {
                System.out.println("Saltando día libre: " + fechaActual + " (" + barbero.getDiaLibre() + ")");
                fechaActual = fechaActual.plusDays(1);
                continue;
            }
            
            if (fechaActual.getDayOfWeek() == DayOfWeek.SUNDAY) {
                System.out.println("Saltando domingo: " + fechaActual);
                fechaActual = fechaActual.plusDays(1);
                continue;
            }
            
            LocalTime horaActual = barbero.getHoraInicio();
            LocalTime horaFin = barbero.getHoraFin();
            LocalTime horaInicioAlmuerzo = barbero.getHoraInicioAlmuerzo();
            LocalTime horaFinAlmuerzo = barbero.getHoraFinAlmuerzo();
            
            int turnosDelDia = 0;
            
            while (horaActual.isBefore(horaFin)) {
                if (horaInicioAlmuerzo != null && horaFinAlmuerzo != null &&
                    !horaActual.isBefore(horaInicioAlmuerzo) && horaActual.isBefore(horaFinAlmuerzo)) {
                    horaActual = horaFinAlmuerzo;
                    continue;
                }
                
                LocalDateTime fechaHoraTurno = LocalDateTime.of(fechaActual, horaActual);
                
                if (fechaHoraTurno.isAfter(LocalDateTime.now())) {
                    boolean exists = turnoRepository.existsByBarberoIdBarberoAndFechaHora(
                        idBarbero, fechaHoraTurno
                    );
                    
                    if (!exists) {
                        Turno turno = new Turno();
                        turno.setBarbero(barbero);
                        turno.setFechaHora(fechaHoraTurno);
                        turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                        
                        Turno turnoGuardado = turnoRepository.save(turno);
                        turnosGenerados.add(turnoGuardado);
                        turnosDelDia++;
                    }
                }
                
                // 🔥 CRÍTICO: Generar turnos cada 15 minutos
                horaActual = horaActual.plusMinutes(15);
            }
            
            if (turnosDelDia > 0) {
                System.out.println("Día " + fechaActual + ": " + turnosDelDia + " turnos generados");
            }
            
            fechaActual = fechaActual.plusDays(1);
        }
        
        System.out.println("Total turnos generados: " + turnosGenerados.size());
        System.out.println("==============================================");
        
        return turnosGenerados;
    }

    // ===== MÉTODOS ADICIONALES =====

    @Override
    public List<Turno> listarTurnosPorBarbero(Long idBarbero) {
        return turnoRepository.findByBarberoIdBarbero(idBarbero);
    }
}