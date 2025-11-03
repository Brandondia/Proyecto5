package com.pa.spring.prueba1.pa_prueba1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "idTurno")
@ToString(of = {"idTurno", "fechaHora", "estado"}) // no imprime barbero ni reservas
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurno;

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private EstadoTurno estado = EstadoTurno.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)           // ⬅️ evita cargar el barbero siempre
    @JoinColumn(name = "idBarbero", referencedColumnName = "idBarbero")
    @JsonIgnore                                  // evita ciclos al serializar
    private Barbero barbero;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference                        // permite serializar reservas si se quiere
    private List<Reserva> reservas = new ArrayList<>();

    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setTurno(this);
    }

    public enum EstadoTurno {
        DISPONIBLE,
        NO_DISPONIBLE
    }
}



