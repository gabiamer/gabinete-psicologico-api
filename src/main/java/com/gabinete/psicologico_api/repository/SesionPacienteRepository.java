package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.SesionPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesionPacienteRepository extends JpaRepository<SesionPaciente, Long> {
}