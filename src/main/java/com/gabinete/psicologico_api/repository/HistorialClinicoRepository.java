package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
    Optional<HistorialClinico> findBySesionPacienteId(Long sesionPacienteId);
    List<HistorialClinico> findBySesionPaciente_PacienteUniversitarioId(Long pacienteUniversitarioId);
}
