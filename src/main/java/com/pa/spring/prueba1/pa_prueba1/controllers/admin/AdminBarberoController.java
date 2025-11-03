package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/barberos")
public class AdminBarberoController {

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private BarberoRepository barberoRepository;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== LISTAR BARBEROS (VISTA UNIFICADA) ====================
    @GetMapping
    @Transactional(readOnly = true)
    public String listarBarberos(Model model) {
        List<Barbero> barberos = barberoService.obtenerTodos();
        
        // Estadísticas
        long activos = barberos.stream().filter(Barbero::isActivo).count();
        long inactivos = barberos.size() - activos;
        
        model.addAttribute("barberos", barberos);
        model.addAttribute("totalBarberos", barberos.size());
        model.addAttribute("barberosActivos", activos);
        model.addAttribute("barberosInactivos", inactivos);
        
        return "admin/barberos/lista";
    }

    // ==================== FORMULARIO NUEVO BARBERO ====================
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        Barbero barbero = new Barbero();
        // Valores por defecto
        barbero.setActivo(true);
        barbero.setNotifReservas(true);
        barbero.setNotifCancelaciones(true);
        barbero.setNotifRecordatorios(true);
        barbero.setAutenticacionDosPasos(false);
        barbero.setRol("ROLE_BARBERO"); // ✅ ROL POR DEFECTO
        
