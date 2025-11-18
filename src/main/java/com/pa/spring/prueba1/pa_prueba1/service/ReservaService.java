package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaService {

    // ==================== MÉTODOS BÁSICOS ====================
    
    // Obtiene todas las reservas
    List<Reserva> obtenerTodas();

    // Obtiene una reserva por su ID
    Reserva obtenerPorId(Long id);

    // Obtiene las reservas de un cliente específico
    List<Reserva> obtenerPorCliente(Long idCliente);

    // Obtiene las reservas de un barbero específico
    List<Reserva> obtenerPorBarbero(Long idBarbero);

    // Obtiene las reservas filtradas por su estado
    List<Reserva> obtenerPorEstado(Reserva.EstadoReserva estado);

    // Obtiene las reservas dentro de un rango de fechas
    List<Reserva> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

    // Crea una nueva reserva, relacionando cliente, barbero, corte, turno y comentarios
    Reserva crearReserva(Long idCliente, Long idBarbero, Long idCorte, Long idTurno, String comentarios);

    // Marca una reserva como completada
    Reserva completarReserva(Long idReserva);

    // Cancela una reserva
    Reserva cancelarReserva(Long idReserva);

    // Elimina una reserva por su ID
    void eliminarReserva(Long idReserva);

    // Verifica si existe una reserva para un turno específico
    boolean existeReservaParaTurno(Long idTurno);

    // ==================== MÉTODOS NUEVOS PARA DASHBOARD DE CLIENTE ====================
    
    /**
     * Obtiene las reservas de un cliente filtradas por estado
     * @param idCliente ID del cliente
     * @param estado Estado de la reserva (PENDIENTE, COMPLETADA, CANCELADA)
     * @return Lista de reservas ordenadas por fecha descendente
     */
    List<Reserva> obtenerPorClienteYEstado(Long idCliente, Reserva.EstadoReserva estado);
    
    /**
     * Cuenta las reservas de un cliente por estado
     * @param idCliente ID del cliente
     * @param estado Estado a contar
     * @return Cantidad de reservas
     */
    long contarPorClienteYEstado(Long idCliente, Reserva.EstadoReserva estado);

    /**
     * Valida si un cliente puede hacer una nueva reserva
     * @param idCliente ID del cliente
     * @throws IllegalStateException si el cliente excede los límites
     */
    void validarLimitesReserva(Long idCliente);
}