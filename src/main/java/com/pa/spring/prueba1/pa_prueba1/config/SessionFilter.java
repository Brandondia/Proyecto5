package com.pa.spring.prueba1.pa_prueba1.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Indica que esta clase es un filtro y se aplicará a todas las solicitudes ("/*")
@WebFilter("/*")
public class SessionFilter implements Filter {

    /**
     * Método principal del filtro que se ejecuta en cada solicitud entrante.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Convertir la solicitud genérica en una solicitud HTTP específica
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Obtener la sesión actual si existe (false = no crear una nueva si no hay)
        HttpSession session = httpRequest.getSession(false);

        // Obtener la URI de la solicitud (por ejemplo: "/admin/home")
        String requestURI = httpRequest.getRequestURI();

        // Filtrar solo las rutas que comienzan con /admin y no sean /admin/login
        if (requestURI.startsWith("/admin") && !requestURI.startsWith("/admin/login")) {
            System.out.println("SessionFilter: URI = " + requestURI);

            if (session != null) {
                // Mostrar detalles si hay sesión activa
                System.out.println("SessionFilter: Sesión ID = " + session.getId());
                System.out.println("SessionFilter: adminLogueado = " + session.getAttribute("adminLogueado"));
                System.out.println("SessionFilter: adminId = " + session.getAttribute("adminId"));
            } else {
                // Mensaje si no hay sesión activa
                System.out.println("SessionFilter: No hay sesión activa");
            }
        }

        // Continuar con la cadena de filtros o la ejecución del controlador
        chain.doFilter(request, response);
    }

    /**
     * Método que se llama al iniciar el filtro. No se necesita configuración inicial aquí.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No se requiere lógica de inicialización
    }

    /**
     * Método que se llama al destruir el filtro. Tampoco se necesita limpieza aquí.
     */
    @Override
    public void destroy() {
        // No se requiere lógica de destrucción
    }
}
