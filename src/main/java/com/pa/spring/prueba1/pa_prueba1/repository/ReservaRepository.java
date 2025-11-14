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

                // ==================== MÉTODOS NUEVOS PARA PANEL DEL BARBERO ====================

                /**
                 * Buscar reservas de un barbero en un rango de fechas, ordenadas por fecha
                 * Se usa para filtrar reservas por día, semana o mes
                 */
                List<Reserva> findByBarberoAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                                Barbero barbero,
                                LocalDateTime inicio,
                                LocalDateTime fin);

                /**
                 * Buscar todas las reservas de un barbero ordenadas por fecha descendente
                 * Se usa para mostrar el historial completo
                 */
                List<Reserva> findByBarberoOrderByFechaHoraTurnoDesc(Barbero barbero);

                /**
                 * Buscar reservas de un barbero por estado específico
                 * Se usa para filtrar reservas confirmadas, completadas, canceladas, etc.
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
                 * Se usa para estadísticas
                 */
                long countByBarberoAndFechaHoraTurnoBetween(
                                Barbero barbero,
                                LocalDateTime inicio,
                                LocalDateTime fin);

                List<Reserva> findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                                Long idBarbero, LocalDateTime fechaInicio, LocalDateTime fechaFin);

                /**
                 * Buscar la próxima reserva confirmada de un barbero
                 * Se usa para mostrar "Próxima Reserva" en el dashboard
                 */
                Reserva findFirstByBarberoAndEstadoAndFechaHoraTurnoAfterOrderByFechaHoraTurno(
                                Barbero barbero,
                                Reserva.EstadoReserva estado,
                                LocalDateTime fechaHora);

                /**
                 * Contar reservas confirmadas de un barbero para hoy
                 * Se usa en el dashboard
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
                 * Se usa para ver la agenda del día
                 */
                @Query("SELECT r FROM Reserva r WHERE r.barbero = :barbero AND " +
                                "DATE(r.fechaHoraTurno) = DATE(:fecha) " +
                                "ORDER BY r.fechaHoraTurno")
                List<Reserva> obtenerReservasPorFecha(
                                @Param("barbero") Barbero barbero,
                                @Param("fecha") LocalDateTime fecha);

                /**
                 * Verificar si existe una reserva para un barbero en un horario específico
                 * Se usa para validar disponibilidad
                 */
                boolean existsByBarberoAndFechaHoraTurno(
                                Barbero barbero,
                                LocalDateTime fechaHora);

                /**
                 * Obtener reservas de un barbero agrupadas por estado
                 * Se usa para reportes y estadísticas
                 */
                @Query("SELECT r.estado, COUNT(r) FROM Reserva r WHERE r.barbero = :barbero " +
                                "GROUP BY r.estado")
                List<Object[]> contarReservasPorEstado(@Param("barbero") Barbero barbero);

                /**
                 * Obtener ingresos totales de un barbero en un período
                 * Se usa para estadísticas financieras
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
                 * Se usa para mostrar historial reciente
                 */
                List<Reserva> findTop10ByBarberoOrderByFechaHoraTurnoDesc(Barbero barbero);

                /**
                 * Buscar reservas pendientes de confirmación para un barbero
                 * Se usa para notificaciones
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
                 * Método alternativo usando solo el objeto Barbero
                 */
                @Query("SELECT r FROM Reserva r WHERE r.barbero.idBarbero = :idBarbero AND " +
                                "DATE(r.fechaHoraTurno) = CURRENT_DATE " +
                                "ORDER BY r.fechaHoraTurno")
                List<Reserva> obtenerReservasDeHoy(@Param("idBarbero") Long idBarbero);

                // ==================== MÉTODO NUEVO PARA FILTROS DE RESERVAS ====================

                /**
                 * NUEVO: Obtiene TODAS las reservas de un barbero ordenadas por fecha ascendente
                 * Este método es necesario para que los filtros (hoy, semana, mes, todas) funcionen correctamente
                 * 
                 * @param idBarbero ID del barbero
                 * @return Lista de todas las reservas del barbero ordenadas por fecha ascendente
                 */
                List<Reserva> findByBarbero_IdBarberoOrderByFechaHoraTurnoAsc(Long idBarbero);

                /**
                 * ALTERNATIVA: Versión con @Query si prefieres usar consulta explícita
                 * Puedes usar este método si el anterior da problemas
                 */
                @Query("SELECT r FROM Reserva r WHERE r.barbero.idBarbero = :idBarbero ORDER BY r.fechaHoraTurno ASC")
                List<Reserva> findAllReservasByBarberoOrderAsc(@Param("idBarbero") Long idBarbero);
        }