package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.Entity; // Anotación que marca la clase como una entidad de base de datos
import jakarta.persistence.GeneratedValue; // Define cómo se genera el valor del identificador
import jakarta.persistence.GenerationType; // Especifica el tipo de estrategia de generación del identificador
import jakarta.persistence.Id; // Define el identificador único de la entidad
import lombok.Data; // Lombok para generar automáticamente los métodos getter, setter, toString, etc.

@Entity // Marca la clase como una entidad JPA que será mapeada a una tabla en la base de datos
@Data // Lombok genera automáticamente los métodos getter, setter, equals, hashCode, y toString
public class Administrador {

    @Id // Define el campo como el identificador de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El valor del ID se genera automáticamente usando la estrategia de identidad (auto incremento)
    private Long id; // Identificador único del administrador
    
    private String nombre; // Nombre del administrador
    private String usuario; // Nombre de usuario del administrador
    private String password; // Contraseña del administrador
    private String email; // Correo electrónico del administrador
    
    // Nivel de acceso: 1 = básico, 2 = medio, 3 = completo
    private Integer nivelAcceso = 3; // Nivel de acceso predeterminado es 3 (completo)
}

