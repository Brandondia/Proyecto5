package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔎 Intentando login con: " + username);

        Cliente cliente = clienteRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // DEBUG
        System.out.println("✅ Cliente encontrado: " + cliente.getCorreo());
        System.out.println("🔑 Clave en BD: " + cliente.getClave());
        System.out.println("🎭 Rol en BD: " + cliente.getRol());
        System.out.println("📌 Activo: " + cliente.isActivo());
        

        return User.builder()
                .username(cliente.getCorreo())
                .password(cliente.getClave())
                .roles(cliente.getRol().replace("ROLE_", "")) // ejemplo: ROLE_ADMIN → ADMIN
                .disabled(!cliente.isActivo())
                .build();
    }
}
























