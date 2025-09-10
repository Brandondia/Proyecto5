package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository // Marca esta interfaz como un componente de acceso a datos gestionado por Spring.
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // Encuentra todas las reservas por el ID del cliente.
    List<Reserva> findByClienteIdCliente(Long idCliente);
    
    // Encuentra todas las reservas por el ID del barbero.
    List<Reserva> findByBarberoIdBarbero(Long idBarbero);
    
    // Encuentra todas las reservas que tienen el estado dado.
    List<Reserva> findByEstado(Reserva.EstadoReserva estado);
    
    // Encuentra todas las reservas cuya fecha de turno esté dentro del rango proporcionado.
    List<Reserva> findByFechaHoraTurnoBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Encuentra todas las reservas de un barbero específico que estén en el estado dado.
    List<Reserva> findByBarberoIdBarberoAndEstado(Long idBarbero, Reserva.EstadoReserva estado);
    
    // Encuentra todas las reservas por el ID del turno.
    List<Reserva> findByTurnoIdTurno(Long idTurno);

    boolean existsByCliente_IdCliente(Long idCliente);
    
    // Método para contar la cantidad total de reservas en la base de datos.
    @Query("SELECT COUNT(r) FROM Reserva r")
    long countAllReservas();
}

