package com.pa.spring.prueba1.pa_prueba1.service.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class PerfilService {

    @Autowired
    private BarberoRepository barberoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads/perfiles}")
    private String uploadDir;

    /** Obtiene las estadísticas del barbero */
    public Map<String, Object> obtenerEstadisticasBarbero(Long idBarbero) {
        Map<String, Object> estadisticas = new HashMap<>();
        try {
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            List<Reserva> reservasMes = reservaRepository
                    .findByBarberoIdBarberoAndFechaHoraTurnoBetween(
                            idBarbero,
                            inicioMes.atStartOfDay(),
                            finMes.atTime(23, 59, 59)
                    );

            long cortesEsteMes = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();

            double ingresos = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .filter(r -> r.getCorte() != null)
                    .mapToDouble(r -> r.getCorte().getPrecio())
                    .sum();

            List<Reserva> todasReservas = reservaRepository
                    .findByBarberoIdBarberoAndEstado(idBarbero, Reserva.EstadoReserva.COMPLETADA);

            double satisfaccion = todasReservas.size() > 0 ? 96.0 : 0.0;
            double valoracion = todasReservas.size() > 0 ? 4.8 : 0.0;
            long totalValoraciones = todasReservas.size();

            long clientesAtendidos = todasReservas.stream()
                    .map(r -> r.getCliente().getIdCliente())
                    .distinct()
                    .count();

            estadisticas.put("cortesEsteMes", cortesEsteMes);
            estadisticas.put("ingresos", formatearMoneda(ingresos));
            estadisticas.put("satisfaccion", satisfaccion);
            estadisticas.put("valoracion", valoracion);
            estadisticas.put("totalValoraciones", totalValoraciones);
            estadisticas.put("clientesAtendidos", clientesAtendidos);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            estadisticas.put("cortesEsteMes", 0);
            estadisticas.put("ingresos", "$0");
            estadisticas.put("satisfaccion", 0.0);
            estadisticas.put("valoracion", 0.0);
            estadisticas.put("totalValoraciones", 0);
            estadisticas.put("clientesAtendidos", 0);
        }
        return estadisticas;
    }

    /** Cambia la contraseña del barbero */
    @Transactional
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Barbero barbero = barberoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

        if (!passwordEncoder.matches(passwordActual, barbero.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(passwordNueva, barbero.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        barbero.setPassword(passwordEncoder.encode(passwordNueva));
        barberoRepository.save(barbero);
        System.out.println("✅ Contraseña cambiada para: " + email);
    }

    /** Guarda la foto de perfil del barbero */
    @Transactional
    public String guardarFotoPerfil(MultipartFile foto, Long idBarbero) throws IOException {
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("No se recibió ninguna foto");
        }

        // directorio absoluto basado en el working dir del proceso
        Path uploadPath = Paths.get(System.getProperty("user.dir")).resolve(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("✅ Directorio creado: " + uploadPath.toAbsolutePath());
        }

        Barbero barbero = barberoRepository.findById(idBarbero).orElse(null);
        if (barbero != null && barbero.getFotoPerfil() != null) {
            try {
                String rutaAlmacenada = barbero.getFotoPerfil();
                if (rutaAlmacenada != null && !rutaAlmacenada.isBlank()) {
                    String relativa = rutaAlmacenada.replaceFirst("^/", "");
                    Path fotoAnterior = Paths.get(System.getProperty("user.dir")).resolve(relativa).toAbsolutePath().normalize();
                    Files.deleteIfExists(fotoAnterior);
                    System.out.println("🗑️ Foto anterior eliminada: " + fotoAnterior);
                }
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo eliminar foto anterior: " + e.getMessage());
            }
        }

        String extension = obtenerExtension(foto.getOriginalFilename());
        String nombreArchivo = "barbero_" + idBarbero + "_" + System.currentTimeMillis() + extension;
        Path destinoPath = uploadPath.resolve(nombreArchivo);

        try {
            Files.copy(foto.getInputStream(), destinoPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("No se pudo guardar la foto: " + e.getMessage(), e);
        }

        System.out.println("✅ Foto guardada en: " + destinoPath.toAbsolutePath());
        String rutaUrl = "/" + uploadDir.replaceFirst("^/", "") + "/" + nombreArchivo;

        if (barbero != null) {
            barbero.setFotoPerfil(rutaUrl);
            barberoRepository.save(barbero);
        }

        System.out.println("🌐 URL de la foto: " + rutaUrl);
        return rutaUrl;
    }

    /** Formatea la última sesión del barbero */
    public String formatearUltimaSesion(LocalDateTime ultimaSesion) {
        if (ultimaSesion == null) return "Nunca";
        LocalDateTime ahora = LocalDateTime.now();
        long minutosDesde = ChronoUnit.MINUTES.between(ultimaSesion, ahora);
        if (minutosDesde < 1) return "Hace un momento";
        if (minutosDesde < 60) return "Hace " + minutosDesde + " minuto" + (minutosDesde > 1 ? "s" : "");
        long horasDesde = ChronoUnit.HOURS.between(ultimaSesion, ahora);
        if (horasDesde < 24) return "Hace " + horasDesde + " hora" + (horasDesde > 1 ? "s" : "");
        long diasDesde = ChronoUnit.DAYS.between(ultimaSesion, ahora);
        if (diasDesde == 0) return "Hoy a las " + ultimaSesion.format(DateTimeFormatter.ofPattern("h:mm a"));
        else if (diasDesde == 1) return "Ayer a las " + ultimaSesion.format(DateTimeFormatter.ofPattern("h:mm a"));
        else if (diasDesde < 7) return "Hace " + diasDesde + " día" + (diasDesde > 1 ? "s" : "");
        else return ultimaSesion.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'a las' h:mm a", java.util.Locale.forLanguageTag("es")));
    }

    private String formatearMoneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return ".jpg";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).toLowerCase();
    }
}
