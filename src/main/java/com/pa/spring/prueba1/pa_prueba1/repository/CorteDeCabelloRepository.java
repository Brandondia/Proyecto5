package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.CorteDeCabello;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository // Indica que esta interfaz es un componente de acceso a datos y debe ser gestionada por Spring.
public interface CorteDeCabelloRepository extends JpaRepository<CorteDeCabello, Long> {
    // Busca solo los servicios activos
    List<CorteDeCabello> findByActivoTrue();
}

