package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/barbero/estadisticas")
public class EstadisticasController {

    private final BarberoService barberoService;

    public EstadisticasController(BarberoService barberoService) {
        this.barberoService = barberoService;
    }

    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    @GetMapping
    public String misEstadisticas(Model model, Authentication auth,
                                  @RequestParam(required = false, defaultValue = "mes") String periodo) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            List<Reserva> reservas = barberoService.obtenerReservasSemanaActual(barbero.getIdBarbero());

            long totalCortes = reservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();

            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            model.addAttribute("totalCortes", totalCortes);
            model.addAttribute("periodo", periodo);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas: " + e.getMessage());
        }
        return "barbero/estadisticas";
    }
}
