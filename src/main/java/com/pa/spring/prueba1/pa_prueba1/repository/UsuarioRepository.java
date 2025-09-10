package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Buscar un usuario por su username (para login)
    Optional<Usuario> findByUsername(String username);

    // Verificar si existe un username (para registro)
    boolean existsByUsername(String username);
}


