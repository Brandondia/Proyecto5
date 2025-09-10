package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Implementación del servicio de Administrador
@Service
public class AdministradorServiceImpl implements AdministradorService {

    // Inyección de dependencia para el repositorio de Administrador
    @Autowired
    private AdministradorRepository administradorRepository;
    
    // Obtiene todos los administradores
    @Override
    public List<Administrador> obtenerTodos() {
        return administradorRepository.findAll();
    }
    
    // Obtiene un administrador por su ID
    @Override
    public Administrador obtenerPorId(Long id) {
        // Si no existe un administrador con el ID dado, retorna null
        Optional<Administrador> optAdmin = administradorRepository.findById(id);
        return optAdmin.orElse(null);
    }
    
    // Guarda o actualiza un administrador
    @Override
    public Administrador guardar(Administrador administrador) {
        return administradorRepository.save(administrador);
    }
    
    // Elimina un administrador por su ID
    @Override
    public void eliminar(Long id) {
        administradorRepository.deleteById(id);
    }
    
    // Verifica las credenciales de un administrador (usuario y contraseña)
    @Override
    public Administrador verificarCredenciales(String usuario, String password) {
        System.out.println("Verificando credenciales para usuario: " + usuario);
        
        // Busca un administrador por usuario y contraseña
        Administrador admin = administradorRepository.findByUsuarioAndPassword(usuario, password);
        
        // Si no se encuentra el administrador con esas credenciales
        if (admin != null) {
            System.out.println("Administrador encontrado: " + admin.getNombre());
        } else {
            System.out.println("No se encontró ningún administrador con esas credenciales");
            // Intento de depuración: buscar solo por usuario
            Administrador adminPorUsuario = administradorRepository.findByUsuario(usuario);
            if (adminPorUsuario != null) {
                System.out.println("Se encontró el usuario pero la contraseña no coincide");
            } else {
                System.out.println("No se encontró ningún usuario con ese nombre");
            }
        }
        return admin;
    }
    
    // Verifica si ya existe un administrador con el nombre de usuario dado
    @Override
    public boolean existeAdministrador(String usuario) {
        return administradorRepository.findByUsuario(usuario) != null;
    }
}
