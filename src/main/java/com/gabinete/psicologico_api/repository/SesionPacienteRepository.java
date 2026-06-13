package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.SesionPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionPacienteRepository extends JpaRepository<SesionPaciente, Long> {

    List<SesionPaciente> findByPacienteUniversitarioId(Long pacienteUniversitarioId);

    List<SesionPaciente> findByPacienteUniversitarioIdAndFechaBetweenOrderByFechaAsc(Long pacienteUniversitarioId, LocalDateTime inicio, LocalDateTime fin);

    List<SesionPaciente> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<SesionPaciente> findByFechaBetweenAndPsicologoId(LocalDateTime inicio, LocalDateTime fin, Long psicologoId);

    Optional<SesionPaciente> findTopByPacienteUniversitarioIdOrderByFechaDesc(Long pacienteUniversitarioId);

    List<SesionPaciente> findByPacienteUniversitarioPsicologoId(Long psicologoId);
}
