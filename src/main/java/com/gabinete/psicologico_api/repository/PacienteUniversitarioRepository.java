package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.PacienteUniversitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteUniversitarioRepository extends JpaRepository<PacienteUniversitario, Long> {
    
    @Query("SELECT pu FROM PacienteUniversitario pu " +
           "JOIN pu.paciente p " +
           "JOIN p.person per " +
           "WHERE LOWER(per.primerNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.segundoNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoMaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR per.celular LIKE CONCAT('%', :termino, '%')")
    List<PacienteUniversitario> buscarPorTermino(@Param("termino") String termino);
}