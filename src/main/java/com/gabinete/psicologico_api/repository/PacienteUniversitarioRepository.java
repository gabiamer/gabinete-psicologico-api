//src/main/java/com/gabinete/psicologico_api/repository/PacienteUniversitarioRepository.java
package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.PacienteUniversitario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    
    /**
     * Busca pacientes que tuvieron sesiones en una fecha específica
     */
    @Query("SELECT DISTINCT pu FROM PacienteUniversitario pu " +
           "LEFT JOIN SesionPaciente s ON s.pacienteUniversitario.id = pu.id " +
           "WHERE CAST(s.fecha AS date) = :fecha")
    List<PacienteUniversitario> buscarPorFechaSesion(@Param("fecha") LocalDate fecha);
    
    /**
     * Busca pacientes por término Y que tuvieron sesiones en una fecha específica
     */
    @Query("SELECT DISTINCT pu FROM PacienteUniversitario pu " +
           "JOIN pu.paciente p " +
           "JOIN p.person per " +
           "LEFT JOIN SesionPaciente s ON s.pacienteUniversitario.id = pu.id " +
           "WHERE (LOWER(per.primerNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.segundoNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoMaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR per.celular LIKE CONCAT('%', :termino, '%')) " +
           "AND CAST(s.fecha AS date) = :fecha")
    List<PacienteUniversitario> buscarPorTerminoYFecha(
            @Param("termino") String termino,
            @Param("fecha") LocalDate fecha);

    List<PacienteUniversitario> findByPsicologoId(Long psicologoId);

    @Query("SELECT pu FROM PacienteUniversitario pu " +
           "JOIN pu.paciente p " +
           "JOIN p.person per " +
           "WHERE pu.psicologo.id = :psicologoId " +
           "AND (LOWER(per.primerNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.segundoNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoMaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR per.celular LIKE CONCAT('%', :termino, '%'))")
    List<PacienteUniversitario> buscarPorTerminoYPsicologo(
            @Param("termino") String termino,
            @Param("psicologoId") Long psicologoId);

    @Query("SELECT DISTINCT pu FROM PacienteUniversitario pu " +
           "LEFT JOIN SesionPaciente s ON s.pacienteUniversitario.id = pu.id " +
           "WHERE pu.psicologo.id = :psicologoId " +
           "AND CAST(s.fecha AS date) = :fecha")
    List<PacienteUniversitario> buscarPorFechaSesionYPsicologo(
            @Param("fecha") LocalDate fecha,
            @Param("psicologoId") Long psicologoId);

    @Query("SELECT DISTINCT pu FROM PacienteUniversitario pu " +
           "JOIN pu.paciente p " +
           "JOIN p.person per " +
           "LEFT JOIN SesionPaciente s ON s.pacienteUniversitario.id = pu.id " +
           "WHERE pu.psicologo.id = :psicologoId " +
           "AND (LOWER(per.primerNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.segundoNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoMaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR per.celular LIKE CONCAT('%', :termino, '%')) " +
           "AND CAST(s.fecha AS date) = :fecha")
    List<PacienteUniversitario> buscarPorTerminoYFechaYPsicologo(
            @Param("termino") String termino,
            @Param("fecha") LocalDate fecha,
            @Param("psicologoId") Long psicologoId);
}