package com.pa.spring.prueba1.pa_prueba1.service.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PerfilService {

    @Autowired
    private BarberoRepository barberoRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Directorio para guardar fotos de perfil
    private static final String UPLOAD_DIR = "uploads/perfiles/";

    /**
     * Obtiene las estadísticas del barbero
     */
    public Map<String, Object> obtenerEstadisticasBarbero(Long idBarbero) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Obtener inicio y fin del mes actual
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            // Reservas del mes
            List<Reserva> reservasMes = reservaRepository
                    .findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                            idBarbero,
                            inicioMes.atStartOfDay(),
                            finMes.atTime(23, 59, 59)
                    );
            
            // Contar cortes completados este mes
            long cortesEsteMes = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();
            
            // Calcular ingresos del mes
            double ingresos = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .filter(r -> r.getCorte() != null)
                    .mapToDouble(r -> r.getCorte().getPrecio())
                    .sum();
            
            // Obtener todas las reservas completadas para calcular satisfacción
            List<Reserva> todasReservas = reservaRepository
                    .findByBarberoIdBarberoAndEstado(
                            idBarbero,
                            Reserva.EstadoReserva.COMPLETADA
                    );
            
            // Calcular satisfacción (simulado - puedes implementar un sistema real de valoraciones)
            double satisfaccion = todasReservas.size() > 0 ? 96.0 : 0.0;
            
            // Calcular valoración promedio (simulado - implementar sistema de valoraciones)
            double valoracion = 4.8;
            long totalValoraciones = todasReservas.size();
            
            // Clientes únicos atendidos
            long clientesAtendidos = todasReservas.stream()
                    .map(r -> r.getCliente().getIdCliente())
                    .distinct()
                    .count();
            
            // Agregar datos al map
            estadisticas.put("cortesEsteMes", cortesEsteMes);
            estadisticas.put("ingresos", formatearMoneda(ingresos));
            estadisticas.put("satisfaccion", satisfaccion);
            estadisticas.put("valoracion", valoracion);
            estadisticas.put("totalValoraciones", totalValoraciones);
            estadisticas.put("clientesAtendidos", clientesAtendidos);
            
        } catch (Exception e) {
            // Valores por defecto en caso de error
            estadisticas.put("cortesEsteMes", 0);
            estadisticas.put("ingresos", "$0");
            estadisticas.put("satisfaccion", 0.0);
            estadisticas.put("valoracion", 0.0);
            estadisticas.put("totalValoraciones", 0);
            estadisticas.put("clientesAtendidos", 0);
        }
        
        return estadisticas;
    }

    /**
     * Cambia la contraseña del barbero
     */
    @Transactional
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Barbero barbero = barberoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));
        
        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, barbero.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(passwordNueva, barbero.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }
        
        // Actualizar contraseña
        barbero.setPassword(passwordEncoder.encode(passwordNueva));
        barberoRepository.save(barbero);
    }

    /**
     * Guarda la foto de perfil del barbero
     */
    @Transactional
    public String guardarFotoPerfil(MultipartFile foto, Long idBarbero) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generar nombre único para el archivo
        String extension = obtenerExtension(foto.getOriginalFilename());
        String nombreArchivo = "barbero_" + idBarbero + "_" + UUID.randomUUID().toString() + extension;
        
        // Guardar archivo
        Path rutaArchivo = uploadPath.resolve(nombreArchivo);
        Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar ruta relativa
        return "/uploads/perfiles/" + nombreArchivo;
    }

    /**
     * Formatea la última sesión del barbero
     */
    public String formatearUltimaSesion(LocalDateTime ultimaSesion) {
        if (ultimaSesion == null) {
            return "Nunca";
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        long minutosDesde = ChronoUnit.MINUTES.between(ultimaSesion, ahora);
        
        if (minutosDesde < 60) {
            return "Hace " + minutosDesde + " minutos";
        }
        
        long horasDesde = ChronoUnit.HOURS.between(ultimaSesion, ahora);
        if (horasDesde < 24) {
            return "Hace " + horasDesde + " hora" + (horasDesde > 1 ? "s" : "");
        }
        
        long diasDesde = ChronoUnit.DAYS.between(ultimaSesion, ahora);
        if (diasDesde == 0) {
            return "Hoy a las " + ultimaSesion.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (diasDesde == 1) {
            return "Ayer a las " + ultimaSesion.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (diasDesde < 7) {
            return "Hace " + diasDesde + " días";
        } else {
            return ultimaSesion.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'a las' h:mm a",
                    java.util.Locale.forLanguageTag("es")));
        }
    }

    /**
     * Formatea un valor monetario
     */
    private String formatearMoneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    /**
     * Obtiene la extensión de un archivo
     */
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".jpg";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }

    /**
     * Actualiza las preferencias de notificaciones
     */
    @Transactional
    public void actualizarPreferenciasNotificaciones(Long idBarbero, 
                                                     boolean notifReservas,
                                                     boolean notifCancelaciones,
                                                     boolean notifRecordatorios) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));
        
        barbero.setNotifReservas(notifReservas);
        barbero.setNotifCancelaciones(notifCancelaciones);
        barbero.setNotifRecordatorios(notifRecordatorios);
        
        barberoRepository.save(barbero);
    }

    /**
     * Activa o desactiva la autenticación en dos pasos
     */
    @Transactional
    public void toggleAutenticacionDosPasos(Long idBarbero, boolean activar) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));
        
        barbero.setAutenticacionDosPasos(activar);
        barberoRepository.save(barbero);
    }

    /**
     * Actualiza la información personal del barbero
     */
    @Transactional
    public void actualizarInformacionPersonal(Long idBarbero, Map<String, String> datos) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));
        
        if (datos.containsKey("nombre")) {
            barbero.setNombre(datos.get("nombre"));
        }
        if (datos.containsKey("apellido")) {
            barbero.setApellido(datos.get("apellido"));
        }
        if (datos.containsKey("documento")) {
            barbero.setDocumento(datos.get("documento"));
        }
        if (datos.containsKey("telefono")) {
            barbero.setTelefono(datos.get("telefono"));
        }
        if (datos.containsKey("email")) {
            // Verificar que el email no esté en uso por otro barbero
            barberoRepository.findByEmail(datos.get("email"))
                    .ifPresent(b -> {
                        if (!b.getIdBarbero().equals(idBarbero)) {
                            throw new RuntimeException("El email ya está en uso");
                        }
                    });
            barbero.setEmail(datos.get("email"));
        }
        if (datos.containsKey("direccion")) {
            barbero.setDireccion(datos.get("direccion"));
        }
        if (datos.containsKey("fechaNacimiento")) {
            barbero.setFechaNacimiento(LocalDate.parse(datos.get("fechaNacimiento")));
        }
        
        barberoRepository.save(barbero);
    }

    /**
     * Actualiza la información profesional del barbero
     */
    @Transactional
    public void actualizarInformacionProfesional(Long idBarbero, Map<String, String> datos) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));
        
        if (datos.containsKey("especialidad")) {
            barbero.setEspecialidad(datos.get("especialidad"));
        }
        if (datos.containsKey("experiencia")) {
            barbero.setExperienciaAnios(Integer.parseInt(datos.get("experiencia")));
        }
        if (datos.containsKey("certificaciones")) {
            barbero.setCertificaciones(datos.get("certificaciones"));
        }
        
        barberoRepository.save(barbero);
    }
}
