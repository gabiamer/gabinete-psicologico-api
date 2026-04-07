package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.HorasDesignadasAp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorasDesignadasApRepository extends JpaRepository<HorasDesignadasAp, Long> {
    List<HorasDesignadasAp> findByAnio(Integer anio);
    Optional<HorasDesignadasAp> findByPsicologo_IdAndAnioAndMesAndTurno(Long psicologoId, Integer anio, Integer mes, String turno);
}
