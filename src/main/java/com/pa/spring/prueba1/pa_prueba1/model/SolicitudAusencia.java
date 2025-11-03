package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter @Setter
public class SolicitudAusencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @ManyToOne
    @JoinColumn(name = "id_barbero", nullable = false)
    private Barbero barbero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAusencia tipoAusencia = TipoAusencia.DIA_COMPLETO;

    // Para días completos
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    
    // Para ausencias parciales (horas específicas)
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    @Column(length = 500)
    private String motivo;
    
    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
    
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    private LocalDateTime fechaRespuesta;
    
    @Column(length = 500)
    private String motivoRechazo;

    /**
     * Tipo de ausencia
     */
    public enum TipoAusencia {
        DIA_COMPLETO,
        HORAS_ESPECIFICAS
    }

    /**
     * Enum para el estado de la solicitud
     */
    public enum EstadoSolicitud {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSolicitud.PENDIENTE;
        }
        if (tipoAusencia == null) {
            tipoAusencia = TipoAusencia.DIA_COMPLETO;
        }
    }
}
