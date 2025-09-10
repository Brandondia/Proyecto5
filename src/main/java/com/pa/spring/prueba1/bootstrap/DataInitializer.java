package com.pa.spring.prueba1.bootstrap;

import com.pa.spring.prueba1.pa_prueba1.model.Rol;
import com.pa.spring.prueba1.pa_prueba1.model.Usuario;
import com.pa.spring.prueba1.pa_prueba1.repository.RolRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Crear roles si no existen
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
            .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_ADMIN")));

        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
            .orElseGet(() -> rolRepository.save(new Rol(null, "ROLE_USER")));

if (usuarioRepository.findByUsername("admin").isEmpty()) {
    Usuario admin = new Usuario();
    admin.setUsername("admin");
    admin.setPassword(passwordEncoder.encode("admin@123"));
    admin.getRoles().add(rolAdmin);
    admin.getRoles().add(rolUser);
    usuarioRepository.save(admin);
}

if (usuarioRepository.findByUsername("user").isEmpty()) {
    Usuario user = new Usuario();
    user.setUsername("user");
    user.setPassword(passwordEncoder.encode("user@123"));
    user.getRoles().add(rolUser);
    usuarioRepository.save(user);
}
}
}








