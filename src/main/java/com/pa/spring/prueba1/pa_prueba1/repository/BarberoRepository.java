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

/**
 * Repositorio para la entidad Barbero
 * Incluye métodos para soft delete y consultas optimizadas
 * 
 * @author Tu Nombre
 * @version 2.0 - Actualizado con soft delete
 */
@Repository
public interface BarberoRepository extends JpaRepository<Barbero, Long> {

    // ==================== MÉTODOS BÁSICOS MEJORADOS ====================
    
    /**
     * Obtiene todos los barberos (activos e inactivos)
     * @return lista de todos los barberos
     */
    @SuppressWarnings("null")
    List<Barbero> findAll();

    /**
     * Versión segura de findAll que nunca retorna null
     * @return lista de barberos o lista vacía si no hay ninguno
     */
    default List<Barbero> findAllNonNull() {
        List<Barbero> barberos = findAll();
        return barberos == null ? new ArrayList<>() : barberos;
    }
    
    // ==================== MÉTODOS PARA AUTENTICACIÓN ====================
    
    /**
     * Buscar barbero por email.
     * CRÍTICO: Se usa para autenticación del barbero.
     * 
     * @param email email del barbero
     * @return Optional con el barbero si existe
     */
    Optional<Barbero> findByEmail(String email);
    
    /**
     * Verificar si existe un barbero con un email específico.
     * Se usa para validaciones durante el registro.
     * 
     * @param email email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Buscar barbero por email y que esté activo.
     * Se usa para login (solo barberos activos pueden entrar).
     * 
     * @param email email del barbero
     * @return Optional con el barbero si existe y está activo
     */
    Optional<Barbero> findByEmailAndActivoTrue(String email);
    
    // ==================== MÉTODOS PARA VALIDACIÓN DE DOCUMENTO ====================
    
    /**
     * Buscar barbero por documento.
     * Se usa para validar que no haya documentos duplicados.
     * 
     * @param documento número de documento
     * @return Optional con el barbero si existe
     */
    Optional<Barbero> findByDocumento(String documento);
    
    /**
     * Verificar si existe un barbero con un documento específico.
     * Se usa para validaciones durante el registro y actualización.
     * 
     * @param documento documento a verificar
     * @return true si existe, false si no
     */
    boolean existsByDocumento(String documento);
    
    // ==================== MÉTODOS PARA GESTIÓN DE BARBEROS ACTIVOS/INACTIVOS ====================
    
    /**
     * Buscar barberos activos.
     * Se usa para listar solo barberos que pueden recibir reservas.
     * 
     * @return lista de barberos activos
     */
    List<Barbero> findByActivoTrue();
    
    /**
     * Buscar barberos inactivos/desvinculados.
     * Se usa para listar barberos que ya no trabajan.
     * 
     * @return lista de barberos desvinculados
     */
    List<Barbero> findByActivoFalse();
    
    /**
     * Buscar barberos por especialidad (solo activos).
     * 
     * @param especialidad especialidad a buscar
     * @return lista de barberos con esa especialidad
     */
    @Query("SELECT b FROM Barbero b WHERE b.especialidad = :especialidad AND b.activo = true")
    List<Barbero> findByEspecialidadAndActivoTrue(@Param("especialidad") String especialidad);
    
    /**
     * Buscar barberos por especialidad (todos).
     * 
     * @param especialidad especialidad a buscar
     * @return lista de barberos con esa especialidad
     */
    List<Barbero> findByEspecialidad(String especialidad);
    
    // ==================== MÉTODOS DE BÚSQUEDA ====================
    
