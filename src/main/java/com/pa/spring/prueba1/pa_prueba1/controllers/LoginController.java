package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {

    /**
     * Muestra el formulario de login.
     * Si ya existe una sesión, Spring Security se encargará de validarla,
     * aquí solo devolvemos la vista.
     */
    @GetMapping
    public String mostrarFormularioLogin(Model model, HttpSession session) {
        model.addAttribute("cliente", new Cliente());
        return "login"; 
    }
}







