package com.pa.spring.prueba1.pa_prueba1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Obtener la ruta absoluta del directorio uploads
            File uploadsDir = new File("uploads");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
                System.out.println("✅ Directorio uploads creado");
            }
            
            File perfilesDir = new File("uploads/perfiles");
            if (!perfilesDir.exists()) {
                perfilesDir.mkdirs();
                System.out.println("✅ Directorio perfiles creado");
            }
            
            String uploadsPath = uploadsDir.getAbsolutePath();
            
            // Configurar el resource handler
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:///" + uploadsPath.replace("\\", "/") + "/")
                    .setCachePeriod(0); // Sin caché para desarrollo
            
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ CONFIGURACIÓN DE UPLOADS COMPLETADA               ║");
            System.out.println("╠════════════════════════════════════════════════════════╣");
            System.out.println("║  📁 Ruta física: " + uploadsPath);
            System.out.println("║  🌐 URL patrón:  /uploads/**");
            System.out.println("║  📸 Ejemplo:     http://localhost:8586/uploads/perfiles/foto.jpg");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR al configurar uploads: " + e.getMessage());
            e.printStackTrace();
        }
    }
}