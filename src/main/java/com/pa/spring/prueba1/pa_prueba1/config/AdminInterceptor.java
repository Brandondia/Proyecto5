package com.pa.spring.prueba1.pa_prueba1.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que verifica si un administrador ha iniciado sesión antes de permitir el acceso
 * a rutas protegidas bajo la URL "/admin".
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    /**
     * Este método se ejecuta antes de que un controlador maneje la solicitud HTTP.
     * Se utiliza para verificar la autenticación del administrador.
     *
     * @param request  Solicitud HTTP entrante
     * @param response Respuesta HTTP saliente
     * @param handler  Manejador (controlador) que se ejecutará
     * @return true si la solicitud debe continuar, false si debe ser redirigida
     * @throws Exception si ocurre algún error
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        System.out.println("AdminInterceptor: Verificando acceso a " + requestURI);

        // Permitir el acceso a rutas públicas: login de administrador y recursos estáticos
        if (requestURI.equals("/admin/login") ||
            requestURI.startsWith("/admin/login/") ||
            requestURI.startsWith("/css/") ||
            requestURI.startsWith("/js/") ||
            requestURI.startsWith("/images/") ||
            requestURI.startsWith("/h2-console")) {
            System.out.println("AdminInterceptor: Acceso permitido a recurso público");
            return true;
        }

        // Intentar obtener la sesión existente, si no hay, devuelve null
        HttpSession session = request.getSession(false);

        // Verificar si existe una cookie de autenticación del administrador
        boolean hasAuthCookie = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ADMIN_AUTH".equals(cookie.getName()) && "true".equals(cookie.getValue())) {
                    hasAuthCookie = true;
                    System.out.println("AdminInterceptor: Cookie de autenticación encontrada");
                    break;
                }
            }
        }

        // Si no hay sesión activa
        if (session == null) {
            System.out.println("AdminInterceptor: No hay sesión activa");
            if (hasAuthCookie) {
                // Si hay cookie de autenticación, se podría recrear la sesión
                // pero aquí se decide redirigir al login
                System.out.println("AdminInterceptor: Creando nueva sesión basada en cookie");
                session = request.getSession(true);
                response.sendRedirect("/admin/login");
                return false;
            } else {
                System.out.println("AdminInterceptor: Redirigiendo a login");
                response.sendRedirect("/admin/login");
                return false;
            }
        }

        // Verificar si hay un atributo de administrador logueado en la sesión
        Object adminLogueado = session.getAttribute("adminLogueado");

        if (adminLogueado == null) {
            System.out.println("AdminInterceptor: No hay administrador en sesión (ID: " + session.getId() + ")");
            if (hasAuthCookie) {
                // Si hay cookie de autenticación pero no se ha seteado el administrador en la sesión
                System.out.println("AdminInterceptor: Cookie de autenticación presente, redirigiendo a login");
                response.sendRedirect("/admin/login");
                return false;
            } else {
                System.out.println("AdminInterceptor: Redirigiendo a login");
                response.sendRedirect("/admin/login");
                return false;
            }
        }

        // Si todo está correcto, permitir el acceso
        System.out.println("AdminInterceptor: Acceso permitido para administrador en sesión (ID: " + session.getId() + ")");
        return true;
    }
}
