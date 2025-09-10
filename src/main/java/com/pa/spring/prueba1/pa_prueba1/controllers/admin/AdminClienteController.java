package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para gestionar clientes desde el panel de administración.
 * Permite listar, crear, editar y eliminar clientes.
 */
@Controller
@RequestMapping("/admin/clientes")
public class AdminClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Muestra la lista de todos los clientes.
     * 
     * @param model modelo para pasar datos a la vista
     * @param session sesión HTTP para obtener información del administrador
     * @return vista con la lista de clientes
     */
    @GetMapping
    public String listarClientes(Model model, HttpSession session) {
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        return "admin/clientes/lista";
    }

    /**
     * Muestra el formulario para crear un nuevo cliente.
     * 
     * @param model modelo para pasar datos a la vista
     * @param session sesión HTTP para obtener información del administrador
     * @return vista con el formulario de cliente
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        model.addAttribute("esNuevo", true);
        return "admin/clientes/formulario";
    }

    /**
     * Muestra el formulario para editar un cliente existente.
     * 
     * @param id ID del cliente a editar
     * @param model modelo para pasar datos a la vista
     * @param session sesión HTTP para obtener información del administrador
     * @return vista con el formulario de cliente
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, HttpSession session) {
        Cliente cliente = clienteService.obtenerPorId(id);
        if (cliente == null) {
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        model.addAttribute("esNuevo", false);
        return "admin/clientes/formulario";
    }

    /**
     * Guarda un nuevo cliente o actualiza uno existente.
     * 
     * @param cliente cliente a guardar o actualizar
     * @param redirectAttributes atributos para mensajes flash
     * @return redirección a la lista de clientes
     */
    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        boolean esNuevo = (cliente.getIdCliente() == null);
        
        // Verificar si el correo ya existe (solo para nuevos clientes)
        if (esNuevo && clienteService.existeCliente(cliente.getClave())) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un cliente con ese correo electrónico");
            return "redirect:/admin/clientes/nuevo";
        }
        
        clienteService.guardar(cliente);
        
        String mensaje = esNuevo ? "Cliente creado correctamente" : "Cliente actualizado correctamente";
        redirectAttributes.addFlashAttribute("mensaje", mensaje);
        
        return "redirect:/admin/clientes";
    }

    /**
     * Elimina un cliente por su ID.
     * 
     * @param id ID del cliente a eliminar
     * @param redirectAttributes atributos para mensajes flash
     * @return redirección a la lista de clientes
     */
    @GetMapping("/inhabilitar/{id}")
    public String inhabilitarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clienteService.inhabilitarCliente(id);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente inhabilitado correctamente");
        return "redirect:/admin/clientes";
    }



    /**
     * Muestra los detalles de un cliente específico.
     * 
     * @param id ID del cliente a mostrar
     * @param model modelo para pasar datos a la vista
     * @param session sesión HTTP para obtener información del administrador
     * @return vista con los detalles del cliente
     */
    @GetMapping("/detalle/{id}")
    public String verDetalleCliente(@PathVariable Long id, Model model, HttpSession session) {
        Cliente cliente = clienteService.obtenerPorId(id);
        if (cliente == null) {
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        return "admin/clientes/detalle";
    }
}


