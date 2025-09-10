package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ClienteService clienteService;

    public HomeController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            // Obtiene el correo (username) del usuario logueado
            String correo = auth.getName();
            Cliente cliente = clienteService.obtenerPorCorreo(correo);

            // Pasamos el cliente al modelo (ej: para mostrar nombre en la vista)
            model.addAttribute("clienteLogueado", cliente);
        }

        // Siempre renderiza el index, ya sea público o logueado
        return "index";
    }
}



