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
    
    // Crear una nueva reserva con las relaciones cliente, barbero, corte, y turno
    @Override
@Transactional
public Reserva crearReserva(Long idCliente, Long idBarbero, Long idCorte, Long idTurno, String comentarios) {
    try {
        // Buscar entidades necesarias
        Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
        Barbero barbero = barberoRepository.findById(idBarbero).orElse(null);
        CorteDeCabello corte = corteRepository.findById(idCorte).orElse(null);
        Turno turno = turnoRepository.findById(idTurno).orElse(null);

        if (cliente == null || barbero == null || corte == null || turno == null) {
            System.out.println("Error: Falta alguna entidad requerida");
            return null;
        }

        // Evitar reservas duplicadas para el mismo turno
        boolean yaExiste = reservaRepository
                .findByTurnoIdTurno(idTurno)
                .stream()
                .anyMatch(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE);
        if (yaExiste) {
            System.out.println("⚠️ Ya existe una reserva pendiente para este turno.");
            return null;
        }

        // Validar disponibilidad
        if (turno.getEstado() != Turno.EstadoTurno.DISPONIBLE) {
            System.out.println("Error: El turno no está disponible");
            return null;
        }

        // Crear y guardar la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setBarbero(barbero);
        reserva.setCorte(corte);
        reserva.setTurno(turno);
        reserva.setFechaHoraReserva(LocalDateTime.now());
        reserva.setFechaHoraTurno(turno.getFechaHora());
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reserva.setComentarios(comentarios);

        // Guardar la reserva primero
        Reserva reservaGuardada = reservaRepository.save(reserva);

        // Luego actualizar el turno sin cascada
        turno.setEstado(Turno.EstadoTurno.NO_DISPONIBLE);
        turnoRepository.save(turno);

        System.out.println("✅ Reserva creada correctamente con ID: " + reservaGuardada.getIdReserva());
        return reservaGuardada;

    } catch (Exception e) {
        System.out.println("❌ Error al crear reserva: " + e.getMessage());
        e.printStackTrace();
        throw e;
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
    
    // Cancelar una reserva (cambiar su estado a CANCELADA y liberar el turno)
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
            return null;
        }
        
        // Actualizar el estado de la reserva
        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        
        // Liberar el turno
        Turno turno = reserva.getTurno();
        if (turno != null) {
            turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
            turnoRepository.save(turno);
        }
        
        return reservaRepository.save(reserva);
    }
    
    // Eliminar una reserva (liberar el turno si es necesario)
    @Override
    @Transactional
    public void eliminarReserva(Long idReserva) {
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isPresent()) {
            Reserva reserva = optReserva.get();
            
            // Liberar el turno si la reserva está pendiente
            if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE) {
                Turno turno = reserva.getTurno();
                if (turno != null) {
                    turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                    turnoRepository.save(turno);
                }
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
