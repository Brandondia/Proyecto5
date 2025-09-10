package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.service.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import com.pa.spring.prueba1.pa_prueba1.service.CorteDeCabelloService;
import com.pa.spring.prueba1.pa_prueba1.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador que maneja las peticiones del panel administrativo del sistema.
 * Gestiona el acceso al dashboard de administración y muestra estadísticas clave del sistema.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BarberoService barberoService;
    
    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private CorteDeCabelloService corteService;
    
    @Autowired
    private ReservaService reservaService;

    /**
     * Muestra el panel de administración con estadísticas del sistema.
     *
     * @param model objeto para enviar datos a la vista
     * @param session objeto de sesión para acceder a la información del administrador logueado
     * @return vista del panel de administración
     */
    @GetMapping
    public String panelAdmin(Model model, HttpSession session) {
        // Obtener información del administrador logueado desde la sesión
        Administrador admin = (Administrador) session.getAttribute("adminLogueado");
        model.addAttribute("admin", admin);
        
        // Obtener estadísticas para mostrar en el dashboard
        model.addAttribute("totalBarberos", barberoService.obtenerTodos().size());
        model.addAttribute("totalClientes", clienteService.obtenerTodos().size());
        model.addAttribute("totalServicios", corteService.obtenerTodos().size());
        model.addAttribute("totalReservas", reservaService.obtenerTodas().size());
        
        // Obtener el número de reservas pendientes
        model.addAttribute("reservasPendientes", reservaService.obtenerPorEstado(
            com.pa.spring.prueba1.pa_prueba1.model.Reserva.EstadoReserva.PENDIENTE).size());
        
        return "admin/panel"; // Retorna la vista del panel de administración
    }
}
