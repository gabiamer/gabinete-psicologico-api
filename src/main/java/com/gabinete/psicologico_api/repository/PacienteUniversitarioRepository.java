package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.PacienteUniversitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteUniversitarioRepository extends JpaRepository<PacienteUniversitario, Long> {
}