package com.pa.spring.prueba1.pa_prueba1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.reserva")
public class ReservaConfigProperties {
    
    /**
     * Número máximo de reservas pendientes que puede tener un cliente simultáneamente
     */
    private int maxPendientes = 3;
    
    /**
     * Número máximo de reservas que puede hacer un cliente por día
     */
    private int maxPorDia = 2;
    
    /**
     * Tiempo mínimo en horas que debe pasar entre reservas
     */
    private int cooldownHoras = 0;

    // Getters y Setters
    public int getMaxPendientes() {
        return maxPendientes;
    }

    public void setMaxPendientes(int maxPendientes) {
        this.maxPendientes = maxPendientes;
    }

    public int getMaxPorDia() {
        return maxPorDia;
    }

    public void setMaxPorDia(int maxPorDia) {
        this.maxPorDia = maxPorDia;
    }

    public int getCooldownHoras() {
        return cooldownHoras;
    }

    public void setCooldownHoras(int cooldownHoras) {
        this.cooldownHoras = cooldownHoras;
    }
}