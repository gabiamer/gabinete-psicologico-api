// src/main/java/com/gabinete/psicologico_api/repository/EstudianteCarreraRepository.java
package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.EstudianteCarrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteCarreraRepository extends JpaRepository<EstudianteCarrera, Long> {
    Optional<EstudianteCarrera> findByPacienteUniversitarioId(Long pacienteUniversitarioId);
}