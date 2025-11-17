package com.pa.spring.prueba1.pa_prueba1.service.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion.TipoNotificacion;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.NotificacionRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.service.EmailService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Barberos con Soft Delete
 * 
 * @author Tu Nombre
 * @version 2.0 - Implementación completa de soft delete con notificaciones
 */
@Service
public class BarberoService {

    @Autowired
    private BarberoRepository barberoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SolicitudAusenciaRepository solicitudAusenciaRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private TurnoRepository turnoRepository;

    // ==================== MÉTODOS BÁSICOS ====================

    public Barbero obtenerBarberoPorEmail(String email) {
        return barberoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado con email: " + email));
    }

    public Barbero obtenerBarberoPorId(Long id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado con ID: " + id));
    }

    public SolicitudAusencia obtenerSolicitudPorId(Long id) {
        return solicitudAusenciaRepository.findById(id).orElse(null);
    }

    public List<Barbero> obtenerBarberosActivos() {
        return barberoRepository.findByActivoTrue();
    }

    /**
     * Obtiene barberos inactivos/desvinculados
     */
    public List<Barbero> obtenerBarberosInactivos() {
        return barberoRepository.findByActivoFalse();
    }

    public List<Reserva> obtenerReservasSemanaActual(Long idBarbero) {
        LocalDateTime inicioSemana = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .toLocalDate()
                .atStartOfDay();
        LocalDateTime finSemana = inicioSemana.plusDays(6).toLocalDate().atTime(23, 59, 59);

        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                idBarbero, inicioSemana, finSemana);
    }

    public List<Reserva> obtenerTodasReservasBarbero(Long idBarbero) {
        return reservaRepository.findByBarbero_IdBarberoOrderByFechaHoraTurnoAsc(idBarbero);
    }

    public List<SolicitudAusencia> obtenerSolicitudesBarbero(Long idBarbero) {
        return solicitudAusenciaRepository.findByBarberoIdBarbero(idBarbero);
    }

    @Transactional
    public Barbero registrarBarbero(Barbero barbero, String passwordPlain) {
        if (barberoRepository.existsByEmail(barbero.getEmail())) {
            throw new RuntimeException("Ya existe un barbero con el email: " + barbero.getEmail());
        }

        if (barbero.getDocumento() != null && !barbero.getDocumento().trim().isEmpty()) {
            if (barberoRepository.existsByDocumento(barbero.getDocumento())) {
                throw new RuntimeException("Ya existe un barbero con el documento: " + barbero.getDocumento());
            }
        }

        barbero.setPassword(passwordEncoder.encode(passwordPlain));
        barbero.setActivo(true);

        return barberoRepository.save(barbero);
    }

    @Transactional
    public Barbero actualizarBarbero(Barbero barbero) {
        Barbero barberoExistente = obtenerBarberoPorId(barbero.getIdBarbero());

        barberoExistente.setNombre(barbero.getNombre());
        barberoExistente.setApellido(barbero.getApellido());
        barberoExistente.setTelefono(barbero.getTelefono());
        barberoExistente.setEspecialidad(barbero.getEspecialidad());
        barberoExistente.setDiaLibre(barbero.getDiaLibre());
        barberoExistente.setHoraInicio(barbero.getHoraInicio());
        barberoExistente.setHoraFin(barbero.getHoraFin());
        barberoExistente.setHoraInicioAlmuerzo(barbero.getHoraInicioAlmuerzo());
        barberoExistente.setHoraFinAlmuerzo(barbero.getHoraFinAlmuerzo());
        barberoExistente.setDuracionTurno(barbero.getDuracionTurno());

        return barberoRepository.save(barberoExistente);
    }

    @Transactional
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Barbero barbero = obtenerBarberoPorEmail(email);

        if (!passwordEncoder.matches(passwordActual, barbero.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        barbero.setPassword(passwordEncoder.encode(passwordNueva));
        barberoRepository.save(barbero);
    }

    /**
     * Cambia el estado de un barbero (activo/inactivo)
     * Usa actualización directa para evitar errores de entidad
     */
    @Transactional
    public void cambiarEstadoBarbero(Long idBarbero, boolean activo) {
        barberoRepository.actualizarEstado(idBarbero, activo);
    }

    // ==================== MÉTODOS DE SOFT DELETE ====================

    /**
     * Desvincula un barbero de forma normal (sin reservas activas)
     * 
     * @param idBarbero ID del barbero
     * @param motivo    Motivo de la desvinculación
     * @throws RuntimeException si el barbero tiene reservas futuras activas
     */
    @Transactional
    public void desvincularBarbero(Long idBarbero, String motivo) {
        Barbero barbero = obtenerBarberoPorId(idBarbero);

        // Verificar que no tenga reservas futuras activas
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                idBarbero, ahora, ahora.plusYears(1));

        long reservasActivas = reservasFuturas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                .count();

        if (reservasActivas > 0) {
            throw new RuntimeException(
                    "No se puede desvincular el barbero porque tiene " + reservasActivas +
                            " reservas futuras activas. Use la desvinculación de emergencia.");
        }

        barbero.desvincular(motivo);
        barberoRepository.save(barbero);

        System.out.println("✅ Barbero desvinculado: " + barbero.getNombreCompleto());
    }

    /**
     * Desvincula un barbero de emergencia (aunque tenga reservas)
     * Cancela todas las reservas y notifica a los clientes
     * 
     * @param idBarbero       ID del barbero
     * @param motivo          Motivo de la desvinculación
     * @param mensajeClientes Mensaje personalizado para clientes (opcional)
     * @return número de reservas canceladas
     */
    @Transactional
    public int desvincularBarberoEmergencia(Long idBarbero, String motivo, String mensajeClientes) {
        Barbero barbero = obtenerBarberoPorId(idBarbero);

        // Obtener reservas futuras activas
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                idBarbero, ahora, ahora.plusMonths(3));

        List<Reserva> reservasActivas = reservasFuturas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                .collect(java.util.stream.Collectors.toList());

        // Cancelar todas las reservas y notificar
        int reservasCanceladas = cancelarReservasYNotificarClientes(reservasActivas, barbero, motivo, mensajeClientes);

        // Desvincular al barbero
        barbero.desvincular("EMERGENCIA: " + motivo);
        barberoRepository.save(barbero);

        System.out.println("✅ Barbero desvinculado en emergencia: " + barbero.getNombreCompleto());
        System.out.println("📊 Reservas canceladas y notificadas: " + reservasCanceladas);

        return reservasCanceladas;
    }

    /**
     * Reasigna reservas de un barbero a otro y notifica a los clientes
     * 
     * @param idBarberoOriginal  ID del barbero original (será desvinculado)
     * @param idBarberoSustituto ID del barbero sustituto
     * @param motivo             Motivo de la desvinculación
     * @param mensajeClientes    Mensaje personalizado (opcional)
     * @return número de reservas reasignadas
     */
    @Transactional
    public int reasignarYDesvincularBarbero(Long idBarberoOriginal, Long idBarberoSustituto,
            String motivo, String mensajeClientes) {
        Barbero barberoOriginal = obtenerBarberoPorId(idBarberoOriginal);
        Barbero barberoSustituto = obtenerBarberoPorId(idBarberoSustituto);

        if (!barberoSustituto.isActivo()) {
            throw new RuntimeException("El barbero sustituto no está activo");
        }

        // Obtener reservas futuras activas
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                idBarberoOriginal, ahora, ahora.plusMonths(3));

        List<Reserva> reservasActivas = reservasFuturas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                .collect(java.util.stream.Collectors.toList());

        // Reasignar reservas
        int reservasReasignadas = reasignarReservas(reservasActivas, barberoOriginal, barberoSustituto,
                mensajeClientes);

        // Desvincular al barbero original
        barberoOriginal.desvincular("EMERGENCIA - Reasignado: " + motivo);
        barberoRepository.save(barberoOriginal);

        System.out.println("✅ Barbero desvinculado: " + barberoOriginal.getNombreCompleto());
        System.out.println("🔄 Reservas reasignadas a: " + barberoSustituto.getNombreCompleto());
        System.out.println("📊 Total reasignadas: " + reservasReasignadas);

        return reservasReasignadas;
    }

    /**
     * Reactiva un barbero desvinculado
     * 
     * @param idBarbero ID del barbero
     */
    @Transactional
    public void reactivarBarbero(Long idBarbero) {
        Barbero barbero = obtenerBarberoPorId(idBarbero);
        barbero.reactivar();
        barberoRepository.save(barbero);

        System.out.println("✅ Barbero reactivado: " + barbero.getNombreCompleto());
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Cancela todas las reservas y notifica a los clientes por email
     */
    private int cancelarReservasYNotificarClientes(List<Reserva> reservas, Barbero barbero,
            String motivo, String mensajePersonalizado) {
        int canceladas = 0;

        for (Reserva reserva : reservas) {
            try {
                // Cancelar reserva
                reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
                reservaRepository.save(reserva);

                // Notificar al cliente por email
                try {
                    emailService.notificarCancelacionPorDesvinculacion(
                            reserva.getCliente(),
                            reserva,
                            barbero,
                            motivo);
                    System.out.println("✅ Email enviado a: " + reserva.getCliente().getCorreo());
                } catch (Exception e) {
                    System.err.println(
                            "❌ Error al enviar email a " + reserva.getCliente().getCorreo() + ": " + e.getMessage());
                }

                canceladas++;
            } catch (Exception e) {
                System.err.println("❌ Error al cancelar reserva " + reserva.getIdReserva() + ": " + e.getMessage());
            }
        }

        return canceladas;
    }

    /**
     * Reasigna reservas a otro barbero y notifica a los clientes
     */
    private int reasignarReservas(List<Reserva> reservas, Barbero barberoOriginal,
            Barbero barberoSustituto, String mensajePersonalizado) {
        int reasignadas = 0;

        for (Reserva reserva : reservas) {
            try {
                // Reasignar barbero
                reserva.setBarbero(barberoSustituto);
                reservaRepository.save(reserva);

                // Notificar al cliente por email
                try {
                    emailService.notificarReasignacionReserva(
                            reserva.getCliente(),
                            reserva,
                            barberoOriginal,
                            barberoSustituto);
                    System.out.println("✅ Email de reasignación enviado a: " + reserva.getCliente().getCorreo());
                } catch (Exception e) {
                    System.err.println("❌ Error al enviar email: " + e.getMessage());
                }

                reasignadas++;
            } catch (Exception e) {
                System.err.println("❌ Error al reasignar reserva " + reserva.getIdReserva() + ": " + e.getMessage());
            }
        }

        return reasignadas;
    }

    public boolean estaDisponible(Long idBarbero, java.time.LocalDate fecha) {
        List<SolicitudAusencia> ausencias = solicitudAusenciaRepository
                .findByBarberoIdBarberoAndEstado(idBarbero, SolicitudAusencia.EstadoSolicitud.APROBADA);

        return ausencias.stream()
                .noneMatch(ausencia -> !fecha.isBefore(ausencia.getFechaInicio()) &&
                        !fecha.isAfter(ausencia.getFechaFin()));
    }

    // ==================== GESTIÓN DE AUSENCIAS ====================

    @Transactional
    public SolicitudAusencia crearSolicitudAusencia(SolicitudAusencia solicitud) {
        Barbero barbero = obtenerBarberoPorId(solicitud.getBarbero().getIdBarbero());

        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            if (solicitud.getFechaInicio() == null || solicitud.getFechaFin() == null) {
                throw new RuntimeException("Debe especificar fecha de inicio y fin para día completo");
            }
            if (solicitud.getFechaInicio().isAfter(solicitud.getFechaFin())) {
                throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha fin");
            }
        } else if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            if (solicitud.getFecha() == null) {
                throw new RuntimeException("Debe especificar la fecha para horas específicas");
            }
            if (solicitud.getHoraInicio() == null || solicitud.getHoraFin() == null) {
                throw new RuntimeException("Debe especificar hora de inicio y fin");
            }
            if (solicitud.getHoraInicio().isAfter(solicitud.getHoraFin())) {
                throw new RuntimeException("La hora de inicio no puede ser posterior a la hora fin");
            }
        }

        List<SolicitudAusencia> solicitudesExistentes = solicitudAusenciaRepository
                .findByBarberoIdBarberoAndEstado(barbero.getIdBarbero(), SolicitudAusencia.EstadoSolicitud.PENDIENTE);

        List<SolicitudAusencia> solicitudesAprobadas = solicitudAusenciaRepository
                .findByBarberoIdBarberoAndEstado(barbero.getIdBarbero(), SolicitudAusencia.EstadoSolicitud.APROBADA);

        for (SolicitudAusencia existente : solicitudesExistentes) {
            if (hayConflictoFechas(solicitud, existente)) {
                throw new RuntimeException("Ya existe una solicitud pendiente para estas fechas");
            }
        }

        for (SolicitudAusencia aprobada : solicitudesAprobadas) {
            if (hayConflictoFechas(solicitud, aprobada)) {
                throw new RuntimeException("Ya tiene una ausencia aprobada para estas fechas");
            }
        }

        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
        solicitud.setFechaRespuesta(null);
        solicitud.setMotivoRechazo(null);

        return solicitudAusenciaRepository.save(solicitud);
    }

    @Transactional
    public void cancelarSolicitud(Long idSolicitud, Long idBarbero) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!solicitud.getBarbero().getIdBarbero().equals(idBarbero)) {
            throw new RuntimeException("No tiene permisos para cancelar esta solicitud");
        }

        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Solo se pueden cancelar solicitudes pendientes");
        }

        solicitudAusenciaRepository.delete(solicitud);
    }

    public void eliminarReserva(Long idReserva, Long idBarbero) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!reserva.getBarbero().getIdBarbero().equals(idBarbero)) {
            throw new RuntimeException("No tienes permiso para eliminar esta reserva");
        }

        reservaRepository.delete(reserva);
    }

    private boolean hayConflictoFechas(SolicitudAusencia solicitud1, SolicitudAusencia solicitud2) {
        if (solicitud1.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO &&
                solicitud2.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {

            if (solicitud1.getFechaInicio() == null || solicitud1.getFechaFin() == null ||
                    solicitud2.getFechaInicio() == null || solicitud2.getFechaFin() == null) {
                return false;
            }

            return !(solicitud1.getFechaFin().isBefore(solicitud2.getFechaInicio()) ||
                    solicitud1.getFechaInicio().isAfter(solicitud2.getFechaFin()));
        }

        if (solicitud1.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS &&
                solicitud2.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {

            if (solicitud1.getFecha() == null || solicitud2.getFecha() == null) {
                return false;
            }

            if (!solicitud1.getFecha().equals(solicitud2.getFecha())) {
                return false;
            }

            if (solicitud1.getHoraInicio() == null || solicitud1.getHoraFin() == null ||
                    solicitud2.getHoraInicio() == null || solicitud2.getHoraFin() == null) {
                return false;
            }

            return !(solicitud1.getHoraFin().isBefore(solicitud2.getHoraInicio()) ||
                    solicitud1.getHoraInicio().isAfter(solicitud2.getHoraFin()) ||
                    solicitud1.getHoraFin().equals(solicitud2.getHoraInicio()) ||
                    solicitud1.getHoraInicio().equals(solicitud2.getHoraFin()));
        }

        if (solicitud1.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            if (solicitud1.getFechaInicio() == null || solicitud1.getFechaFin() == null ||
                    solicitud2.getFecha() == null) {
                return false;
            }
            return !solicitud2.getFecha().isBefore(solicitud1.getFechaInicio()) &&
                    !solicitud2.getFecha().isAfter(solicitud1.getFechaFin());
        } else {
            if (solicitud1.getFecha() == null || solicitud2.getFechaInicio() == null ||
                    solicitud2.getFechaFin() == null) {
                return false;
            }
            return !solicitud1.getFecha().isBefore(solicitud2.getFechaInicio()) &&
                    !solicitud1.getFecha().isAfter(solicitud2.getFechaFin());
        }
    }// ==================== CONTINUACIÓN DE BarberoService.java ====================
    // Esta es la parte 2/2 - Agregar después de los métodos de ausencias

    public List<SolicitudAusencia> obtenerSolicitudesPendientes() {
        return solicitudAusenciaRepository.findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
    }

    public long contarSolicitudesPendientes() {
        return solicitudAusenciaRepository.findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE).size();
    }

    public List<SolicitudAusencia> obtenerTodasLasSolicitudes() {
        return solicitudAusenciaRepository.findAll();
    }

    @Transactional
    public void aprobarSolicitud(Long idSolicitud, String emailAdmin, String comentario) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalStateException("Solicitud no encontrada"));

        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aprobar solicitudes pendientes");
        }

        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            if (solicitud.getFechaInicio() == null) {
                throw new IllegalStateException("La solicitud no tiene fecha de inicio válida");
            }
            if (solicitud.getFechaFin() == null) {
                solicitud.setFechaFin(solicitud.getFechaInicio());
            }
        } else if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            if (solicitud.getFecha() == null) {
                throw new IllegalStateException("La solicitud no tiene fecha válida");
            }
            if (solicitud.getHoraInicio() == null || solicitud.getHoraFin() == null) {
                throw new IllegalStateException("La solicitud no tiene horarios válidos");
            }
        }

        List<Reserva> reservasAfectadas = obtenerReservasAfectadasPorAusencia(solicitud);
        int reservasCanceladas = cancelarReservasYNotificarClientesPorAusencia(reservasAfectadas, solicitud);

        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.APROBADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());

        if (comentario != null && !comentario.trim().isEmpty()) {
            solicitud.setMotivoRechazo(comentario);
        }

        solicitudAusenciaRepository.save(solicitud);

        String fechaTexto;
        try {
            fechaTexto = formatearFecha(solicitud);
        } catch (Exception e) {
            fechaTexto = "la fecha solicitada";
        }

        String mensaje = "Tu solicitud de ausencia para " + fechaTexto + " ha sido aprobada";
        if (reservasCanceladas > 0) {
            mensaje += ". Se cancelaron " + reservasCanceladas + " reserva(s) y se notificó a los clientes.";
        }
        if (comentario != null && !comentario.trim().isEmpty()) {
            mensaje += " Comentario: " + comentario;
        }

        crearNotificacion(
                solicitud.getBarbero(),
                Notificacion.TipoNotificacion.AUSENCIA_APROBADA,
                "Ausencia Aprobada",
                mensaje,
                null);
    }

    @Transactional
    public void rechazarSolicitud(Long idSolicitud, String emailAdmin, String motivoRechazo) {
        if (motivoRechazo == null || motivoRechazo.trim().isEmpty()) {
            throw new IllegalStateException("Debe proporcionar un motivo de rechazo");
        }

        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalStateException("Solicitud no encontrada"));

        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes pendientes");
        }

        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setMotivoRechazo(motivoRechazo);

        solicitudAusenciaRepository.save(solicitud);

        crearNotificacion(
                solicitud.getBarbero(),
                Notificacion.TipoNotificacion.AUSENCIA_RECHAZADA,
                "Ausencia Rechazada",
                "Tu solicitud de ausencia ha sido rechazada. Motivo: " + motivoRechazo,
                null);
    }

    public long obtenerReservasAfectadas(Long idSolicitud) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Long idBarbero = solicitud.getBarbero().getIdBarbero();

        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            LocalDateTime inicioAusencia = solicitud.getFechaInicio().atStartOfDay();
            LocalDateTime finAusencia = solicitud.getFechaFin().atTime(23, 59, 59);

            List<Reserva> reservas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                    idBarbero, inicioAusencia, finAusencia);

            return reservas.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();
        } else {
            LocalDateTime inicioAusencia = LocalDateTime.of(
                    solicitud.getFecha(), solicitud.getHoraInicio());
            LocalDateTime finAusencia = LocalDateTime.of(
                    solicitud.getFecha(), solicitud.getHoraFin());

            List<Reserva> reservas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                    idBarbero, inicioAusencia, finAusencia);

            return reservas.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();
        }
    }

    // ==================== GESTIÓN DE RESERVAS ====================

    @Transactional
    public void completarReserva(Long idReserva, Long idBarbero) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!reserva.getBarbero().getIdBarbero().equals(idBarbero)) {
            throw new RuntimeException("No tiene permisos para completar esta reserva");
        }

        if (reserva.getEstado() == Reserva.EstadoReserva.CANCELADA ||
                reserva.getEstado() == Reserva.EstadoReserva.COMPLETADA) {
            throw new RuntimeException("No se puede completar una reserva cancelada o ya completada");
        }

        reserva.setEstado(Reserva.EstadoReserva.COMPLETADA);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(Long idReserva, Long idBarbero, String motivo) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!reserva.getBarbero().getIdBarbero().equals(idBarbero)) {
            throw new RuntimeException("No tiene permisos para cancelar esta reserva");
        }

        if (reserva.getEstado() == Reserva.EstadoReserva.COMPLETADA) {
            throw new RuntimeException("No se puede cancelar una reserva completada");
        }

        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);

        crearNotificacion(
                reserva.getBarbero(),
                Notificacion.TipoNotificacion.RESERVA_CANCELADA,
                "Reserva Cancelada",
                "Has cancelado la reserva del " +
                        reserva.getFechaHoraTurno().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                        ". Motivo: " + (motivo != null ? motivo : "No especificado"),
                reserva);
    }

    // ==================== OTROS MÉTODOS ====================

    @Transactional
    public Barbero guardar(Barbero barbero) {
        if (barbero.getIdBarbero() == null) {
            if (barberoRepository.existsByEmail(barbero.getEmail())) {
                throw new RuntimeException("Ya existe un barbero con el email: " + barbero.getEmail());
            }

            if (barbero.getDocumento() != null && !barbero.getDocumento().trim().isEmpty()) {
                if (barberoRepository.existsByDocumento(barbero.getDocumento())) {
                    throw new RuntimeException("Ya existe un barbero con el documento: " + barbero.getDocumento());
                }
            }

            barbero.setActivo(true);
        } else {
            Optional<Barbero> barberoConMismoEmail = barberoRepository.findByEmail(barbero.getEmail());
            if (barberoConMismoEmail.isPresent() &&
                    !barberoConMismoEmail.get().getIdBarbero().equals(barbero.getIdBarbero())) {
                throw new RuntimeException("Ya existe otro barbero con el email: " + barbero.getEmail());
            }

            if (barbero.getDocumento() != null && !barbero.getDocumento().trim().isEmpty()) {
                Optional<Barbero> barberoConMismoDocumento = barberoRepository.findByDocumento(barbero.getDocumento());
                if (barberoConMismoDocumento.isPresent() &&
                        !barberoConMismoDocumento.get().getIdBarbero().equals(barbero.getIdBarbero())) {
                    throw new RuntimeException("Ya existe otro barbero con el documento: " + barbero.getDocumento());
                }
            }
        }

        return barberoRepository.save(barbero);
    }

    /**
     * Elimina permanentemente un barbero (solo si está desvinculado y sin
     * relaciones)
     */
    @Transactional
    public void eliminar(Long id) {
        Barbero barbero = obtenerBarberoPorId(id);

        // Verificar que el barbero esté desvinculado
        if (barbero.isActivo()) {
            throw new RuntimeException(
                    "No se puede eliminar un barbero activo. " +
                            "Primero debe desvincularlo.");
        }

        // Verificar reservas futuras
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                id, ahora, ahora.plusYears(1));

        long reservasActivas = reservasFuturas.stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();

        if (reservasActivas > 0) {
            throw new RuntimeException(
                    "No se puede eliminar el barbero porque tiene " + reservasActivas +
                            " reservas futuras. Se recomienda mantenerlo desvinculado.");
        }

        // Verificar turnos
        if (turnoRepository != null) {
            long turnosPendientes = turnoRepository.countByBarberoIdBarbero(id);
            if (turnosPendientes > 0) {
                throw new RuntimeException(
                        "No se puede eliminar el barbero porque tiene " + turnosPendientes +
                                " turnos asociados. Elimine primero los turnos.");
            }
        }

        barberoRepository.delete(barbero);
    }

    public List<Barbero> obtenerTodos() {
        return barberoRepository.findAll();
    }

    public Barbero obtenerPorId(Long id) {
        return barberoRepository.findById(id).orElse(null);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String formatearFecha(SolicitudAusencia solicitud) {
        try {
            if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
                if (solicitud.getFecha() != null) {
                    String fecha = solicitud.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (solicitud.getHoraInicio() != null && solicitud.getHoraFin() != null) {
                        return fecha + " de " + solicitud.getHoraInicio() + " a " + solicitud.getHoraFin();
                    }
                    return fecha;
                }
            } else {
                if (solicitud.getFechaInicio() != null) {
                    if (solicitud.getFechaFin() != null
                            && !solicitud.getFechaInicio().equals(solicitud.getFechaFin())) {
                        return solicitud.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                " al " + solicitud.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                    return solicitud.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            }
        } catch (Exception e) {
            return "la fecha solicitada";
        }
        return "la fecha solicitada";
    }

    private List<Reserva> obtenerReservasAfectadasPorAusencia(SolicitudAusencia solicitud) {
        Long idBarbero = solicitud.getBarbero().getIdBarbero();
        List<Reserva> reservasAfectadas = new java.util.ArrayList<>();

        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            LocalDateTime inicioAusencia = solicitud.getFechaInicio().atStartOfDay();
            LocalDateTime finAusencia = solicitud.getFechaFin().atTime(23, 59, 59);

            reservasAfectadas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                    idBarbero, inicioAusencia, finAusencia);

        } else if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            LocalDateTime inicioAusencia = LocalDateTime.of(solicitud.getFecha(), solicitud.getHoraInicio());
            LocalDateTime finAusencia = LocalDateTime.of(solicitud.getFecha(), solicitud.getHoraFin());

            reservasAfectadas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                    idBarbero, inicioAusencia, finAusencia);
        }

        return reservasAfectadas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA ||
                        r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                .collect(java.util.stream.Collectors.toList());
    }

    private int cancelarReservasYNotificarClientesPorAusencia(List<Reserva> reservas, SolicitudAusencia solicitud) {
        int canceladas = 0;

        for (Reserva reserva : reservas) {
            try {
                reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
                reservaRepository.save(reserva);

                try {
                    emailService.notificarCancelacionPorAusencia(
                            reserva.getCliente(),
                            reserva,
                            solicitud.getBarbero(),
                            "El barbero estará ausente");
                } catch (Exception e) {
                    System.err.println("Error al enviar email: " + e.getMessage());
                }

                canceladas++;
            } catch (Exception e) {
                System.err.println("Error al cancelar reserva " + reserva.getIdReserva() + ": " + e.getMessage());
            }
        }

        return canceladas;
    }

    public boolean esBarberoDisponibleEnFechaHora(Long idBarbero, LocalDateTime fechaHora) {
        List<SolicitudAusencia> ausenciasAprobadas = solicitudAusenciaRepository
                .findByBarberoIdBarberoAndEstado(idBarbero, SolicitudAusencia.EstadoSolicitud.APROBADA);

        for (SolicitudAusencia ausencia : ausenciasAprobadas) {
            if (ausencia.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
                java.time.LocalDate fecha = fechaHora.toLocalDate();
                if (!fecha.isBefore(ausencia.getFechaInicio()) && !fecha.isAfter(ausencia.getFechaFin())) {
                    return false;
                }
            } else if (ausencia.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
                if (fechaHora.toLocalDate().equals(ausencia.getFecha())) {
                    java.time.LocalTime hora = fechaHora.toLocalTime();
                    if (!hora.isBefore(ausencia.getHoraInicio()) && hora.isBefore(ausencia.getHoraFin())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void validarDisponibilidadParaReserva(Long idBarbero, LocalDateTime fechaHora) {
        if (!esBarberoDisponibleEnFechaHora(idBarbero, fechaHora)) {
            throw new RuntimeException("El barbero no está disponible en la fecha y hora seleccionadas");
        }
    }

    public List<Reserva> obtenerReservasPorRangoFechas(Long idBarbero, LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(idBarbero, fechaInicio, fechaFin);
    }

    // ==================== MÉTODOS PARA NOTIFICACIONES ====================

    @Transactional
    public Notificacion crearNotificacion(Barbero barbero, Notificacion.TipoNotificacion tipo,
            String titulo, String mensaje, Reserva reserva) {
        Notificacion notificacion = new Notificacion();
        notificacion.setBarbero(barbero);
        notificacion.setTipo(tipo);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setReserva(reserva);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());

        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> obtenerNotificacionesBarbero(Long idBarbero) {
        return notificacionRepository.findByBarberoIdBarberoOrderByFechaCreacionDesc(idBarbero);
    }

    public List<Notificacion> obtenerNotificacionesNoLeidas(Long idBarbero) {
        return notificacionRepository.findByBarberoIdBarberoAndLeidaOrderByFechaCreacionDesc(
                idBarbero, false);
    }

    public long contarNotificacionesNoLeidas(Long idBarbero) {
        return notificacionRepository.countByBarberoIdBarberoAndLeida(idBarbero, false);
    }

    @Transactional
    public void marcarNotificacionComoLeida(Long idNotificacion) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void marcarTodasComoLeidas(Long idBarbero) {
        List<Notificacion> notificaciones = obtenerNotificacionesNoLeidas(idBarbero);
        notificaciones.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(notificaciones);
    }

    @Transactional
    public void eliminarNotificacion(Long idNotificacion, Long idBarbero) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        if (!notificacion.getBarbero().getIdBarbero().equals(idBarbero)) {
            throw new RuntimeException("No tiene permisos para eliminar esta notificación");
        }

        notificacionRepository.delete(notificacion);
    }

    public List<Notificacion> obtenerNotificacionesRecientes(Long idBarbero) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("fechaCreacion").descending());
        Page<Notificacion> page = notificacionRepository.findByBarberoIdBarberoOrderByFechaCreacionDesc(
                idBarbero, pageable);
        return page.getContent();
    }

    public void guardarConfiguracionNotificaciones(Long idBarbero, Map<String,Boolean> configuracion) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'guardarConfiguracionNotificaciones'");
    }

    public List<Notificacion> obtenerUltimasNotificaciones(Long idBarbero, int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerUltimasNotificaciones'");
    }

    public long contarNotificacionesPorTipo(Long idBarbero, TipoNotificacion sistema) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarNotificacionesPorTipo'");
    }

    public long contarNotificacionesPorTipos(Long idBarbero, List<TipoNotificacion> of) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarNotificacionesPorTipos'");
    }

    public Page<Notificacion> obtenerNotificacionesBarberoP(Long idBarbero, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerNotificacionesBarberoP'");
    }

    public Page<Notificacion> obtenerNotificacionesPorTipoPaginadas(Long idBarbero, TipoNotificacion tipoEnum,
            Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerNotificacionesPorTipoPaginadas'");
    }

    public Page<Notificacion> obtenerNotificacionesNoLeidasPaginadas(Long idBarbero, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerNotificacionesNoLeidasPaginadas'");
    }

    public Page<Notificacion> buscarNotificaciones(Long idBarbero, String buscar, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarNotificaciones'");
    }
}