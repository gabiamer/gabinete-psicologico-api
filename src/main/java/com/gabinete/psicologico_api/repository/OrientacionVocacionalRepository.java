package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.OrientacionVocacional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrientacionVocacionalRepository extends JpaRepository<OrientacionVocacional, Long> {
    
    List<OrientacionVocacional> findByPacienteExternoId(Long pacienteExternoId);
    
    Optional<OrientacionVocacional> findTopByPacienteExternoIdOrderByFechaDesc(Long pacienteExternoId);
}