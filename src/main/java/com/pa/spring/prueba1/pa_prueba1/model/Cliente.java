package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false) 
    private String correo; // será nuestro "username" en login

    @Column(nullable = false)
    private String clave; // contraseña (se guarda encriptada con BCrypt)

    private String telefono;

    private boolean activo = true;

    private String rol = "ROLE_USER"; // por defecto todos serán USER
}


