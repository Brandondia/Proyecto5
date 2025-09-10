package com.pa.spring.prueba1.pa_prueba1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase de configuración de Spring MVC que registra interceptores personalizados.
 * En este caso, registra el AdminInterceptor para proteger rutas del panel de administrador.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    /**
     * Método que registra interceptores en la aplicación web.
     *
     * @param registry objeto que mantiene la lista de interceptores.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                // Aplica el interceptor a todas las rutas bajo /admin/**
                .addPathPatterns("/admin/**")
                // Excluye de la verificación las siguientes rutas públicas
                .excludePathPatterns(
                    "/admin/login",         // Página de login de administrador
                    "/admin/login/**",      // Subrutas del login
                    "/css/**",              // Archivos de estilo
                    "/js/**",               // Archivos JavaScript
                    "/images/**",           // Imágenes públicas
                    "/h2-console/**"        // Consola de la base de datos H2 (opcional para desarrollo)
                );
    }
}
