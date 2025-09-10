package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import com.pa.spring.prueba1.pa_prueba1.repository.CorteDeCabelloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CorteDeCabelloServiceImpl implements CorteDeCabelloService {

    // Inyección del repositorio para interactuar con la base de datos
    @Autowired
    private CorteDeCabelloRepository corteDeCabelloRepository;

    @Override
    // Obtiene todos los cortes de cabello registrados
    public List<CorteDeCabello> obtenerTodos() {
        return corteDeCabelloRepository.findAll();
    }

    @Override
    // Obtiene un corte de cabello específico por su ID
    public CorteDeCabello obtenerPorId(Long id) {
        // Si no se encuentra el corte, devuelve null
        return corteDeCabelloRepository.findById(id).orElse(null);
    }

    @Override
    // Guarda un nuevo corte de cabello o actualiza uno existente
    public CorteDeCabello guardar(CorteDeCabello corteDeCabello) {
        return corteDeCabelloRepository.save(corteDeCabello);
    }

    @Override
    // Actualiza los detalles de un corte de cabello si existe
    public CorteDeCabello actualizar(Long id, CorteDeCabello corteDeCabello) {
        // Se verifica si el corte de cabello existe
        Optional<CorteDeCabello> corteExistente = corteDeCabelloRepository.findById(id);
        if (corteExistente.isPresent()) {
            // Si existe, se actualizan los datos
            CorteDeCabello actualizarCorte = corteExistente.get();
            actualizarCorte.setNombre(corteDeCabello.getNombre());
            actualizarCorte.setPrecio(corteDeCabello.getPrecio());
            return corteDeCabelloRepository.save(actualizarCorte);
        } else {
            // Si no se encuentra, retorna null
            return null;
        }
    }

    @Override
    // Elimina un corte de cabello por su ID
    public void eliminar(Long id) {
        corteDeCabelloRepository.deleteById(id);
    }
}

