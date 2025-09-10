package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import java.util.List;

public interface ClienteService {

    List<Cliente> obtenerTodos();

    Cliente obtenerPorId(Long id);

    Cliente guardar(Cliente cliente);

    Cliente actualizar(Long id, Cliente cliente);

    void eliminar(Long id);

    Cliente verificarCredenciales(String correo, String clave);

    boolean existeCliente(String correo);

    boolean tieneReservasRelacionadas(Long idCliente);

    void inhabilitarCliente(Long id);

    Cliente obtenerPorCorreo(String correo);
}



