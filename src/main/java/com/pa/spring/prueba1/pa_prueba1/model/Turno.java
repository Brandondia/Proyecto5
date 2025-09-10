package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity // Marca esta clase como una entidad JPA para mapear a una tabla en la base de datos.
@Data // Genera automáticamente getters, setters, toString, equals y hashCode con Lombok.
public class Turno {

    @Id // Indica que este campo es la clave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Se genera automáticamente con autoincremento.
    private Long idTurno;

    private LocalDateTime fechaHora; // Fecha y hora exacta del turno.

    @Enumerated(EnumType.STRING) // Almacena el enum como texto ("DISPONIBLE", "NO_DISPONIBLE") en lugar de como ordinal (0, 1).
    private EstadoTurno estado = EstadoTurno.DISPONIBLE; // Estado por defecto: disponible.

    @ManyToOne // Muchos turnos pueden estar asociados a un mismo barbero.
    @JoinColumn(name = "idBarbero", referencedColumnName = "idBarbero") // Llave foránea que une con Barbero.
    private Barbero barbero;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // Un turno puede tener muchas reservas.
    private List<Reserva> reservas = new ArrayList<>(); // Lista de reservas asociadas a este turno.

    // Método para agregar una reserva a la lista y establecer la relación bidireccional.
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setTurno(this); // Asegura la relación de vuelta.
    }

    // Enum para definir los posibles estados de un turno.
    public enum EstadoTurno {
        DISPONIBLE,
        NO_DISPONIBLE
    }
}

