package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.Entity; // Anotación que indica que la clase es una entidad JPA (tabla en la BD)
import jakarta.persistence.GeneratedValue; // Anotación para indicar la generación automática de valores
import jakarta.persistence.GenerationType; // Tipo de estrategia de generación para claves primarias
import jakarta.persistence.Id; // Anotación que define la clave primaria
import lombok.Data; // Lombok: genera getters, setters, toString, equals y hashCode automáticamente

@Entity // Marca esta clase como una entidad persistente (tabla en la base de datos)
@Data // Lombok se encarga de generar automáticamente los métodos comunes (get/set/etc.)
public class CorteDeCabello {

    @Id // Indica que este campo es la clave primaria de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Genera el ID automáticamente con autoincremento
    private Long id; // Identificador único del corte

    private String nombre; // Nombre del corte, por ejemplo: "Corte Clásico"
    private double precio; // Precio del corte en moneda local
    private int duracion; // Duración del corte en minutos 
}
