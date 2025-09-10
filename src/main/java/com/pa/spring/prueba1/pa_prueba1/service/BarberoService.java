package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import java.util.List;

// Interfaz para el servicio de Barbero
public interface BarberoService {

    // Método para obtener todos los barberos
    List<Barbero> obtenerTodos();
    
    // Método para obtener un barbero por su ID
    Barbero obtenerPorId(Long id);
    
    // Método para guardar un nuevo barbero o actualizar uno existente
    Barbero guardar(Barbero barbero);
    
    // Método para actualizar los datos de un barbero existente
    Barbero actualizar(Long id, Barbero barbero);
    
    // Método para eliminar un barbero por su ID
    void eliminar(Long id);
}