    /**
     * Buscar barbero por nombre o apellido (búsqueda parcial).
     * Ignora mayúsculas/minúsculas.
     * 
     * @param busqueda texto a buscar
     * @return lista de barberos que coinciden
     */
    @Query("SELECT b FROM Barbero b WHERE " +
           "LOWER(b.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(b.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Barbero> buscarPorNombre(@Param("busqueda") String busqueda);
    
    /**
     * Buscar barberos disponibles en una fecha específica.
     * Excluye barberos que tengan ausencias aprobadas en esa fecha.
     * 
     * @param fecha fecha a verificar
     * @return lista de barberos disponibles
     */
    @Query("SELECT b FROM Barbero b WHERE b.activo = true AND " +
           "b.idBarbero NOT IN (" +
           "    SELECT DISTINCT s.barbero.idBarbero FROM SolicitudAusencia s " +
           "    WHERE s.estado = 'APROBADA' AND " +
           "    :fecha BETWEEN s.fechaInicio AND s.fechaFin" +
           ")")
    List<Barbero> findBarberosDisponiblesEnFecha(@Param("fecha") LocalDate fecha);
    
    // ==================== MÉTODOS DE CONTEO ====================
    
    /**
     * Contar total de barberos activos.
     * 
     * @return número de barberos activos
     */
    long countByActivoTrue();
    
    /**
     * Contar total de barberos inactivos.
     * 
     * @return número de barberos desvinculados
     */
    long countByActivoFalse();
    
    // ==================== MÉTODOS DE ESTADÍSTICAS ====================
    
    /**
     * Obtener barberos con más reservas en un período.
     * Ordenados por cantidad de reservas completadas (descendente).
     * 
     * @param inicio fecha/hora de inicio del período
     * @param fin fecha/hora de fin del período
     * @return lista de barberos ordenados por reservas
     */
    @Query("SELECT DISTINCT r.barbero " +
           "FROM Reserva r " +
           "WHERE r.fechaHoraTurno BETWEEN :inicio AND :fin " +
           "AND r.estado = 'COMPLETADA' " +
           "GROUP BY r.barbero " +
           "ORDER BY COUNT(r) DESC")
    List<Barbero> obtenerBarberosConMasReservas(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin);
    
    // ==================== MÉTODO CRÍTICO PARA SOFT DELETE ====================
    
    /**
     * Actualiza SOLO el campo activo de un barbero.
     * 
     * IMPORTANTE: Este método evita el error "not-null property references a null or transient value"
     * que ocurre al intentar guardar una entidad parcialmente cargada.
     * 
     * Al usar @Modifying + @Query con UPDATE directo, solo se actualiza el campo específico
     * sin necesidad de cargar toda la entidad en memoria.
     * 
     * @param id ID del barbero
     * @param activo true para activar, false para desactivar
     * @return número de filas actualizadas (debe ser 1 si el barbero existe)
     */
    @Modifying
    @Query("UPDATE Barbero b SET b.activo = :activo WHERE b.idBarbero = :id")
    int actualizarEstado(@Param("id") Long id, @Param("activo") boolean activo);
    
    /**
     * Actualiza los campos de desvinculación de un barbero.
     * Usado para el soft delete completo con motivo.
     * 
     * @param id ID del barbero
     * @param activo estado (false para desvincular)
     * @param fechaDesvinculacion fecha de desvinculación
     * @param motivoDesvinculacion motivo de desvinculación
     * @return número de filas actualizadas
     */
    @Modifying
    @Query("UPDATE Barbero b SET b.activo = :activo, " +
           "b.fechaDesvinculacion = :fechaDesvinculacion, " +
           "b.motivoDesvinculacion = :motivoDesvinculacion " +
           "WHERE b.idBarbero = :id")
    int actualizarDesvinculacion(
        @Param("id") Long id,
        @Param("activo") boolean activo,
        @Param("fechaDesvinculacion") LocalDateTime fechaDesvinculacion,
        @Param("motivoDesvinculacion") String motivoDesvinculacion
    );
    
    // ==================== MÉTODOS DE AUDITORÍA ====================
    
    /**
     * Obtener barberos desvinculados recientemente (últimos 30 días).
     * 
     * @param fechaDesde fecha desde la cual buscar
     * @return lista de barberos desvinculados recientemente
     */
    @Query("SELECT b FROM Barbero b WHERE b.activo = false " +
           "AND b.fechaDesvinculacion >= :fechaDesde " +
           "ORDER BY b.fechaDesvinculacion DESC")
    List<Barbero> findBarberosDesvinculadosRecientes(@Param("fechaDesde") LocalDateTime fechaDesde);
    
    /**
     * Obtener barberos por motivo de desvinculación.
     * 
     * @param motivo texto a buscar en el motivo
     * @return lista de barberos con ese motivo
     */
    @Query("SELECT b FROM Barbero b WHERE b.activo = false " +
           "AND LOWER(b.motivoDesvinculacion) LIKE LOWER(CONCAT('%', :motivo, '%'))")
    List<Barbero> findByMotivoDesvinculacion(@Param("motivo") String motivo);
}