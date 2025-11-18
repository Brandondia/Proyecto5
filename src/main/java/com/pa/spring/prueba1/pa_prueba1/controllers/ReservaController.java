package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.*;
import com.pa.spring.prueba1.pa_prueba1.service.*;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private CorteDeCabelloService corteDeCabelloService;

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String mostrarPaginaReserva(Model model, @AuthenticationPrincipal User user) {
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        List<CorteDeCabello> cortes = corteDeCabelloService.obtenerTodos();
        List<Barbero> barberos = barberoService.obtenerTodos();

        // 🔥 VERIFICAR LÍMITES ANTES DE MOSTRAR EL FORMULARIO
        long reservasPendientes = reservaService.contarPorClienteYEstado(
                cliente.getIdCliente(),
                Reserva.EstadoReserva.PENDIENTE);

        boolean puedeReservar = reservasPendientes < 3; // Obtener de config si quieres

        model.addAttribute("cortes", cortes);
        model.addAttribute("barberos", barberos);
        model.addAttribute("clienteId", cliente.getIdCliente());

        // 🔥 NUEVOS ATRIBUTOS PARA CONTROLAR EL FORMULARIO
        model.addAttribute("puedeReservar", puedeReservar);
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("limiteReservas", 3);

        return "reserva";
    }

    // Obtener turnos disponibles por barbero (endpoint original - mantener para
    // compatibilidad)
    @GetMapping("/turnos/{barberoId}")
    @ResponseBody
    public List<Turno> obtenerTurnosDisponibles(@PathVariable Long barberoId) {
        return turnoService.obtenerTurnosDisponiblesPorBarbero(barberoId);
    }

    // NUEVO: Obtener turnos disponibles considerando la duración del servicio
    @GetMapping("/turnos/{barberoId}/{duracion}")
    @ResponseBody
    public List<Turno> obtenerTurnosPorDuracion(
            @PathVariable Long barberoId,
            @PathVariable Integer duracion) {

        System.out.println("=== ENDPOINT LLAMADO ===");
        System.out.println("Barbero ID: " + barberoId);
        System.out.println("Duración: " + duracion + " minutos");

        List<Turno> turnos = turnoService.obtenerTurnosDisponiblesPorDuracionYBarbero(barberoId, duracion);

        System.out.println("Turnos devueltos: " + turnos.size());
        System.out.println("=======================");

        return turnos;
    }

    // Confirmar reserva (usa PRG para evitar duplicados)
    @PostMapping("/confirmar")
    public String confirmarReserva(
            @RequestParam Long corteId,
            @RequestParam Long barberoId,
            @RequestParam Long turnoId,
            @RequestParam(required = false) String comentarios,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        // Verificar si el turno sigue disponible
        if (!turnoService.esTurnoDisponible(turnoId)) {
            redirectAttributes.addFlashAttribute("error",
                    "El turno seleccionado ya no está disponible. Intenta con otro horario.");
            return "redirect:/reserva";
        }

        try {
            // ✅ VALIDAR LÍMITES ANTES DE CREAR LA RESERVA
            reservaService.validarLimitesReserva(cliente.getIdCliente());

            // Crear reserva
            Reserva reserva = reservaService.crearReserva(
                    cliente.getIdCliente(),
                    barberoId,
                    corteId,
                    turnoId,
                    comentarios);

            if (reserva == null) {
                redirectAttributes.addFlashAttribute("error",
                        "No se pudo crear la reserva. Intenta nuevamente.");
                return "redirect:/reserva";
            }

            // ✅ ENVIAR EMAIL DE CONFIRMACIÓN
            try {
                emailService.notificarConfirmacionReserva(cliente, reserva);
                System.out.println("✅ Email de confirmación enviado a: " + cliente.getCorreo());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo enviar el email de confirmación: " + e.getMessage());
                // La reserva ya está creada, solo falló el email
            }

            // Pasar datos a la vista de confirmación usando redirect
            redirectAttributes.addFlashAttribute("mensaje", "¡Reserva confirmada con éxito! Revisa tu email.");
            redirectAttributes.addFlashAttribute("reserva", reserva);
            redirectAttributes.addFlashAttribute("cliente", cliente);
            redirectAttributes.addFlashAttribute("corte", corteDeCabelloService.obtenerPorId(corteId));
            redirectAttributes.addFlashAttribute("barbero", barberoService.obtenerPorId(barberoId));
            redirectAttributes.addFlashAttribute("turno", turnoService.obtenerPorId(turnoId));

            // Redirigir para romper el ciclo del POST (PRG)
            return "redirect:/reserva/confirmacion";

        } catch (IllegalStateException e) {
            // ✅ CAPTURAR ERRORES DE VALIDACIÓN DE LÍMITES Y OTROS
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error inesperado al crear la reserva. Por favor intenta nuevamente.");
            e.printStackTrace();
            return "redirect:/reserva";
        }
    }

    // Página de confirmación (se muestra después del redirect)
    @GetMapping("/confirmacion")
    public String mostrarConfirmacion() {
        return "confirmacion";
    }

    // ✅ NUEVA VERSION MEJORADA: Ver reservas del cliente con estadísticas completas
    @GetMapping("/mis-reservas")
    public String misReservas(Model model, @AuthenticationPrincipal User user) {
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        // Obtener reservas activas (pendientes)
        List<Reserva> reservasActivas = reservaService.obtenerPorClienteYEstado(
                cliente.getIdCliente(),
                Reserva.EstadoReserva.PENDIENTE);

        // Obtener historial (reservas completadas)
        List<Reserva> historial = reservaService.obtenerPorClienteYEstado(
                cliente.getIdCliente(),
                Reserva.EstadoReserva.COMPLETADA);

        // Calcular estadísticas
        Map<String, Object> estadisticas = calcularEstadisticas(cliente.getIdCliente(), historial);

        // Calcular datos para gráficos
        Map<String, Object> graficos = calcularDatosGraficos(historial);

        model.addAttribute("usuario", cliente);
        model.addAttribute("reservas", reservasActivas);
        model.addAttribute("historial", historial);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("graficos", graficos);

        return "mis-reservas";
    }

    // ✅ NUEVO: Método para calcular estadísticas del cliente
    private Map<String, Object> calcularEstadisticas(Long clienteId, List<Reserva> historial) {
        Map<String, Object> stats = new HashMap<>();

        // Total de visitas completadas
        stats.put("totalVisitas", historial.size());

        // Total gastado
        double totalGastado = historial.stream()
                .filter(r -> r.getCorte() != null)
                .mapToDouble(r -> r.getCorte().getPrecio())
                .sum();
        stats.put("totalGastado", String.format("%.2f", totalGastado));

        // Promedio de gasto
        double promedioGasto = historial.isEmpty() ? 0 : totalGastado / historial.size();
        stats.put("promedioGasto", String.format("%.2f", promedioGasto));

        // Reservas pendientes
        long reservasPendientes = reservaService.contarPorClienteYEstado(
                clienteId,
                Reserva.EstadoReserva.PENDIENTE);
        stats.put("reservasPendientes", reservasPendientes);

        // Barbero favorito (el que más ha atendido)
        Map<String, Long> conteoBarberos = historial.stream()
                .filter(r -> r.getBarbero() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getBarbero().getNombre(),
                        Collectors.counting()));

        String barberoFavorito = conteoBarberos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        stats.put("barberoFavorito", barberoFavorito);

        // Servicio favorito
        Map<String, Long> conteoServicios = historial.stream()
                .filter(r -> r.getCorte() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCorte().getNombre(),
                        Collectors.counting()));

        String servicioFavorito = conteoServicios.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        stats.put("servicioFavorito", servicioFavorito);

        return stats;
    }

    // ✅ NUEVO: Método para calcular datos de gráficos
    private Map<String, Object> calcularDatosGraficos(List<Reserva> historial) {
        Map<String, Object> graficos = new HashMap<>();

        // Datos para gráfico de gastos mensuales (últimos 6 meses)
        Map<String, Double> gastosPorMes = historial.stream()
                .filter(r -> r.getCorte() != null && r.getFechaHoraTurno() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getFechaHoraTurno().getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")),
                        Collectors.summingDouble(r -> r.getCorte().getPrecio())));

        // Convertir a arrays para Chart.js
        List<String> mesesLabels = new ArrayList<>(gastosPorMes.keySet());
        List<Double> mesesData = new ArrayList<>(gastosPorMes.values());

        graficos.put("gastosLabels", mesesLabels);
        graficos.put("gastosData", mesesData);

        // Datos para gráfico de servicios más usados
        Map<String, Long> serviciosCount = historial.stream()
                .filter(r -> r.getCorte() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCorte().getNombre(),
                        Collectors.counting()));

        List<String> serviciosLabels = new ArrayList<>(serviciosCount.keySet());
        List<Long> serviciosData = new ArrayList<>(serviciosCount.values());

        graficos.put("serviciosLabels", serviciosLabels);
        graficos.put("serviciosData", serviciosData);

        return graficos;
    }

    // Cancelar reserva
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user) {
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        try {
            Reserva reserva = reservaService.obtenerPorId(id);

            if (reserva == null) {
                redirectAttributes.addFlashAttribute("error", "Reserva no encontrada.");
                return "redirect:/reserva/mis-reservas";
            }

            // Verificar que la reserva pertenece al cliente
            if (!reserva.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta reserva.");
                return "redirect:/reserva/mis-reservas";
            }

            // Cancelar la reserva
            Reserva reservaCancelada = reservaService.cancelarReserva(id);

            if (reservaCancelada == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo cancelar la reserva.");
                return "redirect:/reserva/mis-reservas";
            }

            // ✅ ENVIAR EMAIL DE CANCELACIÓN
            try {
                emailService.notificarCancelacionReserva(cliente, reservaCancelada);
                System.out.println("✅ Email de cancelación enviado a: " + cliente.getCorreo());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo enviar el email de cancelación: " + e.getMessage());
            }

            redirectAttributes.addFlashAttribute("mensaje",
                    "Reserva cancelada con éxito. Se ha enviado un email de confirmación.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la reserva: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/reserva/mis-reservas";
    }
}