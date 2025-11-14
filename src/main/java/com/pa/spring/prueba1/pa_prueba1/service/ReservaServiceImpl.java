package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.*;
import com.pa.spring.prueba1.pa_prueba1.repository.*;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private BarberoRepository barberoRepository;
    
    @Autowired
    private CorteDeCabelloRepository corteRepository;
    
    @Autowired
    private TurnoRepository turnoRepository;
    
    // Obtener todas las reservas
    @Override
    public List<Reserva> obtenerTodas() {
        List<Reserva> reservas = reservaRepository.findAll();
        System.out.println("ReservaService.obtenerTodas(): Encontradas " + reservas.size() + " reservas");
        return reservas;
    }
    
    // Obtener una reserva por su ID
    @Override
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id).orElse(null);
    }
    
    // Obtener todas las reservas de un cliente específico
    @Override
    public List<Reserva> obtenerPorCliente(Long idCliente) {
        return reservaRepository.findByClienteIdCliente(idCliente);
    }
    
    // Obtener todas las reservas de un barbero específico
    @Override
    public List<Reserva> obtenerPorBarbero(Long idBarbero) {
        return reservaRepository.findByBarberoIdBarbero(idBarbero);
    }
    
    // Obtener reservas por su estado (PENDIENTE, COMPLETADA, CANCELADA)
    @Override
    public List<Reserva> obtenerPorEstado(Reserva.EstadoReserva estado) {
        return reservaRepository.findByEstado(estado);
    }
    
    // Obtener reservas dentro de un rango de fechas
    @Override
    public List<Reserva> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        return reservaRepository.findByFechaHoraTurnoBetween(inicio, fin);
    }
    
    // ✅ CREAR RESERVA CON VALIDACIONES COMPLETAS
    @Override
    @Transactional
    public Reserva crearReserva(Long idCliente, Long idBarbero, Long idCorte, Long idTurno, String comentarios) {
        try {
            // Buscar entidades necesarias
            Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
            Barbero barbero = barberoRepository.findById(idBarbero).orElse(null);
            CorteDeCabello corte = corteRepository.findById(idCorte).orElse(null);
            Turno turnoInicial = turnoRepository.findById(idTurno).orElse(null);

            if (cliente == null || barbero == null || corte == null || turnoInicial == null) {
                throw new IllegalArgumentException("Faltan datos requeridos para crear la reserva");
            }

            System.out.println("=== CREANDO RESERVA ===");
            System.out.println("Cliente: " + cliente.getNombre());
            System.out.println("Barbero: " + barbero.getNombre());
            System.out.println("Servicio: " + corte.getNombre());
            System.out.println("Duración: " + corte.getDuracion() + " minutos");
            System.out.println("Turno inicial: " + turnoInicial.getFechaHora());

            // ✅ VALIDACIÓN 1: Turno inicial debe estar disponible
            if (turnoInicial.getEstado() != Turno.EstadoTurno.DISPONIBLE) {
                throw new IllegalStateException("El turno seleccionado no está disponible");
            }

            // ✅ VALIDACIÓN 2: Evitar reservas duplicadas
            boolean yaExiste = reservaRepository
                    .findByTurnoIdTurno(idTurno)
                    .stream()
                    .anyMatch(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE);
            if (yaExiste) {
                throw new IllegalStateException("Ya existe una reserva pendiente para este turno");
            }

            // Calcular turnos necesarios
            int duracionMinutos = corte.getDuracion();
            int turnosNecesarios = (int) Math.ceil(duracionMinutos / 15.0);
            
            System.out.println("📋 Turnos consecutivos necesarios: " + turnosNecesarios);
            
            // ✅ VALIDACIÓN 3: Verificar que no se exceda el horario de trabajo del barbero
            LocalDateTime horaFinServicio = turnoInicial.getFechaHora().plusMinutes(duracionMinutos);
            LocalTime horaFinTrabajo = barbero.getHoraFin();
            LocalTime horaFinServicioTime = horaFinServicio.toLocalTime();
            
            if (horaFinServicioTime.isAfter(horaFinTrabajo)) {
                throw new IllegalStateException(
                    String.format("El servicio terminaría a las %s, pero el barbero finaliza su jornada a las %s. " +
                                 "Por favor selecciona un turno más temprano.",
                                 horaFinServicioTime, horaFinTrabajo)
                );
            }
            
            // ✅ VALIDACIÓN 4: Verificar que no se cruce con el horario de almuerzo
            if (barbero.getHoraInicioAlmuerzo() != null && barbero.getHoraFinAlmuerzo() != null) {
                LocalTime horaInicioAlmuerzo = barbero.getHoraInicioAlmuerzo();
                LocalTime horaFinAlmuerzo = barbero.getHoraFinAlmuerzo();
                LocalTime horaInicioServicio = turnoInicial.getFechaHora().toLocalTime();
                
                // Verificar si el servicio se cruza con el almuerzo
                boolean cruzaConAlmuerzo = 
                    (horaInicioServicio.isBefore(horaFinAlmuerzo) && horaFinServicioTime.isAfter(horaInicioAlmuerzo));
                
                if (cruzaConAlmuerzo) {
                    throw new IllegalStateException(
                        String.format("El servicio se cruza con el horario de almuerzo del barbero (%s - %s). " +
                                     "Por favor selecciona otro horario.",
                                     horaInicioAlmuerzo, horaFinAlmuerzo)
                    );
                }
            }
            
            // Obtener y validar todos los turnos consecutivos
            List<Turno> turnosABloquear = new ArrayList<>();
            turnosABloquear.add(turnoInicial);
            
            LocalDateTime fechaHoraActual = turnoInicial.getFechaHora();
            
            // Buscar y validar los turnos siguientes
            for (int i = 1; i < turnosNecesarios; i++) {
                LocalDateTime fechaHoraSiguiente = fechaHoraActual.plusMinutes(15);
                
                System.out.println("   Buscando turno para: " + fechaHoraSiguiente);
                
                // Buscar el siguiente turno
                List<Turno> siguienteTurno = turnoRepository.findByBarberoIdBarberoAndFechaHora(
                        idBarbero, fechaHoraSiguiente);
                
                // ✅ VALIDACIÓN 5: El turno consecutivo debe existir
                if (siguienteTurno.isEmpty()) {
                    throw new IllegalStateException(
                        String.format("No hay turnos disponibles suficientes para completar este servicio de %d minutos. " +
                                     "Falta el turno de las %s. Por favor selecciona otro horario.",
                                     duracionMinutos, fechaHoraSiguiente.toLocalTime())
                    );
                }
                
                Turno turno = siguienteTurno.get(0);
                
                // ✅ VALIDACIÓN 6: El turno consecutivo debe estar disponible
                if (turno.getEstado() != Turno.EstadoTurno.DISPONIBLE) {
                    throw new IllegalStateException(
                        String.format("El turno de las %s ya está ocupado. " +
                                     "No hay suficiente espacio consecutivo para este servicio de %d minutos.",
                                     fechaHoraSiguiente.toLocalTime(), duracionMinutos)
                    );
                }
                
                turnosABloquear.add(turno);
                fechaHoraActual = fechaHoraSiguiente;
            }
            
            System.out.println("✅ Todas las validaciones pasaron. Turnos a bloquear:");
            for (Turno t : turnosABloquear) {
                System.out.println("   - " + t.getFechaHora() + " (ID: " + t.getIdTurno() + ")");
            }

            // Crear la reserva
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setBarbero(barbero);
            reserva.setCorte(corte);
            reserva.setTurno(turnoInicial);
            reserva.setFechaHoraReserva(LocalDateTime.now());
            reserva.setFechaHoraTurno(turnoInicial.getFechaHora());
            reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
            reserva.setComentarios(comentarios);

            // Guardar la reserva
            Reserva reservaGuardada = reservaRepository.save(reserva);

            // Bloquear todos los turnos consecutivos
            System.out.println("🔒 Bloqueando turnos:");
            for (Turno turno : turnosABloquear) {
                turno.setEstado(Turno.EstadoTurno.NO_DISPONIBLE);
                turnoRepository.save(turno);
                System.out.println("   ✓ Turno bloqueado: " + turno.getFechaHora() + " (ID: " + turno.getIdTurno() + ")");
            }

            System.out.println("✅ Reserva creada exitosamente con ID: " + reservaGuardada.getIdReserva());
            System.out.println("======================");
            
            return reservaGuardada;

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("❌ Validación fallida: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ Error inesperado al crear reserva: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al crear la reserva: " + e.getMessage(), e);
        }
    }
    
    // Completar una reserva (cambiar su estado a COMPLETADA)
    @Override
    @Transactional
    public Reserva completarReserva(Long idReserva) {
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (!optReserva.isPresent()) {
            return null;
        }
        
        Reserva reserva = optReserva.get();
        
        // Verificar que la reserva esté pendiente
        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE) {
            return null;
        }
        
        // Actualizar el estado de la reserva
        reserva.setEstado(Reserva.EstadoReserva.COMPLETADA);
        
        return reservaRepository.save(reserva);
    }
    
    // ✅ CORREGIDO: Cancelar reserva liberando TODOS los turnos consecutivos
    @Override
    @Transactional
    public Reserva cancelarReserva(Long idReserva) {
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (!optReserva.isPresent()) {
            return null;
        }
        
        Reserva reserva = optReserva.get();
        
        // Verificar que la reserva esté pendiente
        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE) {
            System.out.println("⚠️ La reserva ya no está pendiente (Estado: " + reserva.getEstado() + ")");
            return null;
        }
        
        System.out.println("=== CANCELANDO RESERVA ===");
        System.out.println("Reserva ID: " + idReserva);
        System.out.println("Servicio: " + reserva.getCorte().getNombre());
        System.out.println("Duración: " + reserva.getCorte().getDuracion() + " minutos");
        
        // 🔥 CORREGIDO: Calcular cuántos turnos se bloquearon originalmente
        int duracionMinutos = reserva.getCorte().getDuracion();
        int turnosALiberar = (int) Math.ceil(duracionMinutos / 15.0);
        
        System.out.println("🔓 Turnos a liberar: " + turnosALiberar);
        
        // 🔥 CORREGIDO: Liberar todos los turnos consecutivos
        Turno turnoInicial = reserva.getTurno();
        if (turnoInicial != null) {
            LocalDateTime fechaHoraActual = turnoInicial.getFechaHora();
            
            for (int i = 0; i < turnosALiberar; i++) {
                List<Turno> turnos = turnoRepository.findByBarberoIdBarberoAndFechaHora(
                        reserva.getBarbero().getIdBarbero(), 
                        fechaHoraActual);
                
                if (!turnos.isEmpty()) {
                    Turno turno = turnos.get(0);
                    turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                    turnoRepository.save(turno);
                    System.out.println("   ✓ Turno liberado: " + turno.getFechaHora() + " (ID: " + turno.getIdTurno() + ")");
                } else {
                    System.out.println("   ⚠️ No se encontró turno para liberar: " + fechaHoraActual);
                }
                
                fechaHoraActual = fechaHoraActual.plusMinutes(15);
            }
        }
        
        // Actualizar estado de la reserva
        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        Reserva reservaActualizada = reservaRepository.save(reserva);
        
        System.out.println("✅ Reserva cancelada exitosamente");
        System.out.println("==========================");
        
        return reservaActualizada;
    }
    
    // ✅ CORREGIDO: Eliminar reserva liberando TODOS los turnos consecutivos
    @Override
    @Transactional
    public void eliminarReserva(Long idReserva) {
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isPresent()) {
            Reserva reserva = optReserva.get();
            
            //// Liberar los turnos si la reserva está pendiente
            if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE) {
                System.out.println("=== ELIMINANDO RESERVA Y LIBERANDO TURNOS ===");
                System.out.println("Reserva ID: " + idReserva);
                
                // 🔥 CORREGIDO: Calcular cuántos turnos liberar
                int duracionMinutos = reserva.getCorte().getDuracion();
                int turnosALiberar = (int) Math.ceil(duracionMinutos / 15.0);
                
                System.out.println("🔓 Turnos a liberar: " + turnosALiberar);
                
                // Liberar todos los turnos consecutivos
                Turno turnoInicial = reserva.getTurno();
                if (turnoInicial != null) {
                    LocalDateTime fechaHoraActual = turnoInicial.getFechaHora();
                    
                    for (int i = 0; i < turnosALiberar; i++) {
                        List<Turno> turnos = turnoRepository.findByBarberoIdBarberoAndFechaHora(
                                reserva.getBarbero().getIdBarbero(), 
                                fechaHoraActual);
                        
                        if (!turnos.isEmpty()) {
                            Turno turno = turnos.get(0);
                            turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                            turnoRepository.save(turno);
                            System.out.println("   ✓ Turno liberado: " + turno.getFechaHora());
                        }
                        
                        fechaHoraActual = fechaHoraActual.plusMinutes(15);
                    }
                }
                
                System.out.println("===================================");
            }
            
            reservaRepository.delete(reserva);
        }
    }
    
    // Verificar si existe una reserva pendiente para un turno específico
    @Override
    public boolean existeReservaParaTurno(Long idTurno) {
        List<Reserva> reservas = reservaRepository.findByTurnoIdTurno(idTurno);
        return !reservas.isEmpty() && 
               reservas.stream().anyMatch(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE);
    }

    // Inicialización del servicio para mostrar el estado de las reservas al iniciar la aplicación
    @PostConstruct
    public void init() {
        try {
            long count = reservaRepository.countAllReservas();
            System.out.println("=== INICIALIZACIÓN DE RESERVAS ===");
            System.out.println("Número total de reservas en la base de datos: " + count);

            if (count > 0) {
                List<Reserva> reservas = reservaRepository.findAll();
                System.out.println("Listado de reservas:");
                for (Reserva r : reservas) {
                    System.out.println("  - ID: " + r.getIdReserva() +
                            ", Cliente: " + (r.getCliente() != null ? r.getCliente().getNombre() : "null") +
                            ", Estado: " + r.getEstado() +
                            ", Fecha: " + r.getFechaHoraTurno());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al inicializar el servicio de reservas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}