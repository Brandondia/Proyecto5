package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import java.util.List;

// Interfaz que define los métodos necesarios para gestionar los cortes de cabello
public interface CorteDeCabelloService {
    
    // Método para obtener todos los cortes de cabello registrados
    List<CorteDeCabello> obtenerTodos();
    
    // Método para obtener un corte de cabello específico por su ID
    CorteDeCabello obtenerPorId(Long id);
    
    // Método para guardar un nuevo corte de cabello o actualizar uno existente
    CorteDeCabello guardar(CorteDeCabello corteDeCabello);
    
    // Método para actualizar los detalles de un corte de cabello específico
    CorteDeCabello actualizar(Long id, CorteDeCabello corteDeCabello);
    
    // Método para eliminar un corte de cabello por su ID
    void eliminar(Long id);
}

