package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.*;
import com.pa.spring.prueba1.pa_prueba1.service.*;
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
    public String mostrarPaginaReserva(Model model, 
                                       @AuthenticationPrincipal User user) {
        // Obtener cliente autenticado
        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        // Listar cortes y barberos
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

    // Confirmar reserva
    @PostMapping("/confirmar")
    public String confirmarReserva(
            @RequestParam Long corteId,
            @RequestParam Long barberoId,
            @RequestParam Long turnoId,
            @RequestParam(required = false) String comentarios,
            @AuthenticationPrincipal User user,
            Model model,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());

        // Verificar turno disponible
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

        // Pasar datos de confirmación
        model.addAttribute("mensaje", "¡Reserva confirmada con éxito!");
        model.addAttribute("reserva", reserva);
        model.addAttribute("cliente", cliente);
        model.addAttribute("corte", corteDeCabelloService.obtenerPorId(corteId));
        model.addAttribute("barbero", barberoService.obtenerPorId(barberoId));
        model.addAttribute("turno", turnoService.obtenerPorId(turnoId));

        return "confirmacion";
    }

    // Ver reservas del cliente
    @GetMapping("/mis-reservas")
    public String misReservas(Model model,
                              @AuthenticationPrincipal User user) {
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


