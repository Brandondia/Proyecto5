package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Usuario;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.repository.UsuarioRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.CorteDeCabelloRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final BarberoRepository barberoRepository;
    private final CorteDeCabelloRepository corteRepository;
    private final ReservaRepository reservaRepository;
    
    @Autowired
    private BarberoService barberoService;

    public AdminController(UsuarioRepository usuarioRepository,
                           BarberoRepository barberoRepository,
                           CorteDeCabelloRepository corteRepository,
                           ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.barberoRepository = barberoRepository;
        this.corteRepository = corteRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/admin/panel")
    public String adminHome(Model model) {
        
        // Obtener el authentication del SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailOrUsername = authentication.getName();
        
        // Buscar el usuario por email (ya que te autentican con email)
        Usuario admin = usuarioRepository.findByEmail(emailOrUsername)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + emailOrUsername));
        
        model.addAttribute("admin", admin);
        model.addAttribute("nombreAdmin", admin.getUsername());

        // Estadísticas
        long totalClientes = usuarioRepository.count();
        long totalBarberos = barberoRepository.count();
        long totalServicios = corteRepository.count();
        long reservasPendientes = reservaRepository.countByEstado(Reserva.EstadoReserva.PENDIENTE);
        
        // ✅ NUEVO: Contador de solicitudes de ausencias pendientes
        long totalPendientes = barberoService.contarSolicitudesPendientes();

        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalBarberos", totalBarberos);
        model.addAttribute("totalServicios", totalServicios);
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("totalPendientes", totalPendientes); // ✅ NUEVO

        return "/admin/panel";
    }
}


