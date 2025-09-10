package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador que maneja las peticiones relacionadas con las reservas de los clientes.
 * Permite listar, filtrar, completar, cancelar, eliminar y ver los detalles de las reservas.
 */
@Controller
@RequestMapping("/admin/reservas")
public class AdminReservaController {

    @Autowired
    private ReservaService reservaService;

    /**
     * Lista todas las reservas del sistema.
     * 
     * @param model objeto para enviar los datos a la vista
     * @return vista de lista de reservas
     */
    @GetMapping
    public String listarReservas(Model model) {
        try {
            // Obtener todas las reservas
            List<Reserva> reservas = reservaService.obtenerTodas();
            System.out.println("Número de reservas encontradas: " + reservas.size());
            for (Reserva r : reservas) {
                System.out.println("Reserva ID: " + r.getIdReserva() + 
                                  ", Cliente: " + (r.getCliente() != null ? r.getCliente().getNombre() : "null") + 
                                  ", Estado: " + r.getEstado());
            }
            model.addAttribute("reservas", reservas); // Agregar reservas a la vista
        } catch (Exception e) {
            // En caso de error, se muestra un mensaje
            System.out.println("Error al listar reservas: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar las reservas: " + e.getMessage());
        }
        return "admin/reservas/lista"; // Retorna la vista de reservas
    }

    /**
     * Filtra las reservas según un rango de fechas o un estado.
     * 
     * @param fechaInicio fecha de inicio para el filtro
     * @param fechaFin fecha de fin para el filtro
     * @param estado estado de la reserva (pendiente, completada, etc.)
     * @param model objeto para enviar los datos a la vista
     * @return vista de lista de reservas filtradas
     */
    @GetMapping("/filtrar")
    public String filtrarReservas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String estado,
            Model model) {
        
        List<Reserva> reservas;
        
        // Filtrar por rango de fechas
        if (fechaInicio != null && fechaFin != null) {
            reservas = reservaService.obtenerPorRangoFechas(fechaInicio, fechaFin);
        } 
        // Filtrar por estado
        else if (estado != null && !estado.isEmpty()) {
            Reserva.EstadoReserva estadoEnum = Reserva.EstadoReserva.valueOf(estado);
            reservas = reservaService.obtenerPorEstado(estadoEnum);
        } 
        // Si no hay filtros, obtener todas las reservas
        else {
            reservas = reservaService.obtenerTodas();
        }
        
        // Agregar filtros y resultados a la vista
        model.addAttribute("reservas", reservas);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("estado", estado);
        
        return "admin/reservas/lista"; // Retorna la vista de reservas filtradas
    }

    /**
     * Marca una reserva como completada.
     * 
     * @param id ID de la reserva a completar
     * @param redirectAttributes objeto para enviar mensajes de redirección
     * @return redirige a la lista de reservas con mensaje de éxito o error
     */
    @GetMapping("/completar/{id}")
    public String completarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Reserva reserva = reservaService.completarReserva(id);
        
        if (reserva != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Reserva marcada como completada con éxito.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo completar la reserva.");
        }
        
        return "redirect:/admin/reservas"; // Redirige a la lista de reservas
    }

    /**
     * Cancela una reserva.
     * 
     * @param id ID de la reserva a cancelar
     * @param redirectAttributes objeto para enviar mensajes de redirección
     * @return redirige a la lista de reservas con mensaje de éxito o error
     */
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Reserva reserva = reservaService.cancelarReserva(id);
        
        if (reserva != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada con éxito.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo cancelar la reserva.");
        }
        
        return "redirect:/admin/reservas"; // Redirige a la lista de reservas
    }

    /**
     * Elimina una reserva.
     * 
     * @param id ID de la reserva a eliminar
     * @param redirectAttributes objeto para enviar mensajes de redirección
     * @return redirige a la lista de reservas con mensaje de éxito o error
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservaService.eliminarReserva(id); // Elimina la reserva
            redirectAttributes.addFlashAttribute("mensaje", "Reserva eliminada con éxito.");
        } catch (Exception e) {
            // En caso de error, se muestra un mensaje
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la reserva: " + e.getMessage());
        }
        
        return "redirect:/admin/reservas"; // Redirige a la lista de reservas
    }

    /**
     * Muestra los detalles de una reserva.
     * 
     * @param id ID de la reserva
     * @param model objeto para enviar los datos a la vista
     * @return vista de los detalles de la reserva
     */
    @GetMapping("/detalle/{id}")
    public String detalleReserva(@PathVariable Long id, Model model) {
        Reserva reserva = reservaService.obtenerPorId(id); // Obtiene los detalles de la reserva
        
        if (reserva == null) {
            return "redirect:/admin/reservas"; // Si la reserva no existe, redirige a la lista de reservas
        }
        
        model.addAttribute("reserva", reserva); // Agrega la reserva a la vista
        return "admin/reservas/detalle"; // Retorna la vista con los detalles
    }
}
