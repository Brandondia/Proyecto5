package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Barbero;
import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Servicio para envío de correos electrónicos
 * Maneja todas las notificaciones a clientes y barberos
 * 
 * @author Tu Nombre
 * @version 2.0 - Agregadas notificaciones para desvinculación
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.nombre:Barbería Estilo}")
    private String nombreBarberia;

    // ==================== FORMATEADORES ====================
    
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_COMPLETO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    // ==================== MÉTODOS BÁSICOS ====================
    
    /**
     * Envía un correo simple con texto plano
     * 
     * @param destinatario email del destinatario
     * @param asunto asunto del correo
     * @param mensaje cuerpo del mensaje
     * @throws RuntimeException si hay error al enviar
     */
    public void enviarCorreoSimple(String destinatario, String asunto, String mensaje) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, false); // false = texto plano
            
            mailSender.send(mimeMessage);
            
            System.out.println("✅ Email simple enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email simple: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo: " + e.getMessage());
        }
    }
    
    /**
     * Envía un correo con formato HTML
     * 
     * @param destinatario email del destinatario
     * @param asunto asunto del correo
     * @param mensajeHtml cuerpo del mensaje en HTML
     * @throws RuntimeException si hay error al enviar
     */
    public void enviarCorreoHTML(String destinatario, String asunto, String mensajeHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensajeHtml, true); // true = HTML
            
            mailSender.send(mimeMessage);
            
            System.out.println("✅ Email HTML enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email HTML: " + e.getMessage());
            throw new RuntimeException("Error al enviar correo: " + e.getMessage());
        }
    }

    // ==================== NOTIFICACIONES DE RESERVA ====================
    
    /**
     * Notifica al cliente sobre cancelación de reserva por ausencia del barbero
     */
    public void notificarCancelacionPorAusencia(Cliente cliente, Reserva reserva, Barbero barbero, String motivo) {
        try {
            String asunto = "Cancelación de tu Reserva - " + nombreBarberia;
            
            String mensaje = construirMensajeCancelacionAusencia(cliente, reserva, barbero, motivo);
            
            enviarCorreoHTML(cliente.getCorreo(), asunto, mensaje);
            
        } catch (Exception e) {
            System.err.println("❌ Error al notificar cancelación por ausencia: " + e.getMessage());
        }
    }

    // ==================== NOTIFICACIONES DE DESVINCULACIÓN ====================
    
    /**
     * Notifica al cliente que su reserva fue cancelada por desvinculación del barbero
     * 
     * @param cliente cliente a notificar
     * @param reserva reserva cancelada
     * @param barbero barbero desvinculado
     * @param motivo motivo de la desvinculación
     */
    public void notificarCancelacionPorDesvinculacion(Cliente cliente, Reserva reserva, Barbero barbero, String motivo) {
        try {
            String asunto = "⚠️ Cambio en tu Reserva - " + nombreBarberia;
            
            String mensaje = construirMensajeCancelacionDesvinculacion(cliente, reserva, barbero);
            
            enviarCorreoHTML(cliente.getCorreo(), asunto, mensaje);
            
            System.out.println("✅ Notificación de cancelación enviada a: " + cliente.getCorreo());
            
        } catch (Exception e) {
            System.err.println("❌ Error al notificar cancelación por desvinculación: " + e.getMessage());
        }
    }
    
    /**
     * Notifica al cliente que su reserva fue reasignada a otro barbero
     * 
     * @param cliente cliente a notificar
     * @param reserva reserva reasignada
     * @param barberoOriginal barbero original (desvinculado)
     * @param barberoNuevo nuevo barbero asignado
     */
    public void notificarReasignacionReserva(Cliente cliente, Reserva reserva, Barbero barberoOriginal, Barbero barberoNuevo) {
        try {
            String asunto = "🔄 Cambio en tu Reserva - " + nombreBarberia;
            
            String mensaje = construirMensajeReasignacion(cliente, reserva, barberoOriginal, barberoNuevo);
            
            enviarCorreoHTML(cliente.getCorreo(), asunto, mensaje);
            
            System.out.println("✅ Notificación de reasignación enviada a: " + cliente.getCorreo());
            
        } catch (Exception e) {
            System.err.println("❌ Error al notificar reasignación: " + e.getMessage());
        }
    }

    // ==================== CONSTRUCTORES DE MENSAJES HTML ====================
    
    /**
     * Construye el mensaje HTML para cancelación por ausencia
     */
    private String construirMensajeCancelacionAusencia(Cliente cliente, Reserva reserva, Barbero barbero, String motivo) {
        String fecha = reserva.getFechaHoraTurno().format(FORMATO_FECHA);
        String hora = reserva.getFechaHoraTurno().format(FORMATO_HORA);
        String nombreServicio = reserva.getCorte() != null ? reserva.getCorte().getNombre() : "Servicio";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 5px 5px; }
                    .info-box { background: white; padding: 15px; margin: 20px 0; border-left: 4px solid #dc3545; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 30px; background: #28a745; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>⚠️ Cambio en tu Reserva</h2>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        
                        <p>Lamentamos informarte que tu reserva ha sido <strong>cancelada</strong> debido a que el barbero no estará disponible.</p>
                        
                        <div class="info-box">
                            <h3>Detalles de la Reserva Cancelada:</h3>
                            <p>📅 <strong>Fecha:</strong> %s</p>
                            <p>🕐 <strong>Hora:</strong> %s</p>
                            <p>💈 <strong>Barbero:</strong> %s</p>
                            <p>✂️ <strong>Servicio:</strong> %s</p>
                        </div>
                        
                        <p><strong>¿Qué puedes hacer?</strong></p>
                        <ul>
                            <li>Reagendar tu cita con otro barbero disponible</li>
                            <li>Contactarnos para que te ayudemos a encontrar otro horario</li>
                        </ul>
                        
                        <div style="text-align: center;">
                            <a href="#" class="button">Agendar Nueva Cita</a>
                        </div>
                        
                        <p>Disculpa los inconvenientes causados.</p>
                        
                        <p>Saludos,<br><strong>%s</strong></p>
                    </div>
                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            cliente.getNombre(),
            fecha,
            hora,
            barbero.getNombreCompleto(),
            nombreServicio,
            nombreBarberia
        );
    }
    
    /**
     * Construye el mensaje HTML para cancelación por desvinculación
     */
    private String construirMensajeCancelacionDesvinculacion(Cliente cliente, Reserva reserva, Barbero barbero) {
        String fecha = reserva.getFechaHoraTurno().format(FORMATO_FECHA);
        String hora = reserva.getFechaHoraTurno().format(FORMATO_HORA);
        String nombreServicio = reserva.getCorte() != null ? reserva.getCorte().getNombre() : "Servicio";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #ffc107; color: #333; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 5px 5px; }
                    .info-box { background: white; padding: 15px; margin: 20px 0; border-left: 4px solid #ffc107; }
                    .alert-box { background: #fff3cd; padding: 15px; margin: 20px 0; border-left: 4px solid #ffc107; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 30px; background: #28a745; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>⚠️ Aviso Importante sobre tu Reserva</h2>
                    </div>
                    <div class="content">
                        <p>Estimado/a <strong>%s</strong>,</p>
                        
                        <div class="alert-box">
                            <p><strong>⚠️ Aviso:</strong> El barbero asignado a tu reserva ya no forma parte de nuestro equipo, por lo que tu cita ha sido <strong>cancelada</strong>.</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>Detalles de la Reserva Cancelada:</h3>
                            <p>📅 <strong>Fecha:</strong> %s</p>
                            <p>🕐 <strong>Hora:</strong> %s</p>
                            <p>💈 <strong>Barbero Original:</strong> %s</p>
                            <p>✂️ <strong>Servicio:</strong> %s</p>
                        </div>
                        
                        <h3>¿Qué puedes hacer ahora?</h3>
                        <p>Te invitamos a agendar una nueva cita con cualquiera de nuestros otros excelentes barberos disponibles.</p>
                        
                        <div style="text-align: center;">
                            <a href="#" class="button">Ver Barberos Disponibles</a>
                        </div>
                        
                        <p>Agradecemos tu comprensión y esperamos verte pronto.</p>
                        
                        <p>Cordialmente,<br><strong>Equipo de %s</strong></p>
                    </div>
                    <div class="footer">
                        <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            cliente.getNombre(),
            fecha,
            hora,
            barbero.getNombreCompleto(),
            nombreServicio,
            nombreBarberia
        );
    }
    
    /**
     * Construye el mensaje HTML para reasignación de reserva
     */
    private String construirMensajeReasignacion(Cliente cliente, Reserva reserva, Barbero barberoOriginal, Barbero barberoNuevo) {
        String fecha = reserva.getFechaHoraTurno().format(FORMATO_FECHA);
        String hora = reserva.getFechaHoraTurno().format(FORMATO_HORA);
        String nombreServicio = reserva.getCorte() != null ? reserva.getCorte().getNombre() : "Servicio";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #0d6efd; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 5px 5px; }
                    .info-box { background: white; padding: 15px; margin: 20px 0; border-left: 4px solid #0d6efd; }
                    .change-box { background: #e7f1ff; padding: 15px; margin: 20px 0; border-left: 4px solid #0d6efd; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .highlight { background: #fff3cd; padding: 2px 8px; border-radius: 3px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🔄 Cambio en tu Reserva</h2>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        
                        <p>Te informamos que hemos realizado un cambio en tu reserva programada.</p>
                        
                        <div class="change-box">
                            <h3>📋 ¿Qué cambió?</h3>
                            <p>Tu barbero original <strong>%s</strong> ya no está disponible.</p>
                            <p>Hemos reasignado tu cita a <strong class="highlight">%s</strong>.</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>Detalles de tu Reserva:</h3>
                            <p>📅 <strong>Fecha:</strong> %s <em>(sin cambios)</em></p>
                            <p>🕐 <strong>Hora:</strong> %s <em>(sin cambios)</em></p>
                            <p>💈 <strong>Nuevo Barbero:</strong> <span class="highlight">%s</span></p>
                            <p>✂️ <strong>Servicio:</strong> %s <em>(sin cambios)</em></p>
                        </div>
                        
                        <p><strong>✅ No necesitas hacer nada:</strong> Tu cita se mantiene en la misma fecha y hora. Solo cambia el barbero que te atenderá.</p>
                        
                        <p>Si tienes alguna pregunta o prefieres reagendar, contáctanos.</p>
                        
                        <p>¡Te esperamos!</p>
                        
                        <p>Saludos,<br><strong>%s</strong></p>
                    </div>
                    <div class="footer">
                        <p>Si deseas cancelar o modificar tu cita, contáctanos lo antes posible.</p>
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            cliente.getNombre(),
            barberoOriginal.getNombreCompleto(),
            barberoNuevo.getNombreCompleto(),
            fecha,
            hora,
            barberoNuevo.getNombreCompleto(),
            nombreServicio,
            nombreBarberia
        );
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Valida que el email sea válido
     */
    private boolean esEmailValido(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public void notificarConfirmacionReserva(Cliente cliente, Reserva reserva) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarConfirmacionReserva'");
    }

    public void notificarCancelacionReserva(Cliente cliente, Reserva reservaCancelada) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notificarCancelacionReserva'");
    }

    public void enviarRecordatorioCita(Cliente cliente, Reserva r) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'enviarRecordatorioCita'");
    }
}