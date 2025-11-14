package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TurnoService {
    
    // ===== MÉTODOS BÁSICOS =====
    List<Turno> obtenerTodos();
    
    Turno obtenerPorId(Long id);
    
    Turno guardarTurno(Turno turno);
    
    void eliminarTurno(Long id);
    
    // ===== MÉTODOS POR BARBERO =====
    List<Turno> obtenerTurnosPorBarbero(Long idBarbero);
    
    List<Turno> obtenerPorBarbero(Long idBarbero);
    
    List<Turno> obtenerTurnosDisponiblesPorBarbero(Long idBarbero);
    
    List<Turno> obtenerTurnosNoDisponiblesPorBarbero(Long idBarbero);
    
    List<Turno> obtenerTurnosPorBarberoYEstado(Long idBarbero, Turno.EstadoTurno estado);
    
    List<Turno> obtenerTurnosDisponiblesPorBarberoYFecha(Long idBarbero, LocalDate fecha);
    
    // ===== NUEVO: MÉTODO PARA MANEJAR DIFERENTES DURACIONES =====
    /**
     * Obtiene turnos disponibles considerando la duración del servicio
     * Para servicios de 30 min: devuelve todos los turnos disponibles
     * Para servicios más largos: solo devuelve turnos que tienen suficiente espacio consecutivo
     */
    List<Turno> obtenerTurnosDisponiblesPorDuracionYBarbero(Long barberoId, Integer duracionMinutos);
    
    // ===== MÉTODOS POR ESTADO =====
    List<Turno> obtenerTurnosDisponibles();
    
    List<Turno> obtenerTurnosNoDisponibles();
    
    // ===== MÉTODOS POR RANGO DE FECHAS =====
    List<Turno> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);
    
    List<Turno> obtenerTurnosPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Turno> obtenerTurnosPorBarberoYRangoFechas(Long barberoId, LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Turno> obtenerTurnosPorEstadoYRangoFechas(Turno.EstadoTurno estado, LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Turno> obtenerTurnosPorBarberoYEstadoYRangoFechas(Long barberoId, Turno.EstadoTurno estado, LocalDate fechaInicio, LocalDate fechaFin);
    
    // ===== MÉTODOS PARA CAMBIAR ESTADO =====
    boolean esTurnoDisponible(Long idTurno);
    
    Turno cambiarEstadoTurno(Long idTurno, Turno.EstadoTurno nuevoEstado);
    
    Turno marcarTurnoNoDisponible(Long idTurno);
    
    Turno marcarTurnoDisponible(Long idTurno);
    
    Turno completarTurno(Long idTurno);
    
    Turno cancelarReserva(Long idTurno);
    
    // ===== MÉTODOS PARA CREAR Y ACTUALIZAR =====
    Turno crearTurno(Turno turno);
    
    Turno actualizarTurno(Turno turno);
    
    // ===== GENERACIÓN AUTOMÁTICA DE TURNOS =====
    List<Turno> generarTurnosDisponibles(Barbero barbero, LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Turno> generarTurnosAutomaticos(Long idBarbero, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // ===== MÉTODOS ADICIONALES =====
    List<Turno> listarTurnosPorBarbero(Long idBarbero);
}