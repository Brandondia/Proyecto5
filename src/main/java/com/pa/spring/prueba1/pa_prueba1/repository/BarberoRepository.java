package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository // Indica que esta interfaz es un repositorio de Spring (para acceso a datos y autoinyección).
public interface BarberoRepository extends JpaRepository<Barbero, Long> {

    @SuppressWarnings("null") // Suprime una advertencia sobre posibles valores nulos (normalmente innecesario aquí).
    List<Barbero> findAll(); // Obtiene todos los barberos desde la base de datos.

    // Método adicional que garantiza que nunca se devuelva null.
    default List<Barbero> findAllNonNull() {
        List<Barbero> barberos = findAll(); // Llama al método findAll() heredado de JpaRepository.
        return barberos == null ? new ArrayList<>() : barberos; // Si es null, retorna una lista vacía.
    }
}

