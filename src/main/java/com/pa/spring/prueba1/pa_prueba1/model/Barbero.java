package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad Barbero con soporte para Soft Delete
 * 
 * @author Tu Nombre
 * @version 2.0 - Actualizado con soft delete y valores por defecto
 */
@Entity
@Getter 
@Setter
@Table(name = "barbero")
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBarbero")
    private Long idBarbero;

    // ==================== INFORMACIÓN BÁSICA ====================
    
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 255)
    private String password;

    // ==================== INFORMACIÓN PERSONAL ====================
    
    @Column(unique = true, length = 50)
    private String documento;
    
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    @Column(length = 200)
    private String direccion;

    // ==================== INFORMACIÓN PROFESIONAL ====================
    
    @Column(length = 100)
    private String especialidad;
    
    @Column(name = "experiencia_anios")
    private Integer experienciaAnios;
    
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;
    
    @Column(columnDefinition = "TEXT")
    private String certificaciones;

    // ==================== CONFIGURACIÓN DE HORARIO ====================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_libre", length = 20)
    private DayOfWeek diaLibre;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;
    
    @Column(name = "hora_fin")
    private LocalTime horaFin;
    
    @Column(name = "hora_inicio_almuerzo")
    private LocalTime horaInicioAlmuerzo;
    
    @Column(name = "hora_fin_almuerzo")
    private LocalTime horaFinAlmuerzo;
    
    @Column(name = "duracion_turno")
    private Integer duracionTurno; // en minutos

    // ==================== PERFIL Y SEGURIDAD ====================
    
    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;
    
    @Column(name = "ultima_sesion")
    private LocalDateTime ultimaSesion;
    
    @Column(name = "autenticacion_dos_pasos", nullable = false)
    private Boolean autenticacionDosPasos = false;

    /**
     * Rol del usuario para Spring Security.
     * CRÍTICO: Este campo es necesario para la autenticación.
     * Por defecto: ROLE_BARBERO
     */
    @Column(nullable = false, length = 50)
    private String rol = "ROLE_BARBERO";

    // ==================== PREFERENCIAS DE NOTIFICACIONES ====================
    
    @Column(name = "notif_reservas", nullable = false)
    private Boolean notifReservas = true;
    
    @Column(name = "notif_cancelaciones", nullable = false)
    private Boolean notifCancelaciones = true;
    
    @Column(name = "notif_recordatorios", nullable = false)
    private Boolean notifRecordatorios = true;

    // ==================== ESTADO Y SOFT DELETE ====================
    
    /**
     * Indica si el barbero está activo en el sistema.
     * true = activo (puede trabajar y recibir reservas)
     * false = desvinculado (soft delete - no puede trabajar pero mantiene historial)
     */
    @Column(nullable = false)
    private boolean activo = true;
    
    /**
     * Fecha en la que el barbero fue desvinculado.
     * NULL = barbero activo
     * NOT NULL = barbero desvinculado (fecha de desvinculación)
     */
    @Column(name = "fecha_desvinculacion")
    private LocalDateTime fechaDesvinculacion;
    
    /**
     * Motivo por el cual el barbero fue desvinculado.
     * NULL = barbero activo
     * NOT NULL = razón de la desvinculación
     */
    @Column(name = "motivo_desvinculacion", length = 500)
    private String motivoDesvinculacion;

    // ==================== CONSTRUCTORES ====================
    
    /**
     * Constructor por defecto con valores iniciales
     * Requerido por JPA
     */
    public Barbero() {
        // Inicializar valores por defecto
        this.activo = true;
        this.rol = "ROLE_BARBERO";
        this.autenticacionDosPasos = false;
        this.notifReservas = true;
        this.notifCancelaciones = true;
        this.notifRecordatorios = true;
    }
    
    /**
     * Constructor con datos básicos
     * @param nombre Nombre del barbero
     * @param email Email del barbero
     * @param password Contraseña del barbero
     */
    public Barbero(String nombre, String email, String password) {
        this(); // Llama al constructor por defecto primero
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    // ==================== CALLBACKS DE JPA ====================
    
    /**
     * Inicializa valores por defecto antes de persistir en la base de datos
     * Garantiza que ningún campo requerido quede nulo
     */
    @PrePersist
    protected void onCreate() {
        if (this.autenticacionDosPasos == null) {
            this.autenticacionDosPasos = false;
        }
        if (this.notifReservas == null) {
            this.notifReservas = true;
        }
        if (this.notifCancelaciones == null) {
            this.notifCancelaciones = true;
        }
        if (this.notifRecordatorios == null) {
            this.notifRecordatorios = true;
        }
        if (this.rol == null || this.rol.isEmpty()) {
            this.rol = "ROLE_BARBERO";
        }
        if (this.fechaIngreso == null) {
            this.fechaIngreso = LocalDate.now();
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Obtiene el nombre completo del barbero
     * @return nombre completo (nombre + apellido) o solo nombre si no tiene apellido
     */
    public String getNombreCompleto() {
        if (apellido != null && !apellido.trim().isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }
    
    /**
     * Verifica si el barbero tiene foto de perfil personalizada
     * @return true si tiene foto, false si no
     */
    public boolean tieneFotoPerfil() {
        return fotoPerfil != null && !fotoPerfil.trim().isEmpty();
    }
    
    /**
     * Verifica si el barbero está disponible en un día específico de la semana
     * @param dia día de la semana a verificar
     * @return true si está disponible, false si es su día libre o está inactivo
     */
    public boolean estaDisponibleEnDia(DayOfWeek dia) {
        return activo && !dia.equals(diaLibre);
    }
    
    /**
     * Verifica si tiene configuración de horario completa
     * Necesario para poder generar turnos automáticamente
     * @return true si tiene todos los datos de horario, false si falta alguno
     */
    public boolean tieneHorarioCompleto() {
        return horaInicio != null && horaFin != null && duracionTurno != null && duracionTurno > 0;
    }
    
    // ==================== MÉTODOS PARA SOFT DELETE ====================
    
    /**
     * Desvincula al barbero de la barbería (soft delete)
     * Marca el barbero como inactivo pero mantiene todos sus datos
     * 
     * @param motivo Razón de la desvinculación (obligatorio para auditoría)
     */
    public void desvincular(String motivo) {
        this.activo = false;
        this.fechaDesvinculacion = LocalDateTime.now();
        this.motivoDesvinculacion = motivo != null && !motivo.trim().isEmpty() 
            ? motivo 
            : "Desvinculado por el administrador";
    }
    
    /**
     * Reactiva al barbero en la barbería
     * Vuelve a activar al barbero y limpia los datos de desvinculación
     */
    public void reactivar() {
        this.activo = true;
        this.fechaDesvinculacion = null;
        this.motivoDesvinculacion = null;
    }
    
    /**
     * Verifica si el barbero está desvinculado
     * @return true si está desvinculado, false si está activo
     */
    public boolean estaDesvinculado() {
        return !activo;
    }
    
    /**
     * Obtiene una descripción del estado actual del barbero
     * @return descripción del estado (Activo/Desvinculado desde...)
     */
    public String getEstadoDescripcion() {
        if (activo) {
            return "Activo";
        } else {
            if (fechaDesvinculacion != null) {
                return "Desvinculado desde " + fechaDesvinculacion.toLocalDate();
            }
            return "Desvinculado";
        }
    }

    // ==================== EQUALS & HASHCODE ====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Barbero)) return false;
        Barbero barbero = (Barbero) o;
        return idBarbero != null && idBarbero.equals(barbero.idBarbero);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ==================== TO STRING ====================
    
    @Override
    public String toString() {
        return "Barbero{" +
                "id=" + idBarbero +
                ", nombre='" + getNombreCompleto() + '\'' +
                ", email='" + email + '\'' +
                ", activo=" + activo +
                ", especialidad='" + especialidad + '\'' +
                '}';
    }
}  