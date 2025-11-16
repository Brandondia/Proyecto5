package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.Barbero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Barbería Estilo}")
    private String appName;

    /**
     * Envía un email simple de texto plano
     */
    public void enviarEmailSimple(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(destinatario);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);

            mailSender.send(mailMessage);
            System.out.println("✅ Email enviado a: " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía un email HTML con formato usando un template de Thymeleaf
     */
    private void enviarEmailConTemplate(String destinatario, String asunto, String templateName, Context context) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            // Procesar el template de Thymeleaf
            String contenidoHTML = templateEngine.process(templateName, context);
            helper.setText(contenidoHTML, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Email HTML enviado a: " + destinatario);
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email HTML a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notifica al cliente sobre la cancelación de su reserva por ausencia del barbero
     */
    public void notificarCancelacionPorAusencia(Cliente cliente, Reserva reserva, Barbero barbero, String motivo) {
        String asunto = "⚠️ Tu reserva ha sido cancelada - " + appName;
        
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("clienteNombre", cliente.getNombre());
        context.setVariable("fecha", reserva.getFechaHoraTurno().format(formatoFecha));
        context.setVariable("hora", reserva.getFechaHoraTurno().format(formatoHora));
        context.setVariable("servicio", reserva.getCorte().getNombre());
        context.setVariable("barberoNombre", barbero.getNombre());
        context.setVariable("barberoApellido", barbero.getApellido());
        context.setVariable("motivo", motivo);
        
        enviarEmailConTemplate(cliente.getCorreo(), asunto, "email/cancelacion-ausencia", context);
    }

    /**
     * Notifica al cliente sobre la confirmación de su reserva
     */
    public void notificarConfirmacionReserva(Cliente cliente, Reserva reserva) {
        String asunto = "✅ Confirmación de Reserva - " + appName;
        
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("clienteNombre", cliente.getNombre());
        context.setVariable("fecha", reserva.getFechaHoraTurno().format(formatoFecha));
        context.setVariable("hora", reserva.getFechaHoraTurno().format(formatoHora));
        context.setVariable("servicio", reserva.getCorte().getNombre());
        context.setVariable("duracion", reserva.getCorte().getDuracion());
        context.setVariable("barberoNombre", reserva.getBarbero().getNombre());
        context.setVariable("barberoApellido", reserva.getBarbero().getApellido());
        context.setVariable("precio", reserva.getCorte().getPrecio());
        context.setVariable("urlMisReservas", "http://localhost:8586/reserva/mis-reservas");
        context.setVariable("urlCancelar", "http://localhost:8586/reserva/cancelar/" + reserva.getIdReserva());
        
        enviarEmailConTemplate(cliente.getCorreo(), asunto, "email/confirmacion", context);
    }

    /**
     * Notifica al cliente sobre la cancelación de su reserva (cancelada por el cliente)
     */
    public void notificarCancelacionReserva(Cliente cliente, Reserva reserva) {
        String asunto = "Confirmación de Cancelación - " + appName;
        
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("clienteNombre", cliente.getNombre());
        context.setVariable("fecha", reserva.getFechaHoraTurno().format(formatoFecha));
        context.setVariable("hora", reserva.getFechaHoraTurno().format(formatoHora));
        context.setVariable("servicio", reserva.getCorte().getNombre());
        context.setVariable("barberoNombre", reserva.getBarbero().getNombre());
        context.setVariable("barberoApellido", reserva.getBarbero().getApellido());
        
        enviarEmailConTemplate(cliente.getCorreo(), asunto, "email/cancelacion", context);
    }

    /**
     * Envía un recordatorio de cita al cliente
     */
    public void enviarRecordatorioCita(Cliente cliente, Reserva reserva) {
        String asunto = "🔔 Recordatorio de Cita - " + appName;
        
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        
        Context context = new Context();
        context.setVariable("appName", appName);
        context.setVariable("clienteNombre", cliente.getNombre());
        context.setVariable("fecha", reserva.getFechaHoraTurno().format(formatoFecha));
        context.setVariable("hora", reserva.getFechaHoraTurno().format(formatoHora));
        context.setVariable("servicio", reserva.getCorte().getNombre());
        context.setVariable("barberoNombre", reserva.getBarbero().getNombre());
        context.setVariable("barberoApellido", reserva.getBarbero().getApellido());
        
        enviarEmailConTemplate(cliente.getCorreo(), asunto, "email/recordatorio", context);
    }
}