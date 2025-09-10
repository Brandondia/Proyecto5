package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.service.BarberoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para manejar las operaciones CRUD relacionadas con los barberos.
 * Expone los servicios REST para listar, obtener, crear, actualizar y eliminar barberos.
 */
@RestController
@RequestMapping("/api/barberos")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (puede ajustarse según sea necesario)
public class BarberoController {

    @Autowired
    private BarberoService barberoService;

    /**
     * Obtiene la lista de todos los barberos.
     * 
     * @return lista de barberos
     */
    @GetMapping
    public List<Barbero> listarBarberos() {
        return barberoService.obtenerTodos(); // Llama al servicio para obtener todos los barberos
    }

    /**
     * Obtiene un barbero específico por su ID.
     * 
     * @param id ID del barbero
     * @return barbero encontrado
     */
    @GetMapping("/{id}")
    public Barbero obtenerBarbero(@PathVariable Long id) {
        return barberoService.obtenerPorId(id); // Llama al servicio para obtener el barbero por su ID
    }

    /**
     * Crea un nuevo barbero.
     * 
     * @param barbero objeto Barbero con la información a guardar
     * @return barbero creado
     */
    @PostMapping
    public Barbero crearBarbero(@RequestBody Barbero barbero) {
        return barberoService.guardar(barbero); // Llama al servicio para guardar el nuevo barbero
    }

    /**
     * Actualiza un barbero existente.
     * 
     * @param id ID del barbero a actualizar
     * @param barbero objeto Barbero con la nueva información
     * @return barbero actualizado
     */
    @PutMapping("/{id}")
    public Barbero actualizarBarbero(@PathVariable Long id, @RequestBody Barbero barbero) {
        return barberoService.actualizar(id, barbero); // Llama al servicio para actualizar el barbero
    }

    /**
     * Elimina un barbero por su ID.
     * 
     * @param id ID del barbero a eliminar
     */
    @DeleteMapping("/{id}")
    public void eliminarBarbero(@PathVariable Long id) {
        barberoService.eliminar(id); // Llama al servicio para eliminar el barbero
    }
}
