package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @ManyToOne
    @JoinColumn(name = "id_barbero")
    private Barbero barbero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoNotificacion tipo;

    @Column(name = "titulo")
    private String titulo;
    
    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "leida")
    private Boolean leida = false;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Para notificaciones relacionadas con reservas
    @ManyToOne
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    /**
     * Enum para los tipos de notificaciones
     */
    public enum TipoNotificacion {
        NUEVA_RESERVA,
        RESERVA_CANCELADA,
        RESERVA_CONFIRMADA,
        AUSENCIA_APROBADA,
        AUSENCIA_RECHAZADA,
        RECORDATORIO,
        VALORACION,
        SISTEMA
    }

    // ==================== CONSTRUCTORES ====================
    
    public Notificacion() {
        this.leida = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Notificacion(Barbero barbero, TipoNotificacion tipo, String titulo, String mensaje) {
        this.barbero = barbero;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.leida = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    // ==================== GETTERS Y SETTERS ====================
    
    public Long getIdNotificacion() { 
        return idNotificacion; 
    }
    
    public void setIdNotificacion(Long idNotificacion) { 
        this.idNotificacion = idNotificacion; 
    }

    public Barbero getBarbero() { 
        return barbero; 
    }
    
    public void setBarbero(Barbero barbero) { 
        this.barbero = barbero; 
    }

    public TipoNotificacion getTipo() { 
        return tipo; 
    }
    
    public void setTipo(TipoNotificacion tipo) { 
        this.tipo = tipo; 
    }

    public String getTitulo() { 
        return titulo; 
    }
    
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }

    public String getMensaje() { 
        return mensaje; 
    }
    
    public void setMensaje(String mensaje) { 
        this.mensaje = mensaje; 
    }

    public Boolean getLeida() { 
        return leida != null ? leida : false; 
    }
    
    public void setLeida(Boolean leida) { 
        this.leida = leida; 
    }

    public LocalDateTime getFechaCreacion() { 
        return fechaCreacion; 
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }

    public Reserva getReserva() { 
        return reserva; 
    }
    
    public void setReserva(Reserva reserva) { 
        this.reserva = reserva; 
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    
    /**
     * Marca la notificación como leída
     */
    public void marcarComoLeida() {
        this.leida = true;
    }

    /**
     * Marca la notificación como no leída
     */
    public void marcarComoNoLeida() {
        this.leida = false;
    }

    /**
     * Verifica si la notificación es reciente (menos de 24 horas)
     */
    public boolean esReciente() {
        if (fechaCreacion == null) return false;
        return fechaCreacion.isAfter(LocalDateTime.now().minusHours(24));
    }

    // ==================== TOSTRING ====================
    
    @Override
    public String toString() {
        return "Notificacion{" +
                "id=" + idNotificacion +
                ", tipo=" + tipo +
                ", titulo='" + titulo + '\'' +
                ", leida=" + leida +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}