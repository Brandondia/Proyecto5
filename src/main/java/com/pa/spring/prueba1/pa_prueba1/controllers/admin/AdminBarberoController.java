package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.barbero.BarberoService;
import com.pa.spring.prueba1.pa_prueba1.repository.BarberoRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.service.EmailService;

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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador de Administración de Barberos
 * Incluye desvinculación normal y de emergencia
 * 
 * @author Tu Nombre
 * @version 2.0 - Con soft delete completo
 */
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
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private EmailService emailService;

    // ==================== LISTAR BARBEROS ====================
    
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
        barbero.setActivo(true);
        barbero.setNotifReservas(true);
        barbero.setNotifCancelaciones(true);
        barbero.setNotifRecordatorios(true);
        barbero.setAutenticacionDosPasos(false);
        barbero.setRol("ROLE_BARBERO");

        model.addAttribute("barbero", barbero);
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        return "admin/barberos/formulario";
    }

    // ==================== GUARDAR BARBERO ====================
    
    @PostMapping("/guardar")
    @Transactional
    public String guardarBarbero(
            @ModelAttribute Barbero barbero,
            @RequestParam(required = false) String confirmarPassword,
            RedirectAttributes redirectAttributes) {
        try {
            if (barbero.getIdBarbero() == null) {
                // CREANDO NUEVO BARBERO
                if (barbero.getPassword() == null || barbero.getPassword().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ Debe proporcionar una contraseña para el nuevo barbero");
                    redirectAttributes.addFlashAttribute("barbero", barbero);
                    return "redirect:/admin/barberos/nuevo";
                }

                if (barbero.getPassword().length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "❌ La contraseña debe tener al menos 6 caracteres");
                    redirectAttributes.addFlashAttribute("barbero", barbero);
                    return "redirect:/admin/barberos/nuevo";
                }

                if (confirmarPassword == null || !barbero.getPassword().equals(confirmarPassword)) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ Las contraseñas no coinciden");
                    redirectAttributes.addFlashAttribute("barbero", barbero);
                    return "redirect:/admin/barberos/nuevo";
                }

                if (barberoRepository.existsByEmail(barbero.getEmail())) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ Ya existe un barbero con el email: " + barbero.getEmail());
                    redirectAttributes.addFlashAttribute("barbero", barbero);
                    return "redirect:/admin/barberos/nuevo";
                }

                if (barbero.getDocumento() != null && !barbero.getDocumento().trim().isEmpty()) {
                    if (barberoRepository.existsByDocumento(barbero.getDocumento())) {
                        redirectAttributes.addFlashAttribute("error",
                                "❌ Ya existe un barbero con el documento: " + barbero.getDocumento());
                        redirectAttributes.addFlashAttribute("barbero", barbero);
                        return "redirect:/admin/barberos/nuevo";
                    }
                }

                barbero.setPassword(passwordEncoder.encode(barbero.getPassword()));

                if (barbero.getRol() == null || barbero.getRol().isEmpty()) {
                    barbero.setRol("ROLE_BARBERO");
                }

                barbero.setFechaIngreso(LocalDate.now());

                if (barbero.getNotifReservas() == null) barbero.setNotifReservas(true);
                if (barbero.getNotifCancelaciones() == null) barbero.setNotifCancelaciones(true);
                if (barbero.getNotifRecordatorios() == null) barbero.setNotifRecordatorios(true);
                if (barbero.getAutenticacionDosPasos() == null) barbero.setAutenticacionDosPasos(false);

            } else {
                // EDITANDO BARBERO EXISTENTE
                Barbero barberoExistente = barberoService.obtenerPorId(barbero.getIdBarbero());
                if (barberoExistente == null) {
                    redirectAttributes.addFlashAttribute("error", "❌ Barbero no encontrado");
                    return "redirect:/admin/barberos";
                }

                if (!barberoExistente.getEmail().equals(barbero.getEmail())) {
                    if (barberoRepository.existsByEmail(barbero.getEmail())) {
                        redirectAttributes.addFlashAttribute("error",
                                "❌ Ya existe otro barbero con el email: " + barbero.getEmail());
                        redirectAttributes.addFlashAttribute("barbero", barbero);
                        return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
                    }
                }

                if (barbero.getDocumento() != null && !barbero.getDocumento().trim().isEmpty()) {
                    if (!barbero.getDocumento().equals(barberoExistente.getDocumento())) {
                        if (barberoRepository.existsByDocumento(barbero.getDocumento())) {
                            redirectAttributes.addFlashAttribute("error",
                                    "❌ Ya existe otro barbero con el documento: " + barbero.getDocumento());
                            redirectAttributes.addFlashAttribute("barbero", barbero);
                            return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
                        }
                    }
                }

                if (barbero.getPassword() != null && !barbero.getPassword().isEmpty()) {
                    if (barbero.getPassword().length() < 6) {
                        redirectAttributes.addFlashAttribute("error",
                                "❌ La contraseña debe tener al menos 6 caracteres");
                        redirectAttributes.addFlashAttribute("barbero", barbero);
                        return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
                    }

                    if (confirmarPassword == null || !barbero.getPassword().equals(confirmarPassword)) {
                        redirectAttributes.addFlashAttribute("error", "❌ Las contraseñas no coinciden");
                        redirectAttributes.addFlashAttribute("barbero", barbero);
                        return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
                    }

                    barbero.setPassword(passwordEncoder.encode(barbero.getPassword()));
                } else {
                    barbero.setPassword(barberoExistente.getPassword());
                }

                barbero.setRol(barberoExistente.getRol());
                barbero.setFechaIngreso(barberoExistente.getFechaIngreso());
                barbero.setUltimaSesion(barberoExistente.getUltimaSesion());

                if (barbero.getFotoPerfil() == null || barbero.getFotoPerfil().isEmpty()) {
                    barbero.setFotoPerfil(barberoExistente.getFotoPerfil());
                }
            }

            // VALORES POR DEFECTO PARA HORARIOS
            if (barbero.getHoraInicio() == null) barbero.setHoraInicio(LocalTime.of(8, 0));
            if (barbero.getHoraFin() == null) barbero.setHoraFin(LocalTime.of(18, 0));
            if (barbero.getHoraInicioAlmuerzo() == null) barbero.setHoraInicioAlmuerzo(LocalTime.of(12, 0));
            if (barbero.getHoraFinAlmuerzo() == null) barbero.setHoraFinAlmuerzo(LocalTime.of(13, 0));
            if (barbero.getDuracionTurno() == null) barbero.setDuracionTurno(30);

            barberoService.guardar(barbero);

            String mensaje = (barbero.getIdBarbero() == null)
                    ? String.format("✅ Barbero '%s' creado exitosamente", barbero.getNombreCompleto())
                    : String.format("✅ Barbero '%s' actualizado exitosamente", barbero.getNombreCompleto());

            redirectAttributes.addFlashAttribute("mensaje", mensaje);

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            redirectAttributes.addFlashAttribute("barbero", barbero);

            if (barbero.getIdBarbero() == null) {
                return "redirect:/admin/barberos/nuevo";
            } else {
                return "redirect:/admin/barberos/editar/" + barbero.getIdBarbero();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();

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
                redirectAttributes.addFlashAttribute("error", "❌ Barbero no encontrado");
                return "redirect:/admin/barberos";
            }

            boolean nuevoEstado = !barbero.isActivo();

            if (nuevoEstado) {
                // REACTIVAR
                barberoService.reactivarBarbero(id);
                redirectAttributes.addFlashAttribute("mensaje",
                        "✅ Barbero reactivado exitosamente. Ahora puede iniciar sesión y recibir turnos.");
            } else {
                // DESVINCULAR NORMAL
                try {
                    barberoService.desvincularBarbero(id, "Desvinculado por el administrador");
                    redirectAttributes.addFlashAttribute("mensaje",
                            "✅ Barbero desvinculado exitosamente. No podrá iniciar sesión pero sus datos se mantienen. " +
                            "Puede reactivarlo usando el botón 'Reactivar'.");
                } catch (RuntimeException e) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ " + e.getMessage() + " Use la opción 'Desvinculación de Emergencia'.");
                    return "redirect:/admin/barberos";
                }
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
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
                redirectAttributes.addFlashAttribute("error", "❌ La contraseña debe tener al menos 6 caracteres");
                return "redirect:/admin/barberos";
            }

            Barbero barbero = barberoService.obtenerPorId(id);
            if (barbero != null) {
                barbero.setPassword(passwordEncoder.encode(nuevaPassword));
                barberoService.guardar(barbero);
                redirectAttributes.addFlashAttribute("mensaje",
                        "✅ Contraseña actualizada exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "❌ Barbero no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }
        return "redirect:/admin/barberos";
    }

    // ==================== DESVINCULACIÓN DE EMERGENCIA ====================
    
    @GetMapping("/desvincular-emergencia/{id}")
    @Transactional(readOnly = true)
    public String mostrarFormularioEmergencia(@PathVariable Long id, Model model) {
        Barbero barbero = barberoService.obtenerPorId(id);
        if (barbero == null) {
            return "redirect:/admin/barberos";
        }
        
        // Obtener reservas futuras
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = barberoService.obtenerReservasPorRangoFechas(
            id, ahora, ahora.plusMonths(3)
        );
        
        long reservasActivas = reservasFuturas.stream()
            .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
            .count();
        
        long reservasHoy = reservasFuturas.stream()
            .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
            .filter(r -> r.getFechaHoraTurno().toLocalDate().equals(LocalDate.now()))
            .count();
        
        long reservasEstaSemana = reservasFuturas.stream()
            .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
            .filter(r -> {
                LocalDate fecha = r.getFechaHoraTurno().toLocalDate();
                LocalDate hoy = LocalDate.now();
                return !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(7));
            })
            .count();
        
        // Barberos disponibles para reasignar
        List<Barbero> barberosDisponibles = barberoService.obtenerBarberosActivos()
            .stream()
            .filter(b -> !b.getIdBarbero().equals(id))
            .collect(java.util.stream.Collectors.toList());
        
        model.addAttribute("barbero", barbero);
        model.addAttribute("reservasActivas", reservasActivas);
        model.addAttribute("reservasHoy", reservasHoy);
        model.addAttribute("reservasEstaSemana", reservasEstaSemana);
        model.addAttribute("barberosDisponibles", barberosDisponibles);
        model.addAttribute("reservasFuturas", reservasFuturas.stream()
            .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
            .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
            .collect(java.util.stream.Collectors.toList()));
        
        return "admin/barberos/desvincular-emergencia";
    }
    
    @PostMapping("/desvincular-emergencia/{id}")
    @Transactional
    public String procesarDesvinculacionEmergencia(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestParam String accionReservas,
            @RequestParam(required = false) Long barberoSustitutoId,
            @RequestParam(required = false) String mensajeClientes,
            RedirectAttributes redirectAttributes) {
        
        try {
            Barbero barbero = barberoService.obtenerPorId(id);
            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Barbero no encontrado");
                return "redirect:/admin/barberos";
            }
            
            if (motivo == null || motivo.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "❌ Debe especificar el motivo");
                return "redirect:/admin/barberos/desvincular-emergencia/" + id;
            }
            
            int reservasGestionadas = 0;
            
            switch (accionReservas) {
                case "cancelar":
                    reservasGestionadas = barberoService.desvincularBarberoEmergencia(
                        id, motivo, mensajeClientes
                    );
                    break;
                    
                case "reasignar":
                    if (barberoSustitutoId == null) {
                        redirectAttributes.addFlashAttribute("error", 
                            "❌ Debe seleccionar un barbero sustituto");
                        return "redirect:/admin/barberos/desvincular-emergencia/" + id;
                    }
                    
                    Barbero sustituto = barberoService.obtenerPorId(barberoSustitutoId);
                    if (sustituto == null || !sustituto.isActivo()) {
                        redirectAttributes.addFlashAttribute("error", 
                            "❌ El barbero sustituto no está disponible");
                        return "redirect:/admin/barberos/desvincular-emergencia/" + id;
                    }
                    
                    reservasGestionadas = barberoService.reasignarYDesvincularBarbero(
                        id, barberoSustitutoId, motivo, mensajeClientes
                    );
                    break;
                    
                case "mantener":
                    barberoService.desvincularBarbero(id, motivo);
                    reservasGestionadas = 0;
                    break;
                    
                default:
                    redirectAttributes.addFlashAttribute("error", 
                        "❌ Acción no válida");
                    return "redirect:/admin/barberos/desvincular-emergencia/" + id;
            }
            
            String mensaje = String.format(
                "✅ Barbero '%s' desvinculado inmediatamente<br>" +
                "📋 Motivo: %s<br>" +
                "📊 Acción: %s<br>" +
                "✉️ Clientes notificados: %d",
                barbero.getNombreCompleto(),
                motivo,
                accionReservas.equals("cancelar") ? "Reservas canceladas" :
                accionReservas.equals("reasignar") ? "Reservas reasignadas" : "Reservas mantenidas",
                reservasGestionadas
            );
            
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "❌ Error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/barberos/desvincular-emergencia/" + id;
        }
        
        return "redirect:/admin/barberos";
    }

    // ==================== GESTIONAR TURNOS ====================
    
    @GetMapping("/{id}/turnos")
    @Transactional(readOnly = true)
    public String gestionarTurnos(@PathVariable Long id, Model model) {
        Barbero barbero = barberoService.obtenerPorId(id);
        if (barbero == null) {
            return "redirect:/admin/barberos";
        }

        model.addAttribute("barbero", barbero);

        List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorBarbero(id);
        List<Turno> turnosReservados = turnoService.obtenerTurnosNoDisponiblesPorBarbero(id);

        model.addAttribute("turnosDisponibles", turnosDisponibles);
        model.addAttribute("turnosReservados", turnosReservados);
        model.addAttribute("fechaInicio", LocalDate.now());
        model.addAttribute("fechaFin", LocalDate.now().plusDays(14));

        return "admin/barberos/turnos";
    }

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
                if (!barbero.tieneHorarioCompleto()) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ El barbero debe tener horario configurado");
                    return "redirect:/admin/barberos/" + id + "/turnos";
                }

                if (fechaFin.isBefore(fechaInicio)) {
                    redirectAttributes.addFlashAttribute("error",
                            "❌ La fecha fin debe ser posterior a la fecha inicio");
                    return "redirect:/admin/barberos/" + id + "/turnos";
                }

                List<Turno> turnosGenerados = turnoService.generarTurnosDisponibles(barbero, fechaInicio, fechaFin);

                redirectAttributes.addFlashAttribute("mensaje",
                        "✅ Se generaron " + turnosGenerados.size() + " turnos");
            } else {
                redirectAttributes.addFlashAttribute("error", "❌ Barbero no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/barberos/" + id + "/turnos";
    }

    // ==================== ELIMINAR PERMANENTEMENTE ====================
    
    @PostMapping("/eliminar-permanente/{id}")
    @Transactional
    public String eliminarPermanentemente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Barbero barbero = barberoService.obtenerPorId(id);

            if (barbero == null) {
                redirectAttributes.addFlashAttribute("error", "❌ Barbero no encontrado");
                return "redirect:/admin/barberos";
            }

            if (barbero.isActivo()) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ No se puede eliminar un barbero activo. Primero desvincúlelo.");
                return "redirect:/admin/barberos";
            }

            barberoService.eliminar(id);

            redirectAttributes.addFlashAttribute("mensaje",
                    "✅ Barbero '" + barbero.getNombreCompleto() + "' eliminado permanentemente");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/barberos";
    }
}