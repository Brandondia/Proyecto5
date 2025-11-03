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

import java.util.List;

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

    // Mostrar página de reserva
    @GetMapping
    public String mostrarPaginaReserva(Model model, @AuthenticationPrincipal User user) {
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        List<CorteDeCabello> cortes = corteDeCabelloService.obtenerTodos();
        List<Barbero> barberos = barberoService.obtenerTodos();

        model.addAttribute("cortes", cortes);
        model.addAttribute("barberos", barberos);
        model.addAttribute("clienteId", cliente.getIdCliente());

        return "reserva";
    }

    // Obtener turnos disponibles por barbero
    @GetMapping("/turnos/{barberoId}")
    @ResponseBody
    public List<Turno> obtenerTurnosDisponibles(@PathVariable Long barberoId) {
        return turnoService.obtenerTurnosDisponiblesPorBarbero(barberoId);
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

        // Crear reserva
        Reserva reserva = reservaService.crearReserva(
                cliente.getIdCliente(),
                barberoId,
                corteId,
                turnoId,
                comentarios
        );

        if (reserva == null) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo crear la reserva. Intenta nuevamente.");
            return "redirect:/reserva";
        }

        // Pasar datos a la vista de confirmación usando redirect
        redirectAttributes.addFlashAttribute("mensaje", "¡Reserva confirmada con éxito!");
        redirectAttributes.addFlashAttribute("reserva", reserva);
        redirectAttributes.addFlashAttribute("cliente", cliente);
        redirectAttributes.addFlashAttribute("corte", corteDeCabelloService.obtenerPorId(corteId));
        redirectAttributes.addFlashAttribute("barbero", barberoService.obtenerPorId(barberoId));
        redirectAttributes.addFlashAttribute("turno", turnoService.obtenerPorId(turnoId));

        // Redirigir para romper el ciclo del POST (PRG)
        return "redirect:/reserva/confirmacion";
    }

    // Página de confirmación (se muestra después del redirect)
    @GetMapping("/confirmacion")
    public String mostrarConfirmacion() {
        return "confirmacion";
    }

    // Ver reservas del cliente
    @GetMapping("/mis-reservas")
    public String misReservas(Model model, @AuthenticationPrincipal User user) {
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());
        List<Reserva> reservas = reservaService.obtenerPorCliente(cliente.getIdCliente());

        model.addAttribute("reservas", reservas);
        return "mis-reservas";
    }

    // Cancelar reserva
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes,
                                  @AuthenticationPrincipal User user) {
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        Reserva reserva = reservaService.cancelarReserva(id);

        if (reserva == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo cancelar la reserva.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada con éxito.");
        }

        return "redirect:/reserva/mis-reservas";
    }
}



