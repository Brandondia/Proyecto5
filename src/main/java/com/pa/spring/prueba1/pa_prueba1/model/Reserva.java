package com.pa.spring.prueba1.pa_prueba1.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    private LocalDateTime fechaHoraReserva;
    private LocalDateTime fechaHoraTurno;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCliente", referencedColumnName = "idCliente")
    @JsonIgnore   // ⛔ Evita ciclo Cliente ↔ Reserva
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idBarbero", referencedColumnName = "idBarbero")
    @JsonIgnore   // ⛔ Evita ciclo Barbero ↔ Reserva
    private Barbero barbero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCorte", referencedColumnName = "id")
    private CorteDeCabello corte;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idTurno", referencedColumnName = "idTurno")
    @JsonBackReference   // ✅ Rompe ciclo Reserva ↔ Turno
    private Turno turno;

    @Column(name = "comentarios", length = 100)
    private String comentarios;

    public enum EstadoReserva {
        PENDIENTE,
        COMPLETADA,
        CANCELADA
    }
}


