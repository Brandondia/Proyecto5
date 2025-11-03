package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Para buscar un cliente por correo
    Optional<Cliente> findByCorreo(String correo);

    // Para verificar existencia rápida por correo
    boolean existsByCorreo(String correo);
}







