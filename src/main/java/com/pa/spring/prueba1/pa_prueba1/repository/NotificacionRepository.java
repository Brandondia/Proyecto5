package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByBarberoIdBarberoOrderByFechaCreacionDesc(Long idBarbero);
    
    List<Notificacion> findByBarberoIdBarberoAndLeidaOrderByFechaCreacionDesc(Long idBarbero, Boolean leida);
    
    List<Notificacion> findByBarberoIdBarberoAndTipoOrderByFechaCreacionDesc(Long idBarbero, Notificacion.TipoNotificacion tipo);
    
    long countByBarberoIdBarberoAndLeida(Long idBarbero, Boolean leida);
}