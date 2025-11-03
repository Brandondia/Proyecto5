package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // --- EXISTENTES ---
    List<Turno> findByBarberoIdBarbero(Long barberoId);
    List<Turno> findByBarberoIdBarberoAndEstado(Long barberoId, Turno.EstadoTurno estado);
    List<Turno> findByBarberoIdBarberoAndFechaHora(Long barberoId, LocalDateTime fechaHora);
    List<Turno> findByBarberoIdBarberoAndEstadoAndFechaHoraBetween(
            Long barberoId,
            Turno.EstadoTurno estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
    List<Turno> findByEstado(Turno.EstadoTurno estado);
    List<Turno> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);


    // --- NUEVOS MÉTODOS CORREGIDOS ---

    // Todos los turnos con sus reservas y los clientes de esas reservas
    @Query("SELECT DISTINCT t FROM Turno t " +
           "LEFT JOIN FETCH t.reservas r " +
           "LEFT JOIN FETCH r.cliente " +
           "LEFT JOIN FETCH t.barbero")
    List<Turno> findAllConReservasYClientes();

    // Turnos de un barbero con reservas + cliente incluido
    @Query("SELECT DISTINCT t FROM Turno t " +
           "LEFT JOIN FETCH t.reservas r " +
           "LEFT JOIN FETCH r.cliente " +
           "WHERE t.barbero.idBarbero = :barberoId")
    List<Turno> findByBarberoConReservasYClientes(Long barberoId);
}



