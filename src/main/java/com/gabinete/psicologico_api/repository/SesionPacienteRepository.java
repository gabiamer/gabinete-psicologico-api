// src/main/java/com/gabinete/psicologico_api/repository/SesionPacienteRepository.java
package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.SesionPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionPacienteRepository extends JpaRepository<SesionPaciente, Long> {
    
    // Buscar sesiones por paciente universitario
    List<SesionPaciente> findByPacienteUniversitarioId(Long pacienteUniversitarioId);
}