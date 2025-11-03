package com.pa.spring.prueba1.pa_prueba1.controllers.barbero;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/barbero/perfil")
public class PerfilController {

    private final BarberoService barberoService;
    private final PerfilService perfilService;

    @Autowired
    public PerfilController(BarberoService barberoService, PerfilService perfilService) {
        this.barberoService = barberoService;
        this.perfilService = perfilService;
    }

    /**
     * Obtiene el barbero autenticado
     */
    private Barbero obtenerBarberoActual(Authentication auth) {
        return barberoService.obtenerBarberoPorEmail(auth.getName());
    }

    /**
     * VER PERFIL - Vista principal
     */
    @GetMapping
    public String verPerfil(Model model, Authentication auth) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);

            // Información básica
            model.addAttribute("nombreBarbero", barbero.getNombre());
            model.addAttribute("barbero", barbero);
            
            // Información personal
            model.addAttribute("nombreCompleto", barbero.getNombre() + 
                    (barbero.getApellido() != null ? " " + barbero.getApellido() : ""));
            model.addAttribute("documento", barbero.getDocumento() != null ? 
                    barbero.getDocumento() : "No registrado");
            model.addAttribute("fechaNacimiento", barbero.getFechaNacimiento() != null ? 
                    barbero.getFechaNacimiento().format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", 
                    java.util.Locale.forLanguageTag("es"))) : "No registrada");
            model.addAttribute("email", barbero.getEmail());
            model.addAttribute("telefono", barbero.getTelefono() != null ? 
                    barbero.getTelefono() : "No registrado");
            model.addAttribute("direccion", barbero.getDireccion() != null ? 
                    barbero.getDireccion() : "No registrada");
            
            // Información profesional
            model.addAttribute("anosExperiencia", barbero.getExperienciaAnios() != null ? 
                    barbero.getExperienciaAnios() + " años" : "No especificado");
            model.addAttribute("fechaIngreso", barbero.getFechaIngreso() != null ? 
                    barbero.getFechaIngreso().format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", 
                    java.util.Locale.forLanguageTag("es"))) : "No registrada");
            model.addAttribute("especialidad", barbero.getEspecialidad() != null ? 
                    barbero.getEspecialidad() : "Barbero General");
            model.addAttribute("certificaciones", barbero.getCertificaciones());
            
            // Estadísticas
            Map<String, Object> estadisticas = perfilService.obtenerEstadisticasBarbero(barbero.getIdBarbero());
            model.addAttribute("cortesEsteMes", estadisticas.get("cortesEsteMes"));
            model.addAttribute("ingresos", estadisticas.get("ingresos"));
            model.addAttribute("satisfaccion", estadisticas.get("satisfaccion"));
            model.addAttribute("valoracion", estadisticas.get("valoracion"));
            model.addAttribute("totalValoraciones", estadisticas.get("totalValoraciones"));
            model.addAttribute("clientesAtendidos", estadisticas.get("clientesAtendidos"));
            
            // Seguridad
            model.addAttribute("ultimaSesion", barbero.getUltimaSesion() != null ? 
                    perfilService.formatearUltimaSesion(barbero.getUltimaSesion()) : "Nunca");
            
            // Preferencias
            model.addAttribute("notifReservas", barbero.getNotifReservas() != null ? 
                    barbero.getNotifReservas() : true);
            model.addAttribute("notifCancelaciones", barbero.getNotifCancelaciones() != null ? 
                    barbero.getNotifCancelaciones() : true);
            model.addAttribute("notifRecordatorios", barbero.getNotifRecordatorios() != null ? 
                    barbero.getNotifRecordatorios() : true);
            model.addAttribute("autenticacionDosPasos", barbero.getAutenticacionDosPasos() != null ? 
                    barbero.getAutenticacionDosPasos() : false);
            
            // Foto de perfil
            model.addAttribute("fotoPerfil", barbero.getFotoPerfil() != null ? 
                    barbero.getFotoPerfil() : generarAvatarURL(barbero.getNombre()));

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar perfil: " + e.getMessage());
        }
        return "barbero/perfil";
    }

    /**
     * ACTUALIZAR INFORMACIÓN PERSONAL
     */
    @PostMapping("/actualizar")
    public String actualizarPerfil(@RequestParam Map<String, String> params,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);

            // Actualizar campos
            if (params.containsKey("nombre")) {
                barbero.setNombre(params.get("nombre"));
            }
            if (params.containsKey("documento")) {
                barbero.setDocumento(params.get("documento"));
            }
            if (params.containsKey("fechaNacimiento")) {
                LocalDate fecha = LocalDate.parse(params.get("fechaNacimiento"));
                barbero.setFechaNacimiento(fecha);
            }
            if (params.containsKey("telefono")) {
                barbero.setTelefono(params.get("telefono"));
            }
            if (params.containsKey("email")) {
                barbero.setEmail(params.get("email"));
            }
            if (params.containsKey("direccion")) {
                barbero.setDireccion(params.get("direccion"));
            }

            barberoService.actualizarBarbero(barbero);
            redirectAttributes.addFlashAttribute("mensaje", "Información personal actualizada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/barbero/perfil";
    }

    /**
     * ACTUALIZAR INFORMACIÓN PROFESIONAL
     */
    @PostMapping("/actualizar-profesional")
    public String actualizarProfesional(@RequestParam Map<String, String> params,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = obtenerBarberoActual(auth);

            if (params.containsKey("experiencia")) {
                barbero.setExperienciaAnios(Integer.parseInt(params.get("experiencia")));
            }
            if (params.containsKey("certificaciones")) {
                barbero.setCertificaciones(params.get("certificaciones"));
            }
            
            // Construir especialidades desde checkboxes
            StringBuilder especialidades = new StringBuilder();
            if (params.containsKey("esp1")) especialidades.append("Cortes Clásicos,");
            if (params.containsKey("esp2")) especialidades.append("Fade,");
            if (params.containsKey("esp3")) especialidades.append("Barba,");
            if (params.containsKey("esp4")) especialidades.append("Degradados,");
            if (params.containsKey("esp5")) especialidades.append("Rapado,");
            if (params.containsKey("esp6")) especialidades.append("Diseño,");
            
            if (especialidades.length() > 0) {
                especialidades.deleteCharAt(especialidades.length() - 1); // Quitar última coma
                barbero.setEspecialidad(especialidades.toString());
            }

            barberoService.actualizarBarbero(barbero);
            redirectAttributes.addFlashAttribute("mensaje", "Información profesional actualizada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/barbero/perfil";
    }

    /**
     * CAMBIAR CONTRASEÑA
     */
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam("passwordActual") String passwordActual,
                                  @RequestParam("passwordNueva") String passwordNueva,
                                  @RequestParam("passwordConfirmar") String passwordConfirmar,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas coincidan
            if (!passwordNueva.equals(passwordConfirmar)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                return "redirect:/barbero/perfil";
            }
            
            // Validar longitud mínima
            if (passwordNueva.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                return "redirect:/barbero/perfil";
            }

            String email = auth.getName();
            perfilService.cambiarPassword(email, passwordActual, passwordNueva);
            
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada correctamente");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar contraseña: " + e.getMessage());
        }
        return "redirect:/barbero/perfil";
    }

    /**
     * CAMBIAR FOTO DE PERFIL
     */
    @PostMapping("/cambiar-foto")
    public String cambiarFoto(@RequestParam("foto") MultipartFile foto,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validar archivo
            if (foto.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar una foto");
                return "redirect:/barbero/perfil";
            }
            
            // Validar tamaño (máximo 2MB)
            if (foto.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "La foto no debe superar los 2MB");
                return "redirect:/barbero/perfil";
            }
            
            // Validar tipo de archivo
            String contentType = foto.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && 
                !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten imágenes JPG o PNG");
                return "redirect:/barbero/perfil";
            }

            Barbero barbero = obtenerBarberoActual(auth);
            String rutaFoto = perfilService.guardarFotoPerfil(foto, barbero.getIdBarbero());
            
            barbero.setFotoPerfil(rutaFoto);
            barberoService.actualizarBarbero(barbero);
            
            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil actualizada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir foto: " + e.getMessage());
        }
        return "redirect:/barbero/perfil";
    }

    /**
     * GUARDAR PREFERENCIAS
     */
    @PostMapping("/guardar-preferencias")
    @ResponseBody
    public Map<String, Object> guardarPreferencias(@RequestBody Map<String, Object> preferencias,
                                                   Authentication auth) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            
            // Actualizar preferencias de notificaciones
            if (preferencias.containsKey("notifReservas")) {
                barbero.setNotifReservas((Boolean) preferencias.get("notifReservas"));
            }
            if (preferencias.containsKey("notifCancelaciones")) {
                barbero.setNotifCancelaciones((Boolean) preferencias.get("notifCancelaciones"));
            }
            if (preferencias.containsKey("notifRecordatorios")) {
                barbero.setNotifRecordatorios((Boolean) preferencias.get("notifRecordatorios"));
            }
            
            barberoService.actualizarBarbero(barbero);
            
            response.put("success", true);
            response.put("message", "Preferencias guardadas correctamente");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar preferencias: " + e.getMessage());
        }
        return response;
    }

    /**
     * ACTIVAR/DESACTIVAR AUTENTICACIÓN EN DOS PASOS
     */
    @PostMapping("/toggle-2fa")
    @ResponseBody
    public Map<String, Object> toggleAutenticacionDosPasos(@RequestParam("activar") boolean activar,
                                                           Authentication auth) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            Barbero barbero = obtenerBarberoActual(auth);
            barbero.setAutenticacionDosPasos(activar);
            barberoService.actualizarBarbero(barbero);
            
            response.put("success", true);
            response.put("message", activar ? 
                    "Autenticación en dos pasos activada" : 
                    "Autenticación en dos pasos desactivada");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar configuración: " + e.getMessage());
        }
        return response;
    }

    /**
     * Genera URL de avatar por defecto
     */
    private String generarAvatarURL(String nombre) {
        String nombreEncoded = nombre.replace(" ", "+");
        return "https://ui-avatars.com/api/?name=" + nombreEncoded + 
               "&size=200&background=0d6efd&color=fff";
    }
}
