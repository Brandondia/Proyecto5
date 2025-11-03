package com.pa.spring.prueba1.pa_prueba1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Anotación principal que habilita la configuración automática de Spring Boot
@SpringBootApplication

// Especifica el paquete donde están las entidades JPA para que Spring pueda escanearlas
@EntityScan("com.pa.spring.prueba1.pa_prueba1.model")

// Especifica el paquete donde están los repositorios JPA
@EnableJpaRepositories("com.pa.spring.prueba1.pa_prueba1.repository")

// Permite que Spring escanee servlets personalizados (si los hay)
@ServletComponentScan
public class PaPrueba1Application {

	// Método principal que lanza la aplicación Spring Boot
	public static void main(String[] args) {
		SpringApplication.run(PaPrueba1Application.class, args);
		
		// Mensaje de confirmación cuando la app arranca correctamente
		System.out.println("Aplicación iniciada correctamente con MySQL como base de datos");
	}
	
	// Configuración global de CORS para permitir peticiones desde cualquier origen
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() { 
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") // Aplica a todas las rutas
						.allowedOrigins("*") // Permite peticiones desde cualquier origen
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
						.allowedHeaders("*"); // Cabeceras permitidas
			}
		};
	}
}







	







