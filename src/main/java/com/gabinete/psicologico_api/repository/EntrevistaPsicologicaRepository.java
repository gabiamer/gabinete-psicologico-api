package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.EntrevistaPsicologica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntrevistaPsicologicaRepository extends JpaRepository<EntrevistaPsicologica, Long> {
    Optional<EntrevistaPsicologica> findBySesionPacienteId(Long sesionPacienteId);
    List<EntrevistaPsicologica> findBySesionPaciente_PacienteUniversitarioId(Long pacienteUniversitarioId);
}