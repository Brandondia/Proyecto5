package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter @Setter
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBarbero;

    // ==================== INFORMACIÓN BÁSICA ====================
    
    @Column(nullable = false)
    private String nombre;

    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    private String telefono;

    @Column(nullable = false)
    private String password;

    // ==================== INFORMACIÓN PERSONAL ====================
    
    @Column(unique = true)
    private String documento;
    
    private LocalDate fechaNacimiento;
    
    private String direccion;

    // ==================== INFORMACIÓN PROFESIONAL ====================
    
    private String especialidad;
    
    private Integer experienciaAnios;
    
    private LocalDate fechaIngreso;
    
    @Column(columnDefinition = "TEXT")
    private String certificaciones;

    // ==================== CONFIGURACIÓN DE HORARIO ====================
    
    @Enumerated(EnumType.STRING)
    private DayOfWeek diaLibre;

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalTime horaInicioAlmuerzo;
    private LocalTime horaFinAlmuerzo;
    
    private Integer duracionTurno; // en minutos

    // ==================== PERFIL Y SEGURIDAD ====================
    
    private String fotoPerfil;
    
    private LocalDateTime ultimaSesion;
    
    @Column(nullable = false)
    private Boolean autenticacionDosPasos = false;

    /**
     * Rol del usuario para Spring Security.
     * CRÍTICO: Este campo es necesario para la autenticación.
     * Por defecto: ROLE_BARBERO
     */
    @Column(nullable = false)
    private String rol = "ROLE_BARBERO";

    // ==================== PREFERENCIAS DE NOTIFICACIONES ====================
    
    @Column(nullable = false)
    private Boolean notifReservas = true;
    
    @Column(nullable = false)
    private Boolean notifCancelaciones = true;
    
    @Column(nullable = false)
    private Boolean notifRecordatorios = true;

    // ==================== ESTADO ====================
    
    @Column(nullable = false)
    private boolean activo = true;

    // ==================== CONSTRUCTOR ====================
    
    public Barbero() {
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Obtiene el nombre completo del barbero
     */
    public String getNombreCompleto() {
        if (apellido != null && !apellido.isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }
    
    /**
     * Verifica si el barbero tiene foto de perfil personalizada
     */
    public boolean tieneFotoPerfil() {
        return fotoPerfil != null && !fotoPerfil.isEmpty();
    }
    
    /**
     * Verifica si el barbero está disponible en un día específico
     */
    public boolean estaDisponibleEnDia(DayOfWeek dia) {
        return activo && !dia.equals(diaLibre);
    }
    
    /**
     * Verifica si tiene configuración de horario completa
     */
    public boolean tieneHorarioCompleto() {
        return horaInicio != null && horaFin != null && duracionTurno != null;
    }
}
