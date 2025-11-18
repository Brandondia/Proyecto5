package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // ==================== MÉTODOS ORIGINALES ====================

    List<Reserva> findByClienteIdCliente(Long idCliente);

    List<Reserva> findByBarberoIdBarbero(Long idBarbero);

    List<Reserva> findByEstado(Reserva.EstadoReserva estado);

    List<Reserva> findByFechaHoraTurnoBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Reserva> findByBarberoIdBarberoAndEstado(Long idBarbero, Reserva.EstadoReserva estado);

    List<Reserva> findByTurnoIdTurno(Long idTurno);

    boolean existsByCliente_IdCliente(Long idCliente);

    @Query("SELECT COUNT(r) FROM Reserva r")
    long countAllReservas();

    long countByEstado(Reserva.EstadoReserva estado);

    // ==================== MÉTODOS PARA PANEL DEL BARBERO ====================

    /**
     * Buscar reservas de un barbero en un rango de fechas, ordenadas por fecha
     */
    List<Reserva> findByBarberoAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
            Barbero barbero,
            LocalDateTime inicio,
            LocalDateTime fin);

    /**
     * Buscar todas las reservas de un barbero ordenadas por fecha descendente
     */
    List<Reserva> findByBarberoOrderByFechaHoraTurnoDesc(Barbero barbero);

    /**
     * Buscar reservas de un barbero por estado específico
     */
    List<Reserva> findByBarberoAndEstadoOrderByFechaHoraTurnoDesc(
            Barbero barbero,
            Reserva.EstadoReserva estado);

    @Query("SELECT r FROM Reserva r " +
            "WHERE r.barbero = :barbero " +
            "AND (LOWER(r.cliente.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "     OR LOWER(r.cliente.correo) LIKE LOWER(CONCAT('%', :busqueda, '%')))")
    List<Reserva> buscarPorBarberoYCliente(
            @Param("barbero") Barbero barbero,
            @Param("busqueda") String busqueda);

    /**
     * Contar reservas de un barbero en un período específico
     */
    long countByBarberoAndFechaHoraTurnoBetween(
            Barbero barbero,
            LocalDateTime inicio,
            LocalDateTime fin);

    List<Reserva> findByBarberoIdBarberoAndFechaHoraTurnoBetween(
            Long idBarbero, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Buscar la próxima reserva confirmada de un barbero
     */
    Reserva findFirstByBarberoAndEstadoAndFechaHoraTurnoAfterOrderByFechaHoraTurno(
            Barbero barbero,
            Reserva.EstadoReserva estado,
            LocalDateTime fechaHora);

    /**
     * Contar reservas confirmadas de un barbero para hoy
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.barbero = :barbero AND " +
            "r.estado = :estado AND " +
            "DATE(r.fechaHoraTurno) = DATE(:fecha)")
    long contarReservasHoyPorEstado(
            @Param("barbero") Barbero barbero,
            @Param("estado") Reserva.EstadoReserva estado,
            @Param("fecha") LocalDateTime fecha);

    /**
     * Obtener todas las reservas de un barbero para una fecha específica
     */
    @Query("SELECT r FROM Reserva r WHERE r.barbero = :barbero AND " +
            "DATE(r.fechaHoraTurno) = DATE(:fecha) " +
            "ORDER BY r.fechaHoraTurno")
    List<Reserva> obtenerReservasPorFecha(
            @Param("barbero") Barbero barbero,
            @Param("fecha") LocalDateTime fecha);

    /**
     * Verificar si existe una reserva para un barbero en un horario específico
     */
    boolean existsByBarberoAndFechaHoraTurno(
            Barbero barbero,
            LocalDateTime fechaHora);

    /**
     * Obtener reservas de un barbero agrupadas por estado
     */
    @Query("SELECT r.estado, COUNT(r) FROM Reserva r WHERE r.barbero = :barbero " +
            "GROUP BY r.estado")
    List<Object[]> contarReservasPorEstado(@Param("barbero") Barbero barbero);

    /**
     * Obtener ingresos totales de un barbero en un período
     */
    @Query("SELECT COALESCE(SUM(r.corte.precio), 0) FROM Reserva r " +
            "WHERE r.barbero = :barbero AND " +
            "r.estado = :estado AND " +
            "r.fechaHoraTurno BETWEEN :inicio AND :fin")
    Double calcularIngresosPorPeriodo(
            @Param("barbero") Barbero barbero,
            @Param("estado") Reserva.EstadoReserva estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Obtener las últimas N reservas de un barbero
     */
    List<Reserva> findTop10ByBarberoOrderByFechaHoraTurnoDesc(Barbero barbero);

    /**
     * Buscar reservas pendientes de confirmación para un barbero
     */
    @Query("SELECT r FROM Reserva r WHERE r.barbero = :barbero AND " +
            "r.estado = 'PENDIENTE' AND " +
            "r.fechaHoraTurno >= :ahora " +
            "ORDER BY r.fechaHoraTurno")
    List<Reserva> obtenerReservasPendientes(
            @Param("barbero") Barbero barbero,
            @Param("ahora") LocalDateTime ahora);

    /**
     * Obtener reservas del día actual de un barbero
     */
    @Query("SELECT r FROM Reserva r WHERE r.barbero.idBarbero = :idBarbero AND " +
            "DATE(r.fechaHoraTurno) = CURRENT_DATE " +
            "ORDER BY r.fechaHoraTurno")
    List<Reserva> obtenerReservasDeHoy(@Param("idBarbero") Long idBarbero);

    /**
     * Obtiene TODAS las reservas de un barbero ordenadas por fecha ascendente
     */
    List<Reserva> findByBarbero_IdBarberoOrderByFechaHoraTurnoAsc(Long idBarbero);

    /**
     * Versión alternativa con @Query
     */
    @Query("SELECT r FROM Reserva r WHERE r.barbero.idBarbero = :idBarbero ORDER BY r.fechaHoraTurno ASC")
    List<Reserva> findAllReservasByBarberoOrderAsc(@Param("idBarbero") Long idBarbero);

    // ==================== MÉTODOS PARA AUSENCIAS ====================

    /**
     *  NUEVO: Verifica si existe una reserva para un barbero en una fecha/hora específica
     * que NO esté en el estado especificado (para validar disponibilidad)
     */
    boolean existsByBarberoIdBarberoAndFechaHoraTurnoAndEstadoNot(
            Long idBarbero,
            LocalDateTime fechaHoraTurno,
            Reserva.EstadoReserva estado
    );

    /**
     *  NUEVO: Encuentra todas las reservas activas (no canceladas) de un barbero
     * en un rango de fechas
     */
    List<Reserva> findByBarberoIdBarberoAndFechaHoraTurnoBetweenAndEstadoNot(
            Long idBarbero,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Reserva.EstadoReserva estado
    );

    /**
     *  NUEVO: Encuentra todas las reservas de un barbero en un rango de fechas
     * con un estado específico
     */
    List<Reserva> findByBarberoIdBarberoAndFechaHoraTurnoBetweenAndEstado(
            Long idBarbero,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Reserva.EstadoReserva estado
    );

    // ==================== MÉTODOS NUEVOS PARA DASHBOARD DE CLIENTE ====================

    /**
     *  NUEVO: Obtiene las reservas de un cliente filtradas por estado, ordenadas por fecha descendente
     */
    List<Reserva> findByClienteIdClienteAndEstadoOrderByFechaHoraTurnoDesc(
            Long idCliente, 
            Reserva.EstadoReserva estado
    );
    /**
     *  VALIDACIÓN: Cuenta reservas pendientes de un cliente
     */
    long countByClienteIdClienteAndEstado(Long idCliente, Reserva.EstadoReserva estado);

    /**
     *  VALIDACIÓN: Cuenta reservas de un cliente en un rango de fechas
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.cliente.idCliente = :idCliente " +
           "AND r.fechaHoraTurno BETWEEN :inicio AND :fin")
    long countByClienteAndFechaRange(@Param("idCliente") Long idCliente,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin);

    /**
     *  VALIDACIÓN: Obtiene la última reserva creada por un cliente
     */
    Reserva findFirstByClienteIdClienteOrderByFechaHoraReservaDesc(Long idCliente);
}