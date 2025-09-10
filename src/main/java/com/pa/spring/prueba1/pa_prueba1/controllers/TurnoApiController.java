package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen
public class TurnoApiController {

    // Servicios inyectados para manejar la lógica de negocio
    @Autowired
    private TurnoService turnoService;
    
    @Autowired
    private BarberoService barberoService;

    // Método para obtener los turnos disponibles para un barbero específico
    @GetMapping("/disponibles/{barberoId}")
    public ResponseEntity<?> obtenerTurnosDisponibles(@PathVariable Long barberoId) {
        try {
            // Obtener el barbero por su ID
            Barbero barbero = barberoService.obtenerPorId(barberoId);
            if (barbero == null) {
                // Si no se encuentra el barbero, se responde con un error
                System.out.println("Error: Barbero con ID " + barberoId + " no encontrado");
                return ResponseEntity.badRequest().body(Map.of("error", "Barbero no encontrado"));
            }
            
            System.out.println("Obteniendo turnos disponibles para barbero: " + barbero.getNombre() + " (ID: " + barberoId + ")");
            
            // Obtener los turnos disponibles para el barbero desde la base de datos
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorBarbero(barberoId);
            
            System.out.println("Turnos disponibles encontrados en BD: " + turnosDisponibles.size());
            
            // Si no hay turnos disponibles en la base de datos, generar turnos para los próximos 14 días
            if (turnosDisponibles.isEmpty()) {
                LocalDate hoy = LocalDate.now();
                LocalDate finPeriodo = hoy.plusDays(14);
                
                System.out.println("Generando turnos para el periodo: " + hoy + " a " + finPeriodo);
                
                try {
                    turnosDisponibles = turnoService.generarTurnosDisponibles(barbero, hoy, finPeriodo);
                    System.out.println("Turnos generados: " + turnosDisponibles.size());
                } catch (Exception e) {
                    // Si ocurre un error al generar los turnos, se captura y se muestra el error
                    System.out.println("Error al generar turnos: " + e.getMessage());
                    e.printStackTrace();
                    // Continuar con la lista vacía en lugar de fallar
                }
            }
            
            // Filtrar solo los turnos futuros (a partir de ahora)
            LocalDateTime ahora = LocalDateTime.now();
            turnosDisponibles = turnosDisponibles.stream()
                .filter(turno -> turno.getFechaHora() != null && turno.getFechaHora().isAfter(ahora))
                .collect(Collectors.toList());
            
            System.out.println("Turnos futuros disponibles: " + turnosDisponibles.size());
            
            // Convertir los turnos a un formato más simple para retornar como JSON
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosDisponibles) {
                if (turno.getFechaHora() != null) {
                    Map<String, Object> turnoMap = new HashMap<>();
                    turnoMap.put("idTurno", turno.getIdTurno());
                    // Asegurar que la fecha se envía en formato ISO 8601 para evitar problemas de zona horaria
                    turnoMap.put("fechaHora", turno.getFechaHora().toString());
                    turnoMap.put("estado", turno.getEstado().toString());
                    turnoMap.put("barberoId", turno.getBarbero().getIdBarbero());
                    turnosSimplificados.add(turnoMap);
                }
            }
            
            return ResponseEntity.ok(turnosSimplificados); // Responder con los turnos disponibles en formato JSON
        } catch (Exception e) {
            // Si ocurre un error en el proceso, se captura y se devuelve un error
            System.out.println("Error al obtener turnos disponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    // Método para obtener los turnos disponibles para un barbero en una fecha específica
    @GetMapping("/disponibles/{barberoId}/fecha/{fecha}")
    public ResponseEntity<?> obtenerTurnosDisponiblesPorFecha(
            @PathVariable Long barberoId,
            @PathVariable String fecha) {
        
        try {
            // Convertir la fecha recibida en formato String a LocalDate
            LocalDate fechaSeleccionada = LocalDate.parse(fecha);
            
            // Obtener los turnos disponibles para el barbero en la fecha seleccionada
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorBarberoYFecha(barberoId, fechaSeleccionada);
            
            // Convertir los turnos a un formato más simple para retornar como JSON
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosDisponibles) {
                Map<String, Object> turnoMap = new HashMap<>();
                turnoMap.put("idTurno", turno.getIdTurno());
                turnoMap.put("fechaHora", turno.getFechaHora().toString());
                turnoMap.put("estado", turno.getEstado().toString());
                turnoMap.put("barberoId", turno.getBarbero().getIdBarbero());
                turnosSimplificados.add(turnoMap);
            }
            
            return ResponseEntity.ok(turnosSimplificados); // Responder con los turnos disponibles en formato JSON
        } catch (Exception e) {
            // Si ocurre un error en el proceso, se captura y se devuelve un error
            System.out.println("Error al obtener turnos por fecha: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    // Método para reservar un turno
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarTurno(@RequestBody Map<String, Object> datos) {
        try {
            // Obtener los datos de la reserva del cuerpo de la solicitud (turnoId, clienteId, etc.)
            Long turnoId = Long.valueOf(datos.get("turnoId").toString());
            Long clienteId = Long.valueOf(datos.get("clienteId").toString());
            Long barberoId = Long.valueOf(datos.get("barberoId").toString());
            Long corteId = Long.valueOf(datos.get("corteId").toString());
            String comentarios = datos.get("comentarios") != null ? datos.get("comentarios").toString() : "";
            
            System.out.println("Reservando turno: " + turnoId + " para cliente: " + clienteId);
            
            // Verificar si el turno está disponible
            if (!turnoService.esTurnoDisponible(turnoId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El turno seleccionado ya no está disponible"));
            }
            
            // Marcar el turno como no disponible (reservado)
            Turno turno = turnoService.marcarTurnoNoDisponible(turnoId);
            
            if (turno == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se pudo reservar el turno"));
            }
            
            // Preparar la respuesta con los detalles del turno reservado
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Turno reservado con éxito");
            
            Map<String, Object> turnoMap = new HashMap<>();
            turnoMap.put("idTurno", turno.getIdTurno());
            turnoMap.put("fechaHora", turno.getFechaHora().toString());
            turnoMap.put("estado", turno.getEstado().toString());
            
            respuesta.put("turno", turnoMap);
            
            return ResponseEntity.ok(respuesta); // Responder con éxito y los detalles del turno reservado
        } catch (Exception e) {
            // Si ocurre un error al procesar la reserva, se captura y se devuelve un error
            System.out.println("Error al reservar turno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar la reserva: " + e.getMessage()));
        }
    }
}
