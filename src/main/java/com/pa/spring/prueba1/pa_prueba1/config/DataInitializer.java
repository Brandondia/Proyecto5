package com.pa.spring.prueba1.pa_prueba1.config;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.CorteDeCabelloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Clase de configuración que inicializa datos en la base de datos al arrancar la aplicación.
 * Se asegura de que existan datos básicos como administradores, barberos y tipos de cortes.
 */
// Anotación que indica que esta clase contiene beans de configuración
@Configuration
public class DataInitializer {

    // Inyección de dependencias para acceder a los repositorios
    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private BarberoRepository barberoRepository;

    @Autowired
    private CorteDeCabelloRepository corteRepository;

    /**
     * Bean que se ejecuta automáticamente al iniciar la aplicación.
     * Sirve para insertar datos iniciales en la base de datos si no existen.
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            System.out.println("Inicializando datos en MySQL...");

            // ==========================
            // ADMINISTRADOR POR DEFECTO
            // ==========================
            if (administradorRepository.count() == 0) {
                // Si no hay administradores, se crea uno por defecto
                System.out.println("Creando administrador por defecto...");

                Administrador adminDefault = new Administrador();
                adminDefault.setNombre("Administrador");
                adminDefault.setUsuario("admin");
                adminDefault.setPassword("admin123");
                adminDefault.setEmail("admin@barberia.com");
                adminDefault.setNivelAcceso(3); // Nivel más alto de acceso

                administradorRepository.save(adminDefault);

                System.out.println("Administrador por defecto creado con éxito.");
                System.out.println("Usuario: admin, Contraseña: admin123");
            } else {
                // Si ya existen administradores, se listan en consola
                System.out.println("Ya existe al menos un administrador en el sistema.");
                System.out.println("Administradores disponibles:");
                administradorRepository.findAll().forEach(admin -> {
                    System.out.println("- " + admin.getUsuario() + " (Nombre: " + admin.getNombre() + ")");
                });
            }

            // ====================
            // BARBEROS DE EJEMPLO
            // ====================
            if (barberoRepository.count() == 0) {
                System.out.println("Creando barberos de ejemplo...");

                // Crear primer barbero con su horario y día libre
                Barbero barbero1 = new Barbero();
                barbero1.setNombre("Juan Pérez");
                barbero1.setEspecialidad("Cortes clásicos");
                barbero1.setDiaLibre(DayOfWeek.SUNDAY);
                barbero1.setHoraInicio(LocalTime.of(9, 0));
                barbero1.setHoraFin(LocalTime.of(18, 0));
                barbero1.setHoraInicioAlmuerzo(LocalTime.of(13, 0));
                barbero1.setHoraFinAlmuerzo(LocalTime.of(14, 0));
                barbero1.setDuracionTurno(30); // duración en minutos

                // Crear segundo barbero
                Barbero barbero2 = new Barbero();
                barbero2.setNombre("Carlos Rodríguez");
                barbero2.setEspecialidad("Barbas y degradados");
                barbero2.setDiaLibre(DayOfWeek.MONDAY);
                barbero2.setHoraInicio(LocalTime.of(10, 0));
                barbero2.setHoraFin(LocalTime.of(19, 0));
                barbero2.setHoraInicioAlmuerzo(LocalTime.of(14, 0));
                barbero2.setHoraFinAlmuerzo(LocalTime.of(15, 0));
                barbero2.setDuracionTurno(30);

                try {
                    barberoRepository.save(barbero1);
                    barberoRepository.save(barbero2);
                    System.out.println("Barberos de ejemplo creados con éxito.");
                } catch (Exception e) {
                    System.out.println("Error al crear barberos de ejemplo: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // =======================
            // CORTES DE EJEMPLO
            // =======================
            if (corteRepository.count() == 0) {
                System.out.println("Creando cortes de ejemplo...");

                // Crear tres tipos de cortes con sus precios
                CorteDeCabello corte1 = new CorteDeCabello();
                corte1.setNombre("Corte Clásico");
                corte1.setPrecio(15.0);

                CorteDeCabello corte2 = new CorteDeCabello();
                corte2.setNombre("Degradado");
                corte2.setPrecio(20.0);

                CorteDeCabello corte3 = new CorteDeCabello();
                corte3.setNombre("Corte + Barba");
                corte3.setPrecio(25.0);

                // Guardar los cortes en la base de datos
                corteRepository.save(corte1);
                corteRepository.save(corte2);
                corteRepository.save(corte3);

                System.out.println("Cortes de ejemplo creados con éxito.");
            }

            System.out.println("Inicialización de datos completada.");
        };
    }
}
