package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TurnoService {
    List<Turno> obtenerTurnosPorBarbero(Long idBarbero);
    List<Turno> obtenerTurnosDisponiblesPorBarbero(Long idBarbero);
    List<Turno> obtenerTurnosNoDisponiblesPorBarbero(Long idBarbero);
    List<Turno> obtenerTurnosPorBarberoYEstado(Long idBarbero, Turno.EstadoTurno estado);
    List<Turno> obtenerTurnosDisponibles();
    List<Turno> obtenerTurnosNoDisponibles();
    Turno guardarTurno(Turno turno);
    void eliminarTurno(Long id);
    Turno obtenerPorId(Long id);
    
    // Método para generar turnos disponibles para un barbero en un rango de fechas
    List<Turno> generarTurnosDisponibles(Barbero barbero, LocalDate fechaInicio, LocalDate fechaFin);
    
    // Método para verificar si un turno está disponible
    boolean esTurnoDisponible(Long idTurno);
    
    // Método para marcar un turno como no disponible
    Turno marcarTurnoNoDisponible(Long idTurno);
    
    // Método para marcar un turno como disponible
    Turno marcarTurnoDisponible(Long idTurno);
    
    // Método para obtener todos los turnos
    List<Turno> obtenerTodos();
    
    // Método para completar un turno (usado cuando se completa una reserva)
    Turno completarTurno(Long idTurno);
    
    // Método para cancelar una reserva (liberar el turno)
    Turno cancelarReserva(Long idTurno);
    
    // Método para obtener turnos disponibles por barbero y fecha
    List<Turno> obtenerTurnosDisponiblesPorBarberoYFecha(Long idBarbero, LocalDate fecha);
}
