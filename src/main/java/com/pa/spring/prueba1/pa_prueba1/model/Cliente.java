package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "idCliente")      // solo compara por ID
@ToString(of = {"idCliente", "nombre", "correo"}) // evita imprimir relaciones
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String correo;  // será el "username" en login

    @Column(nullable = false)
    private String clave;   // contraseña encriptada con BCrypt

    private String telefono;

    private boolean activo = true;

    private String rol = "ROLE_USER"; // por defecto todos serán USER
}



