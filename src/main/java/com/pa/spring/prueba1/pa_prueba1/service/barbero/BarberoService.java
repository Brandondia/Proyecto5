package com.pa.spring.prueba1.pa_prueba1.service.barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.NotificacionRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Map;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

    // ==================== MÉTODOS BÁSICOS ====================

    public Barbero obtenerBarberoPorEmail(String email) {
        return barberoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado con email: " + email));
    }

    public Barbero obtenerBarberoPorId(Long id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado con ID: " + id));
    }

    /**
     * Obtiene una solicitud de ausencia por su ID
     * 
     * @param id ID de la solicitud
     * @return La solicitud o null si no existe
     */
    public SolicitudAusencia obtenerSolicitudPorId(Long id) {
        return solicitudAusenciaRepository.findById(id).orElse(null);
    }

    public List<Barbero> obtenerBarberosActivos() {
        return barberoRepository.findByActivoTrue();
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

    /**
     * NUEVO MÉTODO: Obtiene TODAS las reservas de un barbero sin filtrar por fecha
     * 
     * @param idBarbero ID del barbero
     * @return Lista de todas las reservas del barbero ordenadas por fecha
     *         ascendente
     */
    public List<Reserva> obtenerTodasReservasBarbero(Long idBarbero) {
        // Usa el nuevo método del repositorio que obtiene todas las reservas ordenadas
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

    @Transactional
    public void cambiarEstadoBarbero(Long idBarbero, boolean activo) {
        Barbero barbero = obtenerBarberoPorId(idBarbero);
        barbero.setActivo(activo);
        barberoRepository.save(barbero);
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

        // Validaciones según tipo de ausencia
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

        // Verificar que la reserva pertenece al barbero
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
    }

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

        // VALIDAR FECHAS SEGÚN TIPO DE AUSENCIA
        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            if (solicitud.getFechaInicio() == null) {
                throw new IllegalStateException("La solicitud no tiene fecha de inicio válida. No se puede aprobar.");
            }
            // Si no hay fecha fin, usar fecha inicio como fecha fin
            if (solicitud.getFechaFin() == null) {
                solicitud.setFechaFin(solicitud.getFechaInicio());
            }
        } else if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            if (solicitud.getFecha() == null) {
                throw new IllegalStateException("La solicitud no tiene fecha válida. No se puede aprobar.");
            }
            if (solicitud.getHoraInicio() == null || solicitud.getHoraFin() == null) {
                throw new IllegalStateException("La solicitud no tiene horarios válidos. No se puede aprobar.");
            }
        }

        // CANCELAR RESERVAS EXISTENTES Y NOTIFICAR CLIENTES
        List<Reserva> reservasAfectadas = obtenerReservasAfectadasPorAusencia(solicitud);
        int reservasCanceladas = cancelarReservasYNotificarClientes(reservasAfectadas, solicitud);

        // Actualizar estado de la solicitud
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.APROBADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());

        if (comentario != null && !comentario.trim().isEmpty()) {
            solicitud.setMotivoRechazo(comentario);
        }

        solicitudAusenciaRepository.save(solicitud);

        // Crear notificación para el barbero
        String fechaTexto;
        try {
            fechaTexto = formatearFecha(solicitud);
        } catch (Exception e) {
            fechaTexto = "la fecha solicitada";
        }

        String mensaje = "Tu solicitud de ausencia para " + fechaTexto + " ha sido aprobada";
        if (reservasCanceladas > 0) {
            mensaje += ". Se cancelaron " + reservasCanceladas + " reserva(s) existente(s) y se notificó a los clientes.";
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

        // Crear notificación
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
            barbero.setActivo(true);
        }
        return barberoRepository.save(barbero);
    }

    @Transactional
    public void eliminar(Long id) {
        Barbero barbero = obtenerBarberoPorId(id);

        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                id, ahora, ahora.plusYears(1));

        long reservasActivas = reservasFuturas.stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();

        if (reservasActivas > 0) {
            throw new RuntimeException("No se puede eliminar el barbero porque tiene " +
                    reservasActivas + " reservas activas");
        }

        barberoRepository.delete(barbero);
    }

    public List<Barbero> obtenerTodos() {
        return barberoRepository.findAll();
    }

    public Barbero obtenerPorId(Long id) {
        Optional<Barbero> barbero = barberoRepository.findById(id);
        return barbero.orElse(null);
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

    public List<Notificacion> obtenerNotificacionesPorTipo(Long idBarbero,
            Notificacion.TipoNotificacion tipo) {
        return notificacionRepository.findByBarberoIdBarberoAndTipoOrderByFechaCreacionDesc(
                idBarbero, tipo);
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

    /**
     * Formatea las fechas de una solicitud para mostrar en notificaciones
     * Con validaciones robustas para evitar NullPointerException
     */
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
                    if (solicitud.getFechaFin() != null && !solicitud.getFechaInicio().equals(solicitud.getFechaFin())) {
                        return solicitud.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                " al " + solicitud.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                    return solicitud.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            }
        } catch (Exception e) {
            // Si hay cualquier error al formatear, devolver texto genérico
            return "la fecha solicitada";
        }
        return "la fecha solicitada";
    }

    /**
     * Obtiene las reservas que serán afectadas por una ausencia
     */
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

        // Filtrar solo las que no están canceladas o completadas
        return reservasAfectadas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA || 
                            r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Cancela las reservas afectadas y notifica a los clientes
     * @return número de reservas canceladas
     */
    private int cancelarReservasYNotificarClientes(List<Reserva> reservas, SolicitudAusencia solicitud) {
        int canceladas = 0;

        for (Reserva reserva : reservas) {
            try {
                // Cancelar la reserva
                reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
                reservaRepository.save(reserva);

                // Crear mensaje para el cliente
                String mensajeCliente = String.format(
                    "Lo sentimos, tu reserva del %s a las %s con %s %s ha sido cancelada debido a que el barbero estará ausente. " +
                    "Por favor, agenda una nueva cita. Disculpa los inconvenientes.",
                    reserva.getFechaHoraTurno().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    reserva.getFechaHoraTurno().format(DateTimeFormatter.ofPattern("HH:mm")),
                    solicitud.getBarbero().getNombre(),
                    solicitud.getBarbero().getApellido()
                );

                // Enviar notificación al cliente
                enviarNotificacionCliente(reserva.getCliente(), mensajeCliente, reserva);

                canceladas++;
            } catch (Exception e) {
                // Log del error pero continuar con las demás reservas
                System.err.println("Error al cancelar reserva " + reserva.getIdReserva() + ": " + e.getMessage());
            }
        }

        return canceladas;
    }

    /**
     * Envía notificación al cliente sobre la cancelación
     * Puedes implementar esto con emails, SMS, o sistema de notificaciones web
     */
    private void enviarNotificacionCliente(com.pa.spring.prueba1.pa_prueba1.model.Cliente cliente, 
                                           String mensaje, 
                                           Reserva reserva) {
        try {
            // Enviar email al cliente
            emailService.notificarCancelacionPorAusencia(
                cliente, 
                reserva, 
                reserva.getBarbero(),
                "El barbero estará ausente en esta fecha"
            );
            
            System.out.println("✅ Email enviado a: " + cliente.getNombre() + " (" + cliente.getCorreo() + ")");
        } catch (Exception e) {
            // Si falla el email, al menos logeamos
            System.err.println("❌ Error al enviar email a " + cliente.getCorreo() + ": " + e.getMessage());
            System.out.println("NOTIFICACIÓN CLIENTE: " + cliente.getNombre() + " (" + cliente.getCorreo() + ") - " + mensaje);
        }
    }

    /**
     * Verifica si un barbero está disponible en una fecha/hora específica
     * considerando sus ausencias aprobadas
     */
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

    /**
     * Valida si se puede crear una reserva en la fecha/hora solicitada
     * Lanza excepción si el barbero está ausente
     */
    public void validarDisponibilidadParaReserva(Long idBarbero, LocalDateTime fechaHora) {
        if (!esBarberoDisponibleEnFechaHora(idBarbero, fechaHora)) {
            throw new RuntimeException("El barbero no está disponible en la fecha y hora seleccionadas debido a una ausencia programada. Por favor, selecciona otro horario.");
        }
    }

    /**
     * Obtiene las reservas de un barbero en un rango de fechas
     * Para estadísticas y reportes
     */
    public List<Reserva> obtenerReservasPorRangoFechas(Long idBarbero, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findByBarberoIdBarberoAndFechaHoraTurnoBetween(idBarbero, fechaInicio, fechaFin);
    }
    // AGREGAR ESTOS MÉTODOS AL FINAL DE TU CLASE BarberoService (antes del último })

    // ==================== NUEVOS MÉTODOS PARA NOTIFICACIONES CON PAGINACIÓN ====================

    /**
     * Obtiene notificaciones con paginación
     */
    public Page<Notificacion> obtenerNotificacionesBarberoP(Long idBarbero, Pageable pageable) {
        return notificacionRepository.findByBarberoIdBarberoOrderByFechaCreacionDesc(idBarbero, pageable);
    }

    /**
     * Obtiene notificaciones no leídas con paginación
     */
    public Page<Notificacion> obtenerNotificacionesNoLeidasPaginadas(Long idBarbero, Pageable pageable) {
        return notificacionRepository.findByBarberoIdBarberoAndLeidaOrderByFechaCreacionDesc(
            idBarbero, false, pageable
        );
    }

    /**
     * Obtiene notificaciones por tipo con paginación
     */
    public Page<Notificacion> obtenerNotificacionesPorTipoPaginadas(
            Long idBarbero, 
            Notificacion.TipoNotificacion tipo, 
            Pageable pageable) {
        return notificacionRepository.findByBarberoIdBarberoAndTipoOrderByFechaCreacionDesc(
            idBarbero, tipo, pageable
        );
    }

    /**
     * Busca notificaciones por texto
     */
    public Page<Notificacion> buscarNotificaciones(Long idBarbero, String texto, Pageable pageable) {
        return notificacionRepository.buscarPorTexto(idBarbero, texto, pageable);
    }

    /**
     * Cuenta notificaciones por múltiples tipos
     */
    public long contarNotificacionesPorTipos(Long idBarbero, List<Notificacion.TipoNotificacion> tipos) {
        return notificacionRepository.countByBarberoIdBarberoAndTipoIn(idBarbero, tipos);
    }

    /**
     * Cuenta notificaciones por un tipo específico
     */
    public long contarNotificacionesPorTipo(Long idBarbero, Notificacion.TipoNotificacion tipo) {
        return notificacionRepository.countByBarberoIdBarberoAndTipo(idBarbero, tipo);
    }

    /**
     * Obtiene las últimas N notificaciones
     */
    public List<Notificacion> obtenerUltimasNotificaciones(Long idBarbero, int cantidad) {
        Pageable pageable = PageRequest.of(0, cantidad, Sort.by("fechaCreacion").descending());
        Page<Notificacion> page = notificacionRepository.findByBarberoIdBarberoOrderByFechaCreacionDesc(
            idBarbero, pageable
        );
        return page.getContent();
    }

    /**
     * Marca todas las notificaciones como leídas (versión optimizada)
     */
    @Transactional
    public void marcarTodasComoLeidasOptimizado(Long idBarbero) {
        notificacionRepository.marcarTodasComoLeidas(idBarbero);
    }

    /**
     * Guarda configuración de notificaciones del barbero
     */
    @Transactional
    public void guardarConfiguracionNotificaciones(Long idBarbero, Map<String, Boolean> configuracion) {
        // Por ahora solo validamos que el barbero existe
        Barbero barbero = obtenerBarberoPorId(idBarbero);
        
        // Aquí puedes implementar la lógica para guardar las preferencias
        // Por ejemplo, en una tabla separada o en un campo JSON en Barbero
        System.out.println("✅ Configuración guardada para barbero: " + barbero.getNombre());
        System.out.println("Configuración: " + configuracion);
        
        // TODO: Implementar guardado real de preferencias según tu modelo de datos
    }

    /**
     * Obtiene notificaciones recientes (últimas 5) para el dropdown del navbar
     */
    public List<Notificacion> obtenerNotificacionesRecientes(Long idBarbero) {
        return obtenerUltimasNotificaciones(idBarbero, 5);
    }
}