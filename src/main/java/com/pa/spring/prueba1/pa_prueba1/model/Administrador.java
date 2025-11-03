package com.pa.spring.prueba1.pa_prueba1.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Data
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String usuario;   // <--- Nuevo campo para login con nombre de usuario

    @Column(unique = true, nullable = false)
    private String correo;    // para login con correo

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "administrador_roles",
        joinColumns = @JoinColumn(name = "administrador_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles;

    private Integer nivelAcceso = 3;
}





