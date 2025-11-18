package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    // ==================== MÉTODOS EXISTENTES (sin paginación) ====================
    
    List<Notificacion> findByBarberoIdBarberoOrderByFechaCreacionDesc(Long idBarbero);
    
    List<Notificacion> findByBarberoIdBarberoAndLeidaOrderByFechaCreacionDesc(Long idBarbero, Boolean leida);
    
    long countByBarberoIdBarberoAndLeida(Long idBarbero, Boolean leida);
    
    List<Notificacion> findByBarberoIdBarberoAndTipoOrderByFechaCreacionDesc(
        Long idBarbero, 
        Notificacion.TipoNotificacion tipo
    );
    
    // ==================== MÉTODOS CON PAGINACIÓN ====================
    
    /**
     * Obtiene todas las notificaciones de un barbero con paginación
     */
    Page<Notificacion> findByBarberoIdBarberoOrderByFechaCreacionDesc(Long idBarbero, Pageable pageable);
    
    /**
     * Obtiene notificaciones no leídas con paginación
     */
    Page<Notificacion> findByBarberoIdBarberoAndLeidaOrderByFechaCreacionDesc(
        Long idBarbero, 
        Boolean leida, 
        Pageable pageable
    );
    
    /**
     * Obtiene notificaciones por tipo con paginación
     */
    Page<Notificacion> findByBarberoIdBarberoAndTipoOrderByFechaCreacionDesc(
        Long idBarbero, 
        Notificacion.TipoNotificacion tipo, 
        Pageable pageable
    );
    
    /**
     * Obtiene notificaciones por múltiples tipos con paginación
     */
    Page<Notificacion> findByBarberoIdBarberoAndTipoInOrderByFechaCreacionDesc(
        Long idBarbero,
        List<Notificacion.TipoNotificacion> tipos,
        Pageable pageable
    );
    
    /**
     * Busca notificaciones por texto en mensaje o título
     */
    @Query("SELECT n FROM Notificacion n WHERE n.barbero.idBarbero = :idBarbero " +
           "AND (LOWER(n.mensaje) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "OR LOWER(n.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> buscarPorTexto(
        @Param("idBarbero") Long idBarbero, 
        @Param("texto") String texto, 
        Pageable pageable
    );
    
    // ==================== CONTADORES ====================
    
    /**
     * Cuenta notificaciones por múltiples tipos
     */
    long countByBarberoIdBarberoAndTipoIn(
        Long idBarbero, 
        List<Notificacion.TipoNotificacion> tipos
    );
    
    /**
     * Cuenta notificaciones por un tipo específico
     */
    long countByBarberoIdBarberoAndTipo(
        Long idBarbero, 
        Notificacion.TipoNotificacion tipo
    );
    
    // ==================== OPERACIONES MASIVAS ====================
    
    /**
     * Marca todas las notificaciones de un barbero como leídas
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true " +
           "WHERE n.barbero.idBarbero = :idBarbero AND n.leida = false")
    void marcarTodasComoLeidas(@Param("idBarbero") Long idBarbero);
}