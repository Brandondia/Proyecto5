package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import com.pa.spring.prueba1.pa_prueba1.service.CorteDeCabelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para manejar las operaciones CRUD sobre los cortes de cabello
 * a través de una API REST.
 */
@RestController
@RequestMapping("/api/cortes") // Ruta para acceder a los cortes de cabello
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen
public class CorteDeCabelloController {

    @Autowired
    private CorteDeCabelloService corteDeCabelloService;

    /**
     * Obtiene la lista de todos los cortes de cabello.
     * 
     * @return lista de cortes de cabello
     */
    @GetMapping
    public List<CorteDeCabello> listarCortes() {
        return corteDeCabelloService.obtenerTodos(); // Retorna todos los cortes de cabello desde el servicio
    }

    /**
     * Obtiene un corte de cabello específico por su ID.
     * 
     * @param id ID del corte de cabello
     * @return el corte de cabello correspondiente
     */
    @GetMapping("/{id}")
    public CorteDeCabello obtenerCorte(@PathVariable Long id) {
        return corteDeCabelloService.obtenerPorId(id); // Obtiene el corte de cabello por su ID
    }

    /**
     * Crea un nuevo corte de cabello.
     * 
     * @param corteDeCabello el corte de cabello a crear
     * @return el corte de cabello creado
     */
    @PostMapping
    public CorteDeCabello crearCorte(@RequestBody CorteDeCabello corteDeCabello) {
        return corteDeCabelloService.guardar(corteDeCabello); // Guarda el nuevo corte de cabello
    }

    /**
     * Actualiza un corte de cabello existente por su ID.
     * 
     * @param id ID del corte de cabello a actualizar
     * @param corteDeCabello el corte de cabello con los nuevos datos
     * @return el corte de cabello actualizado
     */
    @PutMapping("/{id}")
    public CorteDeCabello actualizarCorte(@PathVariable Long id, @RequestBody CorteDeCabello corteDeCabello) {
        return corteDeCabelloService.actualizar(id, corteDeCabello); // Actualiza el corte de cabello
    }

    /**
     * Elimina un corte de cabello por su ID.
     * 
     * @param id ID del corte de cabello a eliminar
     */
    @DeleteMapping("/{id}")
    public void eliminarCorte(@PathVariable Long id) {
        corteDeCabelloService.eliminar(id); // Elimina el corte de cabello por su ID
    }
}
