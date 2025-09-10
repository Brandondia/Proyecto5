package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private ClienteService clienteService; // Inyección de dependencia para manejar la lógica del cliente

    /**
     * Muestra el formulario de login. Si ya hay un cliente logueado en la sesión, redirige a la página de reservas.
     * 
     * @param model El modelo utilizado para pasar datos a la vista.
     * @param session La sesión HTTP para verificar si el cliente ya está logueado.
     * @return La vista del formulario de login o redirección a la página de reservas.
     */
    @GetMapping
    public String mostrarFormularioLogin(Model model, HttpSession session) {
        // Verifica si ya existe un cliente logueado en la sesión
        if (session.getAttribute("clienteLogueado") != null) {
            return "redirect:/reserva"; // Si ya está logueado, redirige a la página de reservas
        }
        
        model.addAttribute("cliente", new Cliente()); // Agrega un cliente vacío al modelo para el formulario
        return "login"; // Devuelve la vista del formulario de login
    }

    /**
     * Procesa el inicio de sesión del cliente verificando sus credenciales.
     * Si las credenciales son válidas, crea una sesión y redirige a la página de reservas.
     * Si las credenciales son incorrectas, muestra un mensaje de error.
     * 
     * @param cliente El objeto cliente con los datos ingresados en el formulario de login.
     * @param request La solicitud HTTP para manejar la sesión.
     * @param response La respuesta HTTP para agregar cookies si es necesario.
     * @param redirectAttributes Atributos de redirección para mostrar mensajes de éxito o error.
     * @return Redirige a la página de reservas si el login es exitoso, o muestra un mensaje de error.
     */
    @PostMapping
    public String procesarLogin(@ModelAttribute Cliente cliente, 
                               HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) {
        
        // Verificar las credenciales del cliente (nombre y correo)
        Cliente clienteEncontrado = clienteService.verificarCredenciales(
            cliente.getNombre(), cliente.getClave());
        
        if (clienteEncontrado != null) {
            // Si las credenciales son válidas, crear una nueva sesión (invalidar la anterior si existe)
            HttpSession session = request.getSession(true);
            
            // Guardar los datos del cliente en la sesión
            session.setAttribute("clienteLogueado", clienteEncontrado);
            session.setAttribute("clienteId", clienteEncontrado.getIdCliente());
            
            // Establecer un tiempo de expiración para la sesión (8 horas)
            session.setMaxInactiveInterval(8 * 60 * 60); // 8 horas en segundos
            
            // Crear una cookie de sesión adicional para mayor compatibilidad
            Cookie sessionCookie = new Cookie("CLIENT_AUTH", "true");
            sessionCookie.setPath("/"); // Configura el alcance de la cookie
            sessionCookie.setMaxAge(8 * 60 * 60); // Establece la duración de la cookie (8 horas)
            sessionCookie.setHttpOnly(true); // Hace que la cookie no sea accesible mediante JavaScript
            response.addCookie(sessionCookie);
            
            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("mensaje", "¡Inicio de sesión exitoso!");
            return "redirect:/reserva"; // Redirige a la página de reservas
        } else {
            // Si las credenciales son incorrectas, mostrar mensaje de error
            redirectAttributes.addFlashAttribute("error", 
                "Nombre o correo incorrectos. Si no estás registrado, por favor regístrate primero.");
            return "redirect:/login"; // Redirige al formulario de login con un mensaje de error
        }
    }

    /**
     * Cierra la sesión del cliente, invalida la sesión y elimina la cookie de autenticación.
     * 
     * @param request La solicitud HTTP para invalidar la sesión.
     * @param response La respuesta HTTP para eliminar la cookie de autenticación.
     * @return Redirige a la página principal después de cerrar la sesión.
     */
    @GetMapping("/logout")
    public String cerrarSesion(HttpServletRequest request, HttpServletResponse response) {
        // Invalidar la sesión actual si existe
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalida la sesión
        }
        
        // Eliminar la cookie de autenticación
        Cookie cookie = new Cookie("CLIENT_AUTH", null);
        cookie.setPath("/"); // Configura el alcance de la cookie
        cookie.setMaxAge(0); // Establece que la cookie ya no es válida
        response.addCookie(cookie); // Agrega la cookie a la respuesta para eliminarla
        
        return "redirect:/"; // Redirige al usuario a la página principal
    }
}