        model.addAttribute("barbero", barbero);
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        return "admin/barberos/formulario";
    }

    // ==================== GUARDAR BARBERO ====================
    @PostMapping("/guardar")
    @Transactional
    public String guardarBarbero(@ModelAttribute Barbero barbero, RedirectAttributes redirectAttributes) {
        try {
            // Validar email único para nuevos barberos
            if (barbero.getIdBarbero() == null) {
                // ==================== CREANDO NUEVO BARBERO ====================
                
                // Validar email único
                if (barberoRepository.existsByEmail(barbero.getEmail())) {
                    redirectAttributes.addFlashAttribute("error", "Ya existe un barbero con ese correo electrónico");
                    return "redirect:/admin/barberos/nuevo";
                }
                
                // Validar contraseña
                if (barbero.getPassword() == null || barbero.getPassword().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debe proporcionar una contraseña para el nuevo barbero");
                    return "redirect:/admin/barberos/nuevo";
                }
                
                // Validar longitud de contraseña
                if (barbero.getPassword().length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                    return "redirect:/admin/barberos/nuevo";
                }
                
                // ✅ Encriptar contraseña
                barbero.setPassword(passwordEncoder.encode(barbero.getPassword()));
                
                // ✅ Asignar rol de barbero (CRÍTICO para autenticación)
                if (barbero.getRol() == null || barbero.getRol().isEmpty()) {
                    barbero.setRol("ROLE_BARBERO");
                }
                
                // Fecha de ingreso
                barbero.setFechaIngreso(LocalDate.now());
                
                // Valores por defecto para notificaciones
                if (barbero.getNotifReservas() == null) barbero.setNotifReservas(true);
                if (barbero.getNotifCancelaciones() == null) barbero.setNotifCancelaciones(true);
                if (barbero.getNotifRecordatorios() == null) barbero.setNotifRecordatorios(true);
                if (barbero.getAutenticacionDosPasos() == null) barbero.setAutenticacionDosPasos(false);
                
            } else {
                // ==================== EDITANDO BARBERO EXISTENTE ====================
                
                Barbero barberoExistente = barberoService.obtenerPorId(barbero.getIdBarbero());
                if (barberoExistente == null) {
                    redirectAttributes.addFlashAttribute("error", "Barbero no encontrado");
                    return "redirect:/admin/barberos";
                }
                
                // Solo actualizar contraseña si se proporciona una nueva
                if (barbero.getPassword() != null && !barbero.getPassword().isEmpty()) {
                    if (barbero.getPassword().length() < 6) {
                        redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                        return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
                    }
                    barbero.setPassword(passwordEncoder.encode(barbero.getPassword()));
                } else {
                    barbero.setPassword(barberoExistente.getPassword());
                }
                
                // ✅ Mantener rol existente
                barbero.setRol(barberoExistente.getRol());
                
                // Mantener fecha de ingreso y última sesión
                barbero.setFechaIngreso(barberoExistente.getFechaIngreso());
                barbero.setUltimaSesion(barberoExistente.getUltimaSesion());
                
                // Mantener foto de perfil si no se cambió
                if (barbero.getFotoPerfil() == null || barbero.getFotoPerfil().isEmpty()) {
                    barbero.setFotoPerfil(barberoExistente.getFotoPerfil());
                }
            }

            // ==================== VALORES POR DEFECTO PARA HORARIOS ====================
            
            if (barbero.getHoraInicio() == null) {
                barbero.setHoraInicio(LocalTime.of(8, 0));
            }
            if (barbero.getHoraFin() == null) {
                barbero.setHoraFin(LocalTime.of(18, 0));
            }
            if (barbero.getHoraInicioAlmuerzo() == null) {
                barbero.setHoraInicioAlmuerzo(LocalTime.of(12, 0));
            }
            if (barbero.getHoraFinAlmuerzo() == null) {
                barbero.setHoraFinAlmuerzo(LocalTime.of(13, 0));
            }
            if (barbero.getDuracionTurno() == null) {
                barbero.setDuracionTurno(30);
            }

            // ==================== GUARDAR BARBERO ====================
            
            barberoService.guardar(barbero);
            
            // Mensaje de éxito personalizado
            String mensaje;
            if (barbero.getIdBarbero() == null) {
                mensaje = String.format(
                    "✅ Barbero '%s' creado exitosamente. Credenciales de acceso:<br>" +
                    "📧 Email: <strong>%s</strong><br>" +
                    "🔐 Contraseña: (la que ingresaste)<br>" +
                    "🌐 Panel: <a href='/barbero/panel'>/barbero/panel</a>",
                    barbero.getNombreCompleto(),
                    barbero.getEmail()
                );
            } else {
                mensaje = String.format("✅ Barbero '%s' actualizado exitosamente", barbero.getNombreCompleto());
            }
            
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al guardar el barbero: " + e.getMessage());
            e.printStackTrace();
            
            // Redirigir al formulario correspondiente
            if (barbero.getIdBarbero() == null) {
                return "redirect:/admin/barberos/nuevo";
            } else {
                return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
            }
        }
        return "redirect:/admin/barberos";
    }

    // ==================== FORMULARIO EDITAR ====================
    @GetMapping("/editar/{id}")
    @Transactional(readOnly = true)
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Barbero barbero = barberoService.obtenerPorId(id);
        if (barbero == null) {
            return "redirect:/admin/barberos";
        }
        model.addAttribute("barbero", barbero);
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        model.addAttribute("editando", true);
        return "admin/barberos/formulario";
    }

    // ==================== CAMBIAR ESTADO (ACTIVAR/DESACTIVAR) ====================
    @PostMapping("/cambiar-estado/{id}")
    @Transactional
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = barberoService.obtenerPorId(id);
            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "Barbero no encontrado");
                return "redirect:/admin/barberos";
            }
            
            boolean nuevoEstado = !barbero.isActivo();
            
            // Usar consulta JPQL para actualizar SOLO el campo activo
            int filasActualizadas = barberoRepository.actualizarEstado(id, nuevoEstado);
            
            if (filasActualizadas > 0) {
                String estado = nuevoEstado ? "activado" : "desactivado";
                String mensaje = "Barbero " + estado + " exitosamente. ";
                
                if (!nuevoEstado) {
                    mensaje += "El barbero no podrá iniciar sesión hasta que se reactive su cuenta.";
                } else {
                    mensaje += "El barbero ya puede iniciar sesión nuevamente.";
                }
                
                redirectAttributes.addFlashAttribute("mensaje", mensaje);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado del barbero");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/barberos";
    }

    // ==================== RESETEAR CONTRASEÑA ====================
    @PostMapping("/resetear-password")
    @Transactional
    public String resetearPassword(@RequestParam Long id,
                                   @RequestParam String nuevaPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (nuevaPassword == null || nuevaPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                return "redirect:/admin/barberos";
            }
            
            Barbero barbero = barberoService.obtenerPorId(id);
            if (barbero != null) {
                barbero.setPassword(passwordEncoder.encode(nuevaPassword));
                barberoService.guardar(barbero);
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Contraseña actualizada exitosamente. El barbero debe usar la nueva contraseña para iniciar sesión.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Barbero no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al resetear contraseña: " + e.getMessage());
        }
        return "redirect:/admin/barberos";
    }

    // ==================== ELIMINAR BARBERO (SOFT DELETE) ====================
    @GetMapping("/eliminar/{id}")
    @Transactional
    public String eliminarBarbero(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = barberoService.obtenerPorId(id);
            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "Barbero no encontrado");
                return "redirect:/admin/barberos";
            }
            
            // Si ya está inactivo, informar
            if (!barbero.isActivo()) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "El barbero ya estaba desactivado. Sus datos se mantienen en el sistema.");
                return "redirect:/admin/barberos";
            }
            
            // SOFT DELETE: Usar consulta JPQL para actualizar SOLO el campo activo
            int filasActualizadas = barberoRepository.actualizarEstado(id, false);
            
            if (filasActualizadas > 0) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Barbero desactivado exitosamente. No podrá iniciar sesión pero sus datos históricos se mantienen. " +
                    "Puede reactivarlo usando el botón 'Activar'.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo desactivar el barbero");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al desactivar el barbero: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/barberos";
    }

    // ==================== GESTIONAR TURNOS DEL BARBERO ====================
    @GetMapping("/{id}/turnos")
    @Transactional(readOnly = true)
    public String gestionarTurnos(@PathVariable Long id, Model model) {
        Barbero barbero = barberoService.obtenerPorId(id);
        if (barbero == null) {
            return "redirect:/admin/barberos";
        }

        model.addAttribute("barbero", barbero);

        // Obtener turnos disponibles y reservados
        List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorBarbero(id);
        List<Turno> turnosReservados = turnoService.obtenerTurnosNoDisponiblesPorBarbero(id);

        model.addAttribute("turnosDisponibles", turnosDisponibles);
        model.addAttribute("turnosReservados", turnosReservados);

        // Fechas por defecto para generar turnos
        model.addAttribute("fechaInicio", LocalDate.now());
        model.addAttribute("fechaFin", LocalDate.now().plusDays(14));

        return "admin/barberos/turnos";
    }

    // ==================== GENERAR TURNOS AUTOMÁTICAMENTE ====================
    @PostMapping("/{id}/generar-turnos")
    @Transactional
    public String generarTurnos(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            RedirectAttributes redirectAttributes) {

        try {
            Barbero barbero = barberoService.obtenerPorId(id);

            if (barbero != null) {
                // Validar que el barbero tenga configuración de horario
                if (!barbero.tieneHorarioCompleto()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "El barbero debe tener configurado su horario completo (hora inicio, hora fin y duración de turno)");
                    return "redirect:/admin/barberos/" + id + "/turnos";
                }
                
                // Validar fechas
                if (fechaFin.isBefore(fechaInicio)) {
                    redirectAttributes.addFlashAttribute("error", "La fecha de fin debe ser posterior a la fecha de inicio");
                    return "redirect:/admin/barberos/" + id + "/turnos";
                }
                
                // Generar turnos
                List<Turno> turnosGenerados = turnoService.generarTurnosDisponibles(barbero, fechaInicio, fechaFin);
                
                redirectAttributes.addFlashAttribute("mensaje",
                        "Se han generado " + turnosGenerados.size() + " turnos disponibles para " + barbero.getNombreCompleto());
            } else {
                redirectAttributes.addFlashAttribute("error", "Barbero no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al generar turnos: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/barberos/" + id + "/turnos";
    }
}
