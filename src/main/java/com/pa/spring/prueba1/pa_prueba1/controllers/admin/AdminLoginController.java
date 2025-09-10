package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.service.AdministradorService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador que maneja las peticiones relacionadas con el inicio de sesión del administrador.
 * Permite al administrador iniciar sesión, cerrar sesión y configurar un administrador inicial si no existe ninguno.
 */
@Controller
@RequestMapping("/admin/login")
public class AdminLoginController {

    @Autowired
    private AdministradorService administradorService;
    
    /**
     * Muestra el formulario de inicio de sesión.
     * Si ya hay un administrador en sesión, redirige al panel de administración.
     * 
     * @param model objeto para enviar datos a la vista
     * @param session objeto de sesión HTTP
     * @return vista del formulario de login o redirige al panel si ya está logueado
     */
    @GetMapping
    public String mostrarFormularioLogin(Model model, HttpSession session) {
        // Si ya hay un administrador en sesión, redirigir al panel
        if (session.getAttribute("adminLogueado") != null) {
            return "redirect:/admin";
        }
        
        return "admin/login"; // Retorna la vista de login
    }
    
    /**
     * Procesa el inicio de sesión del administrador.
     * Verifica las credenciales y, si son correctas, crea una nueva sesión.
     * 
     * @param usuario nombre de usuario proporcionado
     * @param password contraseña proporcionada
     * @param request objeto de la solicitud HTTP
     * @param response objeto de la respuesta HTTP
     * @param redirectAttributes para enviar un mensaje de éxito o error
     * @return redirige al panel de administración si el login es exitoso, o redirige al login si falla
     */
    @PostMapping
    public String procesarLogin(@RequestParam("usuario") String usuario, 
                               @RequestParam("password") String password,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes) {
        
        System.out.println("Intento de login: Usuario=" + usuario + ", Password=" + password);
        
        // Verificar credenciales
        Administrador adminEncontrado = administradorService.verificarCredenciales(usuario, password);
        
        if (adminEncontrado != null) {
            // Crear una nueva sesión (invalidar la anterior si existe)
            HttpSession session = request.getSession(true);
            
            // Guardar administrador en sesión
            session.setAttribute("adminLogueado", adminEncontrado);
            session.setAttribute("adminId", adminEncontrado.getId());
            session.setAttribute("adminNivel", adminEncontrado.getNivelAcceso());
        
            System.out.println("Login exitoso para: " + adminEncontrado.getNombre());
            System.out.println("Sesión establecida: adminLogueado=" + session.getAttribute("adminLogueado"));
            System.out.println("ID de sesión: " + session.getId());
        
            // Establecer un tiempo de expiración más largo (8 horas en segundos)
            session.setMaxInactiveInterval(8 * 60 * 60);
            
            // Crear una cookie de sesión adicional para mayor compatibilidad
            Cookie sessionCookie = new Cookie("ADMIN_AUTH", "true");
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(8 * 60 * 60); // 8 horas en segundos
            sessionCookie.setHttpOnly(true);
            response.addCookie(sessionCookie);
        
            redirectAttributes.addFlashAttribute("mensaje", "¡Inicio de sesión exitoso!");
            return "redirect:/admin"; // Redirige al panel administrativo
        } else {
            // Credenciales incorrectas
            System.out.println("Login fallido: credenciales incorrectas");
        
            redirectAttributes.addFlashAttribute("error", 
                "Usuario o contraseña incorrectos.");
            return "redirect:/admin/login"; // Redirige de nuevo al formulario de login
        }
    }
    
    /**
     * Cierra la sesión del administrador y elimina la cookie de autenticación.
     * 
     * @param request objeto de la solicitud HTTP
     * @param response objeto de la respuesta HTTP
     * @return redirige al formulario de login
     */
    @GetMapping("/logout")
    public String cerrarSesion(HttpServletRequest request, HttpServletResponse response) {
        // Invalidar la sesión
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Elimina la sesión existente
        }
        
        // Eliminar la cookie de autenticación
        Cookie cookie = new Cookie("ADMIN_AUTH", null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Establece la cookie a un tiempo de expiración 0 para eliminarla
        response.addCookie(cookie);
        
        return "redirect:/admin/login"; // Redirige al formulario de login
    }
    
    /**
     * Método para crear un administrador inicial si no existe ninguno en el sistema.
     * 
     * @param redirectAttributes para enviar un mensaje de éxito o error
     * @return redirige al formulario de login
     */
    @GetMapping("/setup")
    public String crearAdminInicial(RedirectAttributes redirectAttributes) {
        // Verificar si ya existen administradores
        if (!administradorService.obtenerTodos().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", 
                "Ya existen administradores en el sistema.");
            return "redirect:/admin/login"; // Redirige si ya hay administradores
        }
        
        // Crear administrador por defecto
        Administrador adminDefault = new Administrador();
        adminDefault.setNombre("Administrador");
        adminDefault.setUsuario("admin");
        adminDefault.setPassword("admin123");
        adminDefault.setEmail("admin@barberia.com");
        adminDefault.setNivelAcceso(3);
        
        administradorService.guardar(adminDefault); // Guarda el administrador por defecto
        
        redirectAttributes.addFlashAttribute("mensaje", 
            "Administrador inicial creado con éxito. Usuario: admin, Contraseña: admin123");
        
        return "redirect:/admin/login"; // Redirige al login después de la creación
    }
}
