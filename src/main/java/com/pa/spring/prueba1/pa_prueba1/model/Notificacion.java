package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @ManyToOne
    @JoinColumn(name = "id_barbero")
    private Barbero barbero;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    private String titulo;
    private String mensaje;
    private Boolean leida = false;
    
    private LocalDateTime fechaCreacion;

    // Para notificaciones relacionadas con reservas
    @ManyToOne
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    public enum TipoNotificacion {
        NUEVA_RESERVA,
        RESERVA_CANCELADA,
        AUSENCIA_APROBADA,
        AUSENCIA_RECHAZADA,
        RECORDATORIO,
        VALORACION,
        SISTEMA
    }

    // Getters y Setters
    public Long getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Long idNotificacion) { this.idNotificacion = idNotificacion; }

    public Barbero getBarbero() { return barbero; }
    public void setBarbero(Barbero barbero) { this.barbero = barbero; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
}