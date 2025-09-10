package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para manejar las operaciones CRUD relacionadas con los clientes.
 * Expone los servicios REST para listar, obtener, crear, actualizar y eliminar clientes.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen (puede ajustarse según sea necesario)
public class ClienteApiController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Obtiene la lista de todos los clientes.
     * 
     * @return lista de clientes
     */
    @GetMapping
    public List<Cliente> listarClientes() {
        return clienteService.obtenerTodos(); // Llama al servicio para obtener todos los clientes
    }

    /**
     * Obtiene un cliente específico por su ID.
     * 
     * @param id ID del cliente
     * @return cliente encontrado
     */
    @GetMapping("/{id}")
    public Cliente obtenerCliente(@PathVariable Long id) {
        return clienteService.obtenerPorId(id); // Llama al servicio para obtener el cliente por su ID
    }

    /**
     * Crea un nuevo cliente.
     * 
     * @param cliente objeto Cliente con la información a guardar
     * @return cliente creado
     */
    @PostMapping
    public Cliente crearCliente(@RequestBody Cliente cliente) {
        return clienteService.guardar(cliente); // Llama al servicio para guardar el nuevo cliente
    }

    /**
     * Actualiza un cliente existente.
     * 
     * @param id ID del cliente a actualizar
     * @param cliente objeto Cliente con la nueva información
     * @return cliente actualizado
     */
    @PutMapping("/{id}")
    public Cliente actualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        return clienteService.actualizar(id, cliente); // Llama al servicio para actualizar el cliente
    }

    /**
     * Elimina un cliente por su ID.
     * 
     * @param id ID del cliente a eliminar
     */
    @DeleteMapping("/{id}")
    public void eliminarCliente(@PathVariable Long id) {
        clienteService.eliminar(id); // Llama al servicio para eliminar el cliente
    }
}
