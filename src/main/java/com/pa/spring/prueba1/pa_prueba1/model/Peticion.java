package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "peticiones")
@Data
public class Peticion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPeticion;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idBarbero", referencedColumnName = "idBarbero")
    private Barbero barbero;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPeticion tipo;
    
    @Column(nullable = false)
    private LocalDate fecha;
    
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;
    
    @Column(name = "hora_fin")
    private LocalTime horaFin;
    
    @Column(nullable = false, length = 500)
    private String motivo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPeticion estado = EstadoPeticion.PENDIENTE;
    
    @Column(name = "respuesta_admin", length = 500)
    private String respuestaAdmin;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();
    
    @Column(name = "fecha_respuesta")
    private LocalDate fechaRespuesta;
    
    public enum TipoPeticion {
        AUSENCIA,
        CAMBIO_HORARIO
    }
    
    public enum EstadoPeticion {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }
}
