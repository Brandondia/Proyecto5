package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import com.pa.spring.prueba1.pa_prueba1.repository.CorteDeCabelloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CorteDeCabelloServiceImpl implements CorteDeCabelloService {

    // Inyección del repositorio para interactuar con la base de datos
    @Autowired
    private CorteDeCabelloRepository corteDeCabelloRepository;

    @Override
    // Obtiene todos los cortes de cabello ACTIVOS (no eliminados)
    public List<CorteDeCabello> obtenerTodos() {
        // ✅ CAMBIO: Ahora solo retorna servicios activos
        return corteDeCabelloRepository.findByActivoTrue();
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
        // ✅ NUEVO: Asegura que nuevos servicios estén activos por defecto
        if (corteDeCabello.getActivo() == null) {
            corteDeCabello.setActivo(true);
        }
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
            actualizarCorte.setDuracion(corteDeCabello.getDuracion());
            // Mantiene el estado activo existente
            return corteDeCabelloRepository.save(actualizarCorte);
        } else {
            // Si no se encuentra, retorna null
            return null;
        }
    }

    @Override
    @Transactional
    // ✅ CAMBIO IMPORTANTE: Eliminación LÓGICA en lugar de física
    // Marca el servicio como inactivo en lugar de eliminarlo de la base de datos
    public void eliminar(Long id) {
        // Busca el servicio por ID
        CorteDeCabello corte = corteDeCabelloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        
        // Marca como inactivo (eliminación lógica)
        corte.setActivo(false);
        
        // Guarda el cambio en la base de datos
        corteDeCabelloRepository.save(corte);
        
        // Log para confirmar la operación
        System.out.println("✓ Servicio desactivado (eliminación lógica): " + corte.getNombre() + " [ID: " + id + "]");
    }
}