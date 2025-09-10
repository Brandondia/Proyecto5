package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos") // Define la ruta base para los servicios relacionados con turnos
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (para permitir solicitudes desde frontend en diferentes dominios)
public class TurnoController {

    // Servicio inyectado para la lógica relacionada con los turnos
    @Autowired
    private TurnoService turnoService;

    // Método para obtener la lista de turnos de un barbero específico
    @GetMapping("/barbero/{idBarbero}") // Endpoint que recibe el ID del barbero como parámetro
    public List<Turno> listarTurnosPorBarbero(@PathVariable Long idBarbero) {
        // Llama al servicio para obtener los turnos asociados con el barbero dado
        return turnoService.obtenerTurnosPorBarbero(idBarbero);
    }

    // Método para crear un nuevo turno
    @PostMapping // Endpoint para crear un nuevo turno
    public Turno crearTurno(@RequestBody Turno turno) {
        // Llama al servicio para guardar el turno y lo retorna
        return turnoService.guardarTurno(turno);
    }

    // Método para eliminar un turno específico
    @DeleteMapping("/{id}") // Endpoint que recibe el ID del turno a eliminar
    public void eliminarTurno(@PathVariable Long id) {
        // Llama al servicio para eliminar el turno con el ID proporcionado
        turnoService.eliminarTurno(id);
    }
}
