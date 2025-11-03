package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.service.AdministradorService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminLoginController {

    private final AdministradorService administradorService;
    private final PasswordEncoder passwordEncoder;

    public AdminLoginController(AdministradorService administradorService,
                                PasswordEncoder passwordEncoder) {
        this.administradorService = administradorService;
        this.passwordEncoder = passwordEncoder;
    }

}