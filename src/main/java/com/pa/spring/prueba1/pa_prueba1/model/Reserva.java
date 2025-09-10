package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*; // Importa todas las anotaciones de JPA necesarias
import lombok.Data; // Lombok genera getters, setters, toString, etc.
import java.time.LocalDateTime; // Clase para manejar fecha y hora

@Entity // Indica que esta clase será mapeada como una entidad JPA
@Data // Lombok genera automáticamente getters/setters y otros métodos útiles
public class Reserva {

    @Id // Define la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental (auto-generado por la BD)
    private Long idReserva; // Identificador único de la reserva

    private LocalDateTime fechaHoraReserva; // Momento en que se hizo la reserva

    private LocalDateTime fechaHoraTurno; // Fecha y hora del turno reservado (registro histórico)

    @Enumerated(EnumType.STRING) // Guarda el valor del enum como texto (no como ordinal)
    private EstadoReserva estado = EstadoReserva.PENDIENTE; // Estado de la reserva

    @ManyToOne(fetch = FetchType.EAGER) // Relación muchos-a-uno con Cliente
    @JoinColumn(name = "idCliente", referencedColumnName = "idCliente") // Clave foránea en BD
    private Cliente cliente; // Cliente que hace la reserva

    @ManyToOne(fetch = FetchType.EAGER) // Relación muchos-a-uno con Barbero
    @JoinColumn(name = "idBarbero", referencedColumnName = "idBarbero")
    private Barbero barbero; // Barbero asignado

    @ManyToOne(fetch = FetchType.EAGER) // Relación muchos-a-uno con CorteDeCabello
    @JoinColumn(name = "idCorte", referencedColumnName = "id")
    private CorteDeCabello corte; // Corte solicitado

    @ManyToOne(fetch = FetchType.EAGER) // Relación muchos-a-uno con Turno
    @JoinColumn(name = "idTurno", referencedColumnName = "idTurno")
    private Turno turno; // Turno asignado

    @Column(name = "comentarios", length = 100) // Comentarios con longitud máxima de 100 caracteres
    private String comentarios;

    // Enumeración para representar el estado de la reserva
    public enum EstadoReserva {
        PENDIENTE,
        COMPLETADA,
        CANCELADA
    }
}

