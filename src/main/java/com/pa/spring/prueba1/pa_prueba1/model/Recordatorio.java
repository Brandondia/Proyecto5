package com.pa.spring.prueba1.pa_prueba1.model;

import com.pa.spring.prueba1.pa_prueba1.model.Reserva.EstadoReserva;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class Recordatorio {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private EmailService emailService;


    // ============================================================
    // 📌 1. RECORDATORIO 24 HORAS ANTES
    // ============================================================
    @Scheduled(cron = "0 0 * * * *") // se ejecuta cada hora
    public void enviarRecordatorios24HorasAntes() {

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en24Horas = ahora.plusHours(24);

        List<Reserva> reservas =
                reservaRepository.findByFechaHoraTurnoBetween(ahora, en24Horas)
                        .stream()
                        .filter(r -> r.getEstado() == EstadoReserva.COMPLETADA)
                        .toList();

        for (Reserva r : reservas) {
            try {
                emailService.enviarRecordatorioCita(r.getCliente(), r);
                System.out.println("📧 Recordatorio 24h enviado a: " + r.getCliente().getCorreo());
            } catch (Exception e) {
                System.err.println("❌ Error enviando recordatorio 24h: " + e.getMessage());
            }
        }
    }


    // ============================================================
    // 📌 2. RECORDATORIO 2 HORAS ANTES
    // ============================================================
    @Scheduled(cron = "0 30 * * * *") // cada hora en el minuto 30
    public void enviarRecordatorios2HorasAntes() {

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime en2Horas = ahora.plusHours(2);

        List<Reserva> reservas =
                reservaRepository.findByFechaHoraTurnoBetween(ahora, en2Horas)
                        .stream()
                        .filter(r -> r.getEstado() == EstadoReserva.COMPLETADA)
                        .toList();

        for (Reserva r : reservas) {
            try {
                emailService.enviarRecordatorioCita(r.getCliente(), r);
                System.out.println("📧 Recordatorio 2h enviado a: " + r.getCliente().getCorreo());
            } catch (Exception e) {
                System.err.println("❌ Error enviando recordatorio 2h: " + e.getMessage());
            }
        }
    }
}
