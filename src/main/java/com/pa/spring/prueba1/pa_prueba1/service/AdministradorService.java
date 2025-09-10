package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import java.util.List;

// Interfaz que define los métodos de servicio para la entidad Administrador
public interface AdministradorService {
    
    // Obtiene una lista con todos los administradores.
    List<Administrador> obtenerTodos();
    
    // Obtiene un administrador por su ID.
    Administrador obtenerPorId(Long id);
    
    // Guarda o actualiza un administrador.
    Administrador guardar(Administrador administrador);
    
    // Elimina un administrador por su ID.
    void eliminar(Long id);
    
    // Verifica si las credenciales de un administrador son correctas.
    Administrador verificarCredenciales(String usuario, String password);
    
    // Verifica si ya existe un administrador con un nombre de usuario específico.
    boolean existeAdministrador(String usuario);
}

