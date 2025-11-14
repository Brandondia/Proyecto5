package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/barbero/reservas")
public class BarberoReservasController {

    private final BarberoService barberoService;

    public BarberoReservasController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    /**
     * Obtiene el barbero autenticado actualmente
     */
    private Barbero obtenerBarberoActual(Authentication authentication) {
        String email = authentication.getName();
        return barberoService.obtenerBarberoPorEmail(email);
    }

    /**
     * Muestra la lista de reservas del barbero con filtros
     * GET /barbero/reservas?filtro=hoy|semana|mes|todas
     */
    @GetMapping
    public String misReservas(Model model, Authentication authentication,
                              @RequestParam(required = false, defaultValue = "hoy") String filtro) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                model.addAttribute("error", "No se pudo obtener la información del barbero");
                return "barbero/reservas";
            }
            
            System.out.println("========== DEBUG FILTROS ==========");
            System.out.println("Filtro seleccionado: " + filtro);
            System.out.println("ID Barbero: " + barbero.getIdBarbero());
            
            // Configurar rango de fechas según el filtro seleccionado
            LocalDateTime inicioFiltro;
            LocalDateTime finFiltro;
            
            switch (filtro.toLowerCase()) {
                case "semana":
                    // Desde el lunes de esta semana hasta el domingo
                    inicioFiltro = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            .atStartOfDay();
                    finFiltro = inicioFiltro.plusWeeks(1);
                    System.out.println("Filtro SEMANA - Inicio: " + inicioFiltro + " | Fin: " + finFiltro);
                    break;
                    
                case "mes":
                    // Desde el día 1 del mes actual hasta el último día
                    inicioFiltro = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    finFiltro = inicioFiltro.plusMonths(1);
                    System.out.println("Filtro MES - Inicio: " + inicioFiltro + " | Fin: " + finFiltro);
                    break;
                    
                case "todas":
                    // Rango muy amplio para obtener todas las reservas
                    inicioFiltro = LocalDateTime.now().minusYears(2);
                    finFiltro = LocalDateTime.now().plusYears(2);
                    System.out.println("Filtro TODAS - Inicio: " + inicioFiltro + " | Fin: " + finFiltro);
                    break;
                    
                default: // "hoy"
                    // Desde las 00:00 hasta las 23:59:59 de hoy
                    inicioFiltro = LocalDate.now().atStartOfDay();
                    finFiltro = inicioFiltro.plusDays(1);
                    System.out.println("Filtro HOY - Inicio: " + inicioFiltro + " | Fin: " + finFiltro);
            }

            // IMPORTANTE: Obtener TODAS las reservas del barbero (sin filtrar por fecha en el servicio)
            List<Reserva> todasReservas = barberoService.obtenerTodasReservasBarbero(barbero.getIdBarbero());
            System.out.println("Total de reservas obtenidas del servicio: " + todasReservas.size());
            
            // Debug: Imprimir fechas de todas las reservas
            System.out.println("Fechas de las reservas:");
            for (Reserva r : todasReservas) {
                System.out.println("  - Reserva ID " + r.getIdReserva() + ": " + r.getFechaHoraTurno());
            }
            
            // Filtrar las reservas según el período seleccionado EN EL CONTROLLER
            List<Reserva> reservasFiltradas = todasReservas.stream()
                    .filter(r -> r.getFechaHoraTurno() != null) // Validar que no sea null
                    .filter(r -> !r.getFechaHoraTurno().isBefore(inicioFiltro) 
                            && r.getFechaHoraTurno().isBefore(finFiltro))
                    .sorted(Comparator.comparing(Reserva::getFechaHoraTurno))
                    .collect(Collectors.toList());
            
            System.out.println("Reservas después del filtro: " + reservasFiltradas.size());
            System.out.println("===================================");

            // Calcular estadísticas sobre TODAS las reservas (no solo las filtradas)
            long reservasCompletadas = todasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();
            long reservasPendientes = todasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                    .count();
            long reservasCanceladas = todasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.CANCELADA)
                    .count();

            // Atributos del modelo
            model.addAttribute("nombreBarbero", barbero.getNombre() != null ? barbero.getNombre() : "Barbero");
            model.addAttribute("reservasHoyList", reservasFiltradas);
            model.addAttribute("reservasHoy", reservasFiltradas.size());
            model.addAttribute("reservasCompletadas", reservasCompletadas);
            model.addAttribute("reservasPendientes", reservasPendientes);
            model.addAttribute("reservasCanceladas", reservasCanceladas);
            
            // Formato de fecha en español
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", new Locale("es", "ES"));
            model.addAttribute("fechaHoy", LocalDate.now().format(formatter));
            model.addAttribute("filtroActual", filtro);

        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            model.addAttribute("error", "Error al cargar reservas: " + e.getMessage());
            model.addAttribute("reservasHoyList", new ArrayList<>());
            model.addAttribute("reservasHoy", 0);
            model.addAttribute("reservasCompletadas", 0);
            model.addAttribute("reservasPendientes", 0);
            model.addAttribute("reservasCanceladas", 0);
            model.addAttribute("fechaHoy", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy")));
            model.addAttribute("filtroActual", filtro);
            model.addAttribute("nombreBarbero", "Barbero");
        }
        return "barbero/reservas";
    }

    /**
     * Completa una reserva (cambia estado a COMPLETADA)
     * GET /barbero/reservas/completar/{id}
     */
    @GetMapping("/completar/{id}")
    public String completarReserva(@PathVariable Long id,
                                   @RequestParam(required = false, defaultValue = "hoy") String filtro,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo obtener la información del barbero");
                return "redirect:/barbero/reservas?filtro=" + filtro;
            }
            
            barberoService.completarReserva(id, barbero.getIdBarbero());
            redirectAttributes.addFlashAttribute("mensaje", "✓ Reserva completada correctamente");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "✗ Error al completar la reserva: " + e.getMessage());
        }
        return "redirect:/barbero/reservas?filtro=" + filtro;
    }

    /**
     * Cancela una reserva (cambia estado a CANCELADA)
     * GET /barbero/reservas/cancelar/{id}
     */
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "hoy") String filtro,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo obtener la información del barbero");
                return "redirect:/barbero/reservas?filtro=" + filtro;
            }
            
            // Motivo por defecto si no se proporciona
            String motivo = "Cancelada por el barbero";
            barberoService.cancelarReserva(id, barbero.getIdBarbero(), motivo);
            redirectAttributes.addFlashAttribute("mensaje", "✓ Reserva cancelada correctamente");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "✗ Error al cancelar la reserva: " + e.getMessage());
        }
        return "redirect:/barbero/reservas?filtro=" + filtro;
    }

    /**
     * Elimina una reserva permanentemente
     * GET /barbero/reservas/eliminar/{id}
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarReserva(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "hoy") String filtro,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo obtener la información del barbero");
                return "redirect:/barbero/reservas?filtro=" + filtro;
            }
            
            barberoService.eliminarReserva(id, barbero.getIdBarbero());
            redirectAttributes.addFlashAttribute("mensaje", "✓ Reserva eliminada correctamente");
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "✗ Error al eliminar la reserva: " + e.getMessage());
        }
        return "redirect:/barbero/reservas?filtro=" + filtro;
    }

    // ==================== ENDPOINTS AJAX (OPCIONALES) ====================
    // Estos métodos son para si quieres usar peticiones AJAX en el futuro
    
    /**
     * Completa una reserva vía AJAX
     * POST /barbero/reservas/completar/{id}
     */
    @PostMapping("/completar/{id}")
    @ResponseBody
    public Map<String, Object> completarReservaAjax(@PathVariable Long id,
                                                    Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                response.put("success", false);
                response.put("message", "No se pudo obtener la información del barbero");
                return response;
            }
            
            barberoService.completarReserva(id, barbero.getIdBarbero());
            
            response.put("success", true);
            response.put("message", "Reserva completada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * Cancela una reserva vía AJAX (con motivo)
     * POST /barbero/reservas/cancelar/{id}
     */
    @PostMapping("/cancelar/{id}")
    @ResponseBody
    public Map<String, Object> cancelarReservaAjax(@PathVariable Long id,
                                                   @RequestBody Map<String, String> payload,
                                                   Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                response.put("success", false);
                response.put("message", "No se pudo obtener la información del barbero");
                return response;
            }
            
            String motivo = payload.get("motivo");
            
            if (motivo == null || motivo.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Debe proporcionar un motivo de cancelación");
                return response;
            }
            
            barberoService.cancelarReserva(id, barbero.getIdBarbero(), motivo);
            
            response.put("success", true);
            response.put("message", "Reserva cancelada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    /**
     * Elimina una reserva vía AJAX
     * DELETE /barbero/reservas/eliminar/{id}
     */
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarReservaAjax(@PathVariable Long id,
                                                   Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(authentication);
            
            if (barbero == null) {
                response.put("success", false);
                response.put("message", "No se pudo obtener la información del barbero");
                return response;
            }
            
            barberoService.eliminarReserva(id, barbero.getIdBarbero());
            
            response.put("success", true);
            response.put("message", "Reserva eliminada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }
}