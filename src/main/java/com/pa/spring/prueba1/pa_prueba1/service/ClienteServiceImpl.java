package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Para encriptar claves

    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    @Override
    public Cliente guardar(Cliente cliente) {
    // Si no tiene rol, le damos USER por defecto
        if (cliente.getRol() == null || cliente.getRol().isEmpty()) {
            cliente.setRol("ROLE_USER");
        }
    // Encriptamos clave
        cliente.setClave(passwordEncoder.encode(cliente.getClave()));
        return clienteRepository.save(cliente);
        }


    @Override
    public Cliente actualizar(Long id, Cliente cliente) {
        Optional<Cliente> clienteExistente = clienteRepository.findById(id);
        if (clienteExistente.isPresent()) {
            Cliente actualizarCliente = clienteExistente.get();
            actualizarCliente.setNombre(cliente.getNombre());
            actualizarCliente.setCorreo(cliente.getCorreo());
            actualizarCliente.setTelefono(cliente.getTelefono());

            // Si se envía una nueva clave, la encriptamos
            if (cliente.getClave() != null && !cliente.getClave().isEmpty()) {
                if (!cliente.getClave().startsWith("$2a$") && !cliente.getClave().startsWith("$2b$")) {
                    actualizarCliente.setClave(passwordEncoder.encode(cliente.getClave()));
                } else {
                    actualizarCliente.setClave(cliente.getClave());
                }
            }

            return clienteRepository.save(actualizarCliente);
        } else {
            return null;
        }
    }

    @Override
    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }

    @Override
    public Cliente verificarCredenciales(String correo, String clave) {
        // ⚠️ Spring Security se encarga normalmente de la autenticación,
        // pero dejamos este helper por si lo usas en algún flujo manual.
        return clienteRepository.findByCorreo(correo)
                .filter(c -> passwordEncoder.matches(clave, c.getClave()))
                .orElse(null);
    }

    @Override
    public boolean existeCliente(String correo) {
        return clienteRepository.findByCorreo(correo).isPresent();
    }

    @Override
    public boolean tieneReservasRelacionadas(Long idCliente) {
        return reservaRepository.existsByCliente_IdCliente(idCliente);
    }

    @Override
    public void inhabilitarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Asegúrate que tu entidad Cliente tenga el campo 'activo'
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    // Nuevo método para obtener cliente por correo (necesario para controllers)
    @Override
    public Cliente obtenerPorCorreo(String correo) {
        return clienteRepository.findByCorreo(correo).orElse(null);
    }
}












