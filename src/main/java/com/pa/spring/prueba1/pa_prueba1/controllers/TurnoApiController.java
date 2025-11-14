package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.repository.CorteDeCabelloRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoApiController {

    @Autowired
    private TurnoService turnoService;
    
    @Autowired
    private BarberoService barberoService;
    
    @Autowired
    private CorteDeCabelloRepository corteRepository;

    /**
     * Obtiene los turnos disponibles para un barbero específico
     * GET /api/turnos/disponibles/{barberoId}
     */
    @GetMapping("/disponibles/{barberoId}")
    public ResponseEntity<?> obtenerTurnosDisponibles(@PathVariable Long barberoId) {
        try {
            Barbero barbero = barberoService.obtenerPorId(barberoId);
            if (barbero == null) {
                System.out.println("Error: Barbero con ID " + barberoId + " no encontrado");
                return ResponseEntity.badRequest().body(Map.of("error", "Barbero no encontrado"));
            }
            
            System.out.println("Obteniendo turnos disponibles para barbero: " + barbero.getNombre() + " (ID: " + barberoId + ")");
            
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorBarbero(barberoId);
            
            System.out.println("Turnos disponibles encontrados en BD: " + turnosDisponibles.size());
            
            // Si no hay turnos disponibles, generar para los próximos 14 días
            if (turnosDisponibles.isEmpty()) {
                LocalDate hoy = LocalDate.now();
                LocalDate finPeriodo = hoy.plusDays(14);
                
                System.out.println("Generando turnos para el periodo: " + hoy + " a " + finPeriodo);
                
                try {
                    // ✅ CORRECCIÓN: Convertir LocalDate a LocalDateTime y usar el método correcto
                    LocalDateTime fechaInicioTime = hoy.atStartOfDay();
                    LocalDateTime fechaFinTime = finPeriodo.atTime(23, 59, 59);
                    
                    turnosDisponibles = turnoService.generarTurnosAutomaticos(
                        barberoId,
                        fechaInicioTime,
                        fechaFinTime
                    );
                    System.out.println("Turnos generados: " + turnosDisponibles.size());
                } catch (Exception e) {
                    System.out.println("Error al generar turnos: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Filtrar solo turnos futuros
            LocalDateTime ahora = LocalDateTime.now();
            turnosDisponibles = turnosDisponibles.stream()
                .filter(turno -> turno.getFechaHora() != null && turno.getFechaHora().isAfter(ahora))
                .collect(Collectors.toList());
            
            System.out.println("Turnos futuros disponibles: " + turnosDisponibles.size());
            
            // Convertir a formato simple para JSON
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosDisponibles) {
                if (turno.getFechaHora() != null) {
                    Map<String, Object> turnoMap = new HashMap<>();
                    turnoMap.put("idTurno", turno.getIdTurno());
                    turnoMap.put("fechaHora", turno.getFechaHora().toString());
                    turnoMap.put("estado", turno.getEstado().toString());
                    turnoMap.put("barberoId", turno.getBarbero().getIdBarbero());
                    turnosSimplificados.add(turnoMap);
                }
            }
            
            return ResponseEntity.ok(turnosSimplificados);
        } catch (Exception e) {
            System.out.println("Error al obtener turnos disponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene turnos disponibles para un barbero en una fecha específica
     * GET /api/turnos/disponibles/{barberoId}/fecha/{fecha}
     */
    @GetMapping("/disponibles/{barberoId}/fecha/{fecha}")
    public ResponseEntity<?> obtenerTurnosDisponiblesPorFecha(
            @PathVariable Long barberoId,
            @PathVariable String fecha) {
        
        try {
            LocalDate fechaSeleccionada = LocalDate.parse(fecha);
            
            // Convertir a LocalDateTime para filtrar
            LocalDateTime inicioDelDia = fechaSeleccionada.atStartOfDay();
            LocalDateTime finDelDia = fechaSeleccionada.atTime(23, 59, 59);
            
            // Obtener todos los turnos del barbero en ese rango
            List<Turno> todosTurnos = turnoService.obtenerPorRangoFechas(inicioDelDia, finDelDia);
            
            // Filtrar por barbero y estado disponible
            List<Turno> turnosDisponibles = todosTurnos.stream()
                .filter(t -> t.getBarbero().getIdBarbero().equals(barberoId))
                .filter(t -> t.getEstado() == Turno.EstadoTurno.DISPONIBLE)
                .collect(Collectors.toList());
            
            // Convertir a formato simple
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosDisponibles) {
                Map<String, Object> turnoMap = new HashMap<>();
                turnoMap.put("idTurno", turno.getIdTurno());
                turnoMap.put("fechaHora", turno.getFechaHora().toString());
                turnoMap.put("estado", turno.getEstado().toString());
                turnoMap.put("barberoId", turno.getBarbero().getIdBarbero());
                turnosSimplificados.add(turnoMap);
            }
            
            return ResponseEntity.ok(turnosSimplificados);
        } catch (Exception e) {
            System.out.println("Error al obtener turnos por fecha: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    /**
     * NUEVO: Obtiene turnos disponibles filtrados por duración del servicio
     * GET /api/turnos/disponibles/{barberoId}/servicio/{servicioId}
     */
    @GetMapping("/disponibles/{barberoId}/servicio/{servicioId}")
    public ResponseEntity<?> obtenerTurnosDisponiblesPorServicio(
            @PathVariable Long barberoId,
            @PathVariable Long servicioId) {
        try {
            System.out.println("=== OBTENIENDO TURNOS POR SERVICIO ===");
            System.out.println("Barbero ID: " + barberoId);
            System.out.println("Servicio ID: " + servicioId);
            
            // Obtener el servicio para conocer su duración
            CorteDeCabello servicio = corteRepository.findById(servicioId).orElse(null);
            if (servicio == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Servicio no encontrado"));
            }
            
            int duracionMinutos = servicio.getDuracion();
            System.out.println("Duración del servicio: " + duracionMinutos + " minutos");
            
            // Obtener turnos válidos para esta duración
            List<Turno> turnosValidos = turnoService.obtenerTurnosDisponiblesPorDuracionYBarbero(
                barberoId, 
                duracionMinutos
            );
            
            System.out.println("Turnos válidos encontrados: " + turnosValidos.size());
            
            // Filtrar solo turnos futuros
            LocalDateTime ahora = LocalDateTime.now();
            turnosValidos = turnosValidos.stream()
                .filter(turno -> turno.getFechaHora() != null && turno.getFechaHora().isAfter(ahora))
                .collect(Collectors.toList());
            
            System.out.println("Turnos futuros válidos: " + turnosValidos.size());
            
            // Convertir a formato simple para JSON
            List<Map<String, Object>> turnosSimplificados = new ArrayList<>();
            
            for (Turno turno : turnosValidos) {
                Map<String, Object> turnoMap = new HashMap<>();
                turnoMap.put("idTurno", turno.getIdTurno());
                turnoMap.put("fechaHora", turno.getFechaHora().toString());
                turnoMap.put("estado", turno.getEstado().toString());
                turnoMap.put("barberoId", turno.getBarbero().getIdBarbero());
                
                // Información adicional útil
                LocalDateTime horaFin = turno.getFechaHora().plusMinutes(duracionMinutos);
                turnoMap.put("horaFin", horaFin.toLocalTime().toString());
                
                turnosSimplificados.add(turnoMap);
            }
            
            return ResponseEntity.ok(Map.of(
                "turnos", turnosSimplificados,
                "duracionServicio", duracionMinutos,
                "nombreServicio", servicio.getNombre()
            ));
            
        } catch (Exception e) {
            System.out.println("Error al obtener turnos por servicio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener turnos: " + e.getMessage()));
        }
    }
    
    /**
     * Reserva un turno
     * POST /api/turnos/reservar
     */
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarTurno(@RequestBody Map<String, Object> datos) {
        try {
            Long turnoId = Long.valueOf(datos.get("turnoId").toString());
            
            System.out.println("Reservando turno: " + turnoId);
            
            // Verificar disponibilidad
            if (!turnoService.esTurnoDisponible(turnoId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El turno seleccionado ya no está disponible"));
            }
            
            // Cambiar estado a NO_DISPONIBLE
            Turno turno = turnoService.cambiarEstadoTurno(turnoId, Turno.EstadoTurno.NO_DISPONIBLE);
            
            if (turno == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se pudo reservar el turno"));
            }
            
            // Preparar respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Turno reservado con éxito");
            
            Map<String, Object> turnoMap = new HashMap<>();
            turnoMap.put("idTurno", turno.getIdTurno());
            turnoMap.put("fechaHora", turno.getFechaHora().toString());
            turnoMap.put("estado", turno.getEstado().toString());
            
            respuesta.put("turno", turnoMap);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            System.out.println("Error al reservar turno: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al procesar la reserva: " + e.getMessage()));
        }
    }
    
    /**
     * Genera turnos para un barbero en un rango de fechas
     * POST /api/turnos/generar
     * Body: { "barberoId": 1, "fechaInicio": "2025-11-15", "fechaFin": "2025-11-30" }
     */
    @PostMapping("/generar")
    public ResponseEntity<?> generarTurnos(@RequestBody Map<String, Object> datos) {
        try {
            Long barberoId = Long.valueOf(datos.get("barberoId").toString());
            LocalDate fechaInicio = LocalDate.parse(datos.get("fechaInicio").toString());
            LocalDate fechaFin = LocalDate.parse(datos.get("fechaFin").toString());
            
            System.out.println("Generando turnos para barbero: " + barberoId + 
                " desde " + fechaInicio + " hasta " + fechaFin);
            
            LocalDateTime inicioTime = fechaInicio.atStartOfDay();
            LocalDateTime finTime = fechaFin.atTime(23, 59, 59);
            
            List<Turno> turnosGenerados = turnoService.generarTurnosAutomaticos(
                barberoId, 
                inicioTime, 
                finTime
            );
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Turnos generados exitosamente",
                "cantidad", turnosGenerados.size()
            ));
        } catch (Exception e) {
            System.out.println("Error al generar turnos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al generar turnos: " + e.getMessage()));
        }
    }
}