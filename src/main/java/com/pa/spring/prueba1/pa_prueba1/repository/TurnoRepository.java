package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository // Marca esta interfaz como un componente de acceso a datos gestionado por Spring.
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    // Encuentra todos los turnos de un barbero específico por su ID.
    List<Turno> findByBarberoIdBarbero(Long barberoId);
    
    // Encuentra todos los turnos de un barbero específico con un estado determinado.
    List<Turno> findByBarberoIdBarberoAndEstado(Long barberoId, Turno.EstadoTurno estado);
    
    // Encuentra todos los turnos de un barbero específico para una fecha y hora determinadas.
    List<Turno> findByBarberoIdBarberoAndFechaHora(Long barberoId, LocalDateTime fechaHora);
    
    // Encuentra todos los turnos de un barbero específico con un estado determinado y dentro de un rango de fechas.
    List<Turno> findByBarberoIdBarberoAndEstadoAndFechaHoraBetween(
        Long barberoId, 
        Turno.EstadoTurno estado, 
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin
    );
    
    // Encuentra todos los turnos con un estado determinado.
    List<Turno> findByEstado(Turno.EstadoTurno estado);
    
    // Encuentra todos los turnos dentro de un rango de fechas.
    List<Turno> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
