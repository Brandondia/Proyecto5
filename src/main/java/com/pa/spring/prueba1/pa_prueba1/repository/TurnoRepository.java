package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
            LocalDateTime fechaFin);

    List<Turno> findByEstado(Turno.EstadoTurno estado);

    List<Turno> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // --- MÉTODOS CON FETCH JOINS ---
    @Query("SELECT DISTINCT t FROM Turno t " +
            "LEFT JOIN FETCH t.reservas r " +
            "LEFT JOIN FETCH r.cliente " +
            "LEFT JOIN FETCH t.barbero")
    List<Turno> findAllConReservasYClientes();

    @Query("SELECT DISTINCT t FROM Turno t " +
            "LEFT JOIN FETCH t.reservas r " +
            "LEFT JOIN FETCH r.cliente " +
            "WHERE t.barbero.idBarbero = :barberoId")
    List<Turno> findByBarberoConReservasYClientes(@Param("barberoId") Long barberoId);

    // --- NUEVOS MÉTODOS PARA FILTROS SEMANALES (usando LocalDateTime) ---

    @Query("SELECT t FROM Turno t " +
            "WHERE t.fechaHora >= :fechaInicio AND t.fechaHora < :fechaFin " +
            "ORDER BY t.fechaHora ASC")
    List<Turno> findByFechaHoraBetweenDates(@Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT t FROM Turno t " +
            "WHERE t.barbero.idBarbero = :barberoId " +
            "AND t.fechaHora >= :fechaInicio AND t.fechaHora < :fechaFin " +
            "ORDER BY t.fechaHora ASC")
    List<Turno> findByBarberoAndFechaHoraBetweenDates(@Param("barberoId") Long barberoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT t FROM Turno t " +
            "WHERE t.estado = :estado " +
            "AND t.fechaHora >= :fechaInicio AND t.fechaHora < :fechaFin " +
            "ORDER BY t.fechaHora ASC")
    List<Turno> findByEstadoAndFechaHoraBetweenDates(@Param("estado") Turno.EstadoTurno estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT t FROM Turno t " +
            "WHERE t.barbero.idBarbero = :barberoId " +
            "AND t.estado = :estado " +
            "AND t.fechaHora >= :fechaInicio AND t.fechaHora < :fechaFin " +
            "ORDER BY t.fechaHora ASC")
    List<Turno> findByBarberoAndEstadoAndFechaHoraBetweenDates(@Param("barberoId") Long barberoId,
            @Param("estado") Turno.EstadoTurno estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Método de conveniencia con ordenamiento
    List<Turno> findByBarberoIdBarberoAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long barberoId, LocalDateTime inicio, LocalDateTime fin);

    boolean existsByBarberoIdBarberoAndFechaHora(Long barberoId, LocalDateTime fechaHora);

    long countByBarberoIdBarbero(Long idBarbero);

    /**
     * Cuenta el número de turnos disponibles (no reservados) de un barbero.
     * CORREGIDO: Usa t.estado en lugar de t.disponible
     * 
     * @param idBarbero ID del barbero
     * @return número de turnos disponibles
     */
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.barbero.idBarbero = :idBarbero AND t.estado = 'DISPONIBLE'")
    long countTurnosDisponiblesByBarbero(@Param("idBarbero") Long idBarbero);

    /**
     * Verifica si un barbero tiene turnos futuros.
     * CORREGIDO: Usa t.fechaHora en lugar de t.fechaHoraTurno
     * 
     * @param idBarbero ID del barbero
     * @return true si tiene turnos futuros, false si no
     */
    @Query("SELECT COUNT(t) > 0 FROM Turno t WHERE t.barbero.idBarbero = :idBarbero " +
            "AND t.fechaHora > CURRENT_TIMESTAMP")
    boolean tieneTurnosFuturos(@Param("idBarbero") Long idBarbero);
}