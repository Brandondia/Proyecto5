package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring (para inyección de dependencias).
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    
    // Busca un administrador por su nombre de usuario.
    Administrador findByUsuario(String usuario);
    
    // Busca un administrador por su nombre de usuario y contraseña (útil para autenticación simple).
    Administrador findByUsuarioAndPassword(String usuario, String password);
}
