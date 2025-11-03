package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    // Buscar por correo
    Optional<Administrador> findByCorreo(String correo);

    // Verificar si existe por correo
    boolean existsByCorreo(String correo);
}












