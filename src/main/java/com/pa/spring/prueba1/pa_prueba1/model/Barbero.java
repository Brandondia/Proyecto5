package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*; // Importaciones para las anotaciones JPA
import lombok.Data; // Lombok para la generación automática de métodos getter, setter, etc.

import java.time.DayOfWeek; // Para representar días de la semana
import java.time.LocalTime; // Para representar horas específicas
import java.util.ArrayList; // Lista dinámica
import java.util.List; // Interfaz de lista

@Entity // Marca la clase como una entidad JPA para mapeo a una tabla en la base de datos
@Data // Lombok genera automáticamente los métodos getter, setter, equals, hashCode y toString
public class Barbero {

    @Id // Define el campo como el identificador de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El valor del ID se genera automáticamente, típicamente en bases de datos con auto incremento
    private Long idBarbero; // Identificador único del barbero
    
    private String nombre; // Nombre completo del barbero
    private String especialidad; // Especialidad o área de enfoque del barbero (por ejemplo, cortes, afeitado, etc.)
    
    // Día libre del barbero (1 = Lunes, 7 = Domingo)
    private DayOfWeek diaLibre = DayOfWeek.SUNDAY; // El día libre predeterminado es el domingo
    
    // Hora de inicio de jornada (por defecto 9:00 AM)
    private LocalTime horaInicio = LocalTime.of(9, 0); 
    
    // Hora de fin de jornada (por defecto 6:00 PM)
    private LocalTime horaFin = LocalTime.of(18, 0); 
    
    // Hora de inicio de almuerzo (por defecto 1:00 PM)
    private LocalTime horaInicioAlmuerzo = LocalTime.of(13, 0); 
    
    // Hora de fin de almuerzo (por defecto 3:00 PM)
    private LocalTime horaFinAlmuerzo = LocalTime.of(15, 0); 
    
    // Duración de cada turno en minutos (por defecto 30 minutos)
    private int duracionTurno = 30; 
    
    // Relación con la entidad Turno (un barbero tiene muchos turnos)
    @OneToMany(mappedBy = "barbero", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Turno> turnos = new ArrayList<>(); // Lista de turnos del barbero
}
