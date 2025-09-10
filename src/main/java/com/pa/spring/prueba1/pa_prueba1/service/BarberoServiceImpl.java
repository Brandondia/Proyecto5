package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BarberoServiceImpl implements BarberoService {

    // Inyectamos el repositorio de Barbero para interactuar con la base de datos
    @Autowired
    private BarberoRepository barberoRepository;

    // Método para obtener todos los barberos
    @Override
    public List<Barbero> obtenerTodos() {
        return barberoRepository.findAll(); // Retorna todos los barberos usando el método findAll() de JpaRepository
    }

    // Método para obtener un barbero por su ID
    @Override
    public Barbero obtenerPorId(Long id) {
        // Busca un barbero por su ID. Si no se encuentra, devuelve null
        Optional<Barbero> barbero = barberoRepository.findById(id);
        return barbero.orElse(null); // Retorna el barbero si lo encuentra, o null si no
    }

    // Método para guardar o crear un nuevo barbero
    @Override
    public Barbero guardar(Barbero barbero) {
        return barberoRepository.save(barbero); // Guarda el barbero en la base de datos
    }

    // Método para actualizar los detalles de un barbero existente
    @Override
    public Barbero actualizar(Long id, Barbero barbero) {
        barbero.setIdBarbero(id); // Establece el ID del barbero para asegurarse de que se actualice el correcto
        return barberoRepository.save(barbero); // Guarda el barbero actualizado
    }

    // Método para eliminar un barbero por su ID
    @Override
    public void eliminar(Long id) {
        barberoRepository.deleteById(id); // Elimina el barbero con el ID especificado de la base de datos
    }
}

