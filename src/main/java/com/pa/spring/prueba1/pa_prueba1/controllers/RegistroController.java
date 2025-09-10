package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroController(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String mostrarFormularioRegistro(Model model) {
        // Si ya está autenticado, mejor mandarlo a reservas directamente
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/reserva";
        }

        model.addAttribute("cliente", new Cliente());
        return "registro";
    }

    @PostMapping
    public String registrarCliente(@ModelAttribute("cliente") Cliente cliente,
                                   RedirectAttributes redirectAttributes) {

        // Validar correo único
        boolean existeCorreo = clienteRepository.findByCorreo(cliente.getCorreo()).isPresent();
        if (existeCorreo) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con este correo. Por favor, inicia sesión.");
            return "redirect:/registro";
        }

        // Encriptar contraseña y setear valores por defecto
        cliente.setClave(passwordEncoder.encode(cliente.getClave()));
        if (cliente.getRol() == null || cliente.getRol().isBlank()) {
            cliente.setRol("ROLE_USER");
        }
        if (!cliente.isActivo()) {
            cliente.setActivo(true);
        }

        // Guardar
        clienteRepository.save(cliente);

        // MUY IMPORTANTE: no meter nada manual en sesión; dejar que Spring Security maneje el login
        redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ahora inicia sesión con tu correo y contraseña.");
        return "redirect:/login";
    }
}






