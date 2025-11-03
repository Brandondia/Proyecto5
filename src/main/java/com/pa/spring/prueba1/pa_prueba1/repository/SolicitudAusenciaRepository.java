package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitudAusenciaRepository extends JpaRepository<SolicitudAusencia, Long> {
    
    /**
     * Encuentra todas las solicitudes de ausencia de un barbero específico.
     *
     * @param idBarbero ID del barbero
     * @return lista de solicitudes del barbero
     */
    List<SolicitudAusencia> findByBarberoIdBarbero(Long idBarbero);
    
    /**
     * Encuentra las solicitudes de ausencia de un barbero con un estado específico.
     *
     * @param idBarbero ID del barbero
     * @param estado estado de la solicitud
     * @return lista de solicitudes filtradas por estado
     */
    List<SolicitudAusencia> findByBarberoIdBarberoAndEstado(Long idBarbero, SolicitudAusencia.EstadoSolicitud estado);
    
    /**
     * Encuentra todas las solicitudes con un estado específico.
     *
     * @param estado estado de la solicitud
     * @return lista de solicitudes con ese estado
     */
    List<SolicitudAusencia> findByEstado(SolicitudAusencia.EstadoSolicitud estado);
    
    /**
     * Encuentra solicitudes de ausencia aprobadas que incluyan una fecha específica.
     *
     * @param idBarbero ID del barbero
     * @param fecha fecha a verificar
     * @param estado estado de la solicitud
     * @return lista de solicitudes que incluyen esa fecha
     */
    List<SolicitudAusencia> findByBarberoIdBarberoAndEstadoAndFechaInicioBetween(
            Long idBarbero, 
            SolicitudAusencia.EstadoSolicitud estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
    
    /**
     * Encuentra todas las solicitudes pendientes (útil para el admin).
     *
     * @return lista de solicitudes pendientes
     */
    default List<SolicitudAusencia> findAllPendientes() {
        return findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
    }
}