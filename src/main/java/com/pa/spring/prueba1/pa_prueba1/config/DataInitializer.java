package com.pa.spring.prueba1.pa_prueba1.config;

import com.pa.spring.prueba1.pa_prueba1.model.Rol;
import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.repository.RolRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            RolRepository rolRepository,
            ClienteRepository clienteRepository,
            BarberoRepository barberoRepository,    
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            System.out.println("Inicializando datos en MySQL...");
            
            // ================== ROLES ==================
            if (rolRepository.count() == 0) {
                System.out.println("Creando roles...");
                
                Rol roleAdmin = new Rol();
                roleAdmin.setNombre("ROLE_ADMIN");
                rolRepository.save(roleAdmin);
                System.out.println("✓ Rol ROLE_ADMIN creado");

                Rol roleUser = new Rol();
                roleUser.setNombre("ROLE_USER");
                rolRepository.save(roleUser);
                System.out.println("✓ Rol ROLE_USER creado");

                Rol roleBarbero = new Rol();
                roleBarbero.setNombre("ROLE_BARBERO");
                rolRepository.save(roleBarbero);
                System.out.println("✓ Rol ROLE_BARBERO creado");
            } else {
                System.out.println("Los roles ya existen en la base de datos");
            }

            // ================== USUARIO ADMIN ==================
            if (clienteRepository.findByCorreo("admin@barberia.com").isEmpty()) {
                System.out.println("Creando usuario administrador...");
                
                Cliente admin = new Cliente();
                admin.setNombre("Administrador");
                admin.setCorreo("admin@barberia.com");
                admin.setClave(passwordEncoder.encode("admin123"));
                admin.setTelefono("3001234567");
                admin.setActivo(true);
                admin.setRol("ROLE_ADMIN");
                
                clienteRepository.save(admin);
                System.out.println("✓ Usuario admin creado: admin@barberia.com / admin123");
            }

            // ================== USUARIO DE PRUEBA ==================
            if (clienteRepository.findByCorreo("usuario@test.com").isEmpty()) {
                System.out.println("Creando usuario de prueba...");
                
                Cliente usuario = new Cliente();
                usuario.setNombre("Usuario Prueba");
                usuario.setCorreo("usuario@test.com");
                usuario.setClave(passwordEncoder.encode("123456"));
                usuario.setTelefono("3007654321");
                usuario.setActivo(true);
                usuario.setRol("ROLE_USER");
                
                clienteRepository.save(usuario);
                System.out.println("✓ Usuario de prueba creado: usuario@test.com / 123456");
            }

        };
    }
}






