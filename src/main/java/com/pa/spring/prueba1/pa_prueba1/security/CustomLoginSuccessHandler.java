package com.pa.spring.prueba1.pa_prueba1.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Obtener roles del usuario
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = request.getContextPath(); // Por defecto al inicio

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "admin/panel"; // Dashboard administrador
                break;
            } else if (role.equals("ROLE_BARBERO")) {
                redirectUrl = "barbero/panel"; // Dashboard barbero
                break;
            } else if (role.equals("ROLE_USER")) {
                redirectUrl = "/home"; // Dashboard cliente
                break;
            }
        }

        // Redirigir según rol
        response.sendRedirect(redirectUrl);
    }
}









