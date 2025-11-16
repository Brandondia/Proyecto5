package com.pa.spring.prueba1.pa_prueba1.service.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.NotificacionRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudAusenciaService {

    @Autowired
    private SolicitudAusenciaRepository solicitudAusenciaRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    /**
     * Crea una nueva solicitud de ausencia
     */
    @Transactional
    public SolicitudAusencia crearSolicitud(SolicitudAusencia solicitud) {
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
        solicitud.setFechaCreacion(LocalDateTime.now());
        return solicitudAusenciaRepository.save(solicitud);
    }

    /**
     * Obtiene todas las solicitudes de un barbero
     */
    public List<SolicitudAusencia> obtenerSolicitudesPorBarbero(Long idBarbero) {
        return solicitudAusenciaRepository.findByBarberoIdBarbero(idBarbero);
    }

    /**
     * Obtiene todas las solicitudes pendientes (para el admin)
     */
    public List<SolicitudAusencia> obtenerSolicitudesPendientes() {
        return solicitudAusenciaRepository.findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
    }

    /**
     * Obtiene todas las solicitudes (para el admin)
     */
    public List<SolicitudAusencia> obtenerTodasLasSolicitudes() {
        return solicitudAusenciaRepository.findAll();
    }

    /**
     * Aprueba una solicitud de ausencia
     */
    @Transactional
    public SolicitudAusencia aprobarSolicitud(Long idSolicitud) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.APROBADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        
        SolicitudAusencia solicitudGuardada = solicitudAusenciaRepository.save(solicitud);
        
        // Crear notificación para el barbero
        crearNotificacionAprobacion(solicitud.getBarbero(), solicitud);
        
        return solicitudGuardada;
    }

    /**
     * Rechaza una solicitud de ausencia
     */
    @Transactional
    public SolicitudAusencia rechazarSolicitud(Long idSolicitud, String motivoRechazo) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setMotivoRechazo(motivoRechazo);
        
        SolicitudAusencia solicitudGuardada = solicitudAusenciaRepository.save(solicitud);
        
        // Crear notificación para el barbero
        crearNotificacionRechazo(solicitud.getBarbero(), solicitud);
        
        return solicitudGuardada;
    }

    /**
     * Cancela una solicitud pendiente (solo el barbero puede hacerlo)
     */
    @Transactional
    public void cancelarSolicitud(Long idSolicitud, Long idBarbero) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        // Verificar que la solicitud pertenece al barbero
        if (!solicitud.getBarbero().getIdBarbero().equals(idBarbero)) {
            throw new RuntimeException("No tienes permiso para cancelar esta solicitud");
        }
        
        // Solo se pueden cancelar solicitudes pendientes
        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Solo puedes cancelar solicitudes pendientes");
        }
        
        solicitudAusenciaRepository.deleteById(idSolicitud);
    }

    /**
     * Obtiene una solicitud por ID
     */
    public SolicitudAusencia obtenerSolicitudPorId(Long idSolicitud) {
        return solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    /**
     * Crea una notificación cuando se aprueba una ausencia
     */
    private void crearNotificacionAprobacion(Barbero barbero, SolicitudAusencia solicitud) {
        Notificacion notificacion = new Notificacion();
        notificacion.setBarbero(barbero);
        notificacion.setTipo(Notificacion.TipoNotificacion.AUSENCIA_APROBADA);
        notificacion.setTitulo("Solicitud de Ausencia Aprobada");
        notificacion.setMensaje("Tu solicitud de ausencia para el " + 
                formatearFecha(solicitud) + " ha sido aprobada.");
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        
        notificacionRepository.save(notificacion);
    }

    /**
     * Crea una notificación cuando se rechaza una ausencia
     */
    private void crearNotificacionRechazo(Barbero barbero, SolicitudAusencia solicitud) {
        Notificacion notificacion = new Notificacion();
        notificacion.setBarbero(barbero);
        notificacion.setTipo(Notificacion.TipoNotificacion.AUSENCIA_RECHAZADA);
        notificacion.setTitulo("Solicitud de Ausencia Rechazada");
        notificacion.setMensaje("Tu solicitud de ausencia para el " + 
                formatearFecha(solicitud) + " ha sido rechazada. Motivo: " + 
                solicitud.getMotivoRechazo());
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        
        notificacionRepository.save(notificacion);
    }

    /**
     * Formatea las fechas de la solicitud para mostrar en notificaciones
     */
    private String formatearFecha(SolicitudAusencia solicitud) {
        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            return solicitud.getFecha() + " de " + solicitud.getHoraInicio() + 
                   " a " + solicitud.getHoraFin();
        } else if (solicitud.getFechaFin() != null && !solicitud.getFechaInicio().equals(solicitud.getFechaFin())) {
            return solicitud.getFechaInicio() + " al " + solicitud.getFechaFin();
        } else {
            return solicitud.getFechaInicio().toString();
        }
    }

    /**
     * Cuenta solicitudes por estado para un barbero
     */
    public long contarPorEstado(Long idBarbero, SolicitudAusencia.EstadoSolicitud estado) {
        return solicitudAusenciaRepository.findByBarberoIdBarberoAndEstado(idBarbero, estado).size();
    }
}
