package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.ActividadPsicologo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActividadPsicologoRepository extends JpaRepository<ActividadPsicologo, Long> {
    List<ActividadPsicologo> findByPsicologoId(Long psicologoId);
    List<ActividadPsicologo> findByFechaInicioBetween(LocalDateTime desde, LocalDateTime hasta);
    List<ActividadPsicologo> findByFechaInicioGreaterThanEqualAndFechaFinLessThanEqual(LocalDateTime desde, LocalDateTime hasta);
    List<ActividadPsicologo> findAllByOrderByFechaInicioDesc();
    List<ActividadPsicologo> findByPsicologoIdOrderByFechaInicioDesc(Long psicologoId);
    List<ActividadPsicologo> findByPsicologoIdAndFechaInicioGreaterThanEqualAndFechaFinLessThanEqual(
            Long psicologoId, LocalDateTime desde, LocalDateTime hasta);
}
