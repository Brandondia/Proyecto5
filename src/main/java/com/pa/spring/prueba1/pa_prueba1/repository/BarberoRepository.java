package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberoRepository extends JpaRepository<Barbero, Long> {

    // ==================== MÉTODOS ORIGINALES ====================
    
    @SuppressWarnings("null")
    List<Barbero> findAll();

    default List<Barbero> findAllNonNull() {
        List<Barbero> barberos = findAll();
        return barberos == null ? new ArrayList<>() : barberos;
    }
    
    // ==================== MÉTODOS PARA AUTENTICACIÓN ====================
    
    /**
     * Buscar barbero por email.
     * CRÍTICO: Se usa para autenticación del barbero.
     */
    Optional<Barbero> findByEmail(String email);
    
    /**
     * Verificar si existe un barbero con un email específico.
     * Se usa para validaciones durante el registro.
     */
    boolean existsByEmail(String email);
    
    /**
     * Buscar barbero por email y que esté activo.
     * Se usa para login (solo barberos activos pueden entrar).
     */
    Optional<Barbero> findByEmailAndActivoTrue(String email);
    
    // ==================== MÉTODOS PARA GESTIÓN ====================
    
    /**
     * Buscar barberos activos.
     * Se usa para listar solo barberos que pueden recibir reservas.
     */
    List<Barbero> findByActivoTrue();
    
    /**
     * Buscar barberos por especialidad.
     */
    List<Barbero> findByEspecialidad(String especialidad);
    
    /**
     * Buscar barbero por nombre o apellido (búsqueda parcial).
     */
    @Query("SELECT b FROM Barbero b WHERE " +
           "LOWER(b.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(b.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Barbero> buscarPorNombre(@Param("busqueda") String busqueda);
    
    /**
     * Buscar barberos disponibles en una fecha específica.
     * Excluye barberos que tengan ausencias aprobadas en esa fecha.
     */
    @Query("SELECT b FROM Barbero b WHERE b.activo = true AND " +
           "b.idBarbero NOT IN (" +
           "    SELECT DISTINCT s.barbero.idBarbero FROM SolicitudAusencia s " +
           "    WHERE s.estado = 'APROBADA' AND " +
           "    :fecha BETWEEN s.fechaInicio AND s.fechaFin" +
           ")")
    List<Barbero> findBarberosDisponiblesEnFecha(@Param("fecha") LocalDate fecha);
    
    /**
     * Contar total de barberos activos.
     */
    long countByActivoTrue();
    
    /**
     * Obtener barberos con más reservas en un período.
     * Versión simplificada sin el conteo
     */
    @Query("SELECT DISTINCT r.barbero " +
           "FROM Reserva r " +
           "WHERE r.fechaHoraTurno BETWEEN :inicio AND :fin " +
           "AND r.estado = 'COMPLETADA' " +
           "GROUP BY r.barbero " +
           "ORDER BY COUNT(r) DESC")
    List<Barbero> obtenerBarberosConMasReservas(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin);
    
    // ==================== NUEVO: MÉTODO PARA ACTUALIZAR ESTADO ====================
    
    /**
     * Actualiza SOLO el campo activo de un barbero.
     * SOLUCIÓN al error "not-null property references a null or transient value: password"
     * 
     * Este método evita tener que cargar y guardar toda la entidad,
     * lo cual causaba problemas con campos @NotNull como password.
     * 
     * @param id ID del barbero
     * @param activo true para activar, false para desactivar
     * @return número de filas actualizadas (debe ser 1 si el barbero existe)
     */
    @Modifying
    @Query("UPDATE Barbero b SET b.activo = :activo WHERE b.idBarbero = :id")
    int actualizarEstado(@Param("id") Long id, @Param("activo") boolean activo);
}


