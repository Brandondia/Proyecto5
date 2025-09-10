package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Buscar un rol por su nombre (ejemplo: "ADMIN", "BARBERO", "CLIENTE")
    Optional<Rol> findByNombre(String nombre);
}
