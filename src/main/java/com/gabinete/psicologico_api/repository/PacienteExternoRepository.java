// src/main/java/com/gabinete/psicologico_api/repository/PacienteExternoRepository.java
package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.PacienteExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteExternoRepository extends JpaRepository<PacienteExterno, Long> {
    
    @Query("SELECT pe FROM PacienteExterno pe " +
           "JOIN pe.paciente p " +
           "JOIN p.person per " +
           "WHERE LOWER(per.primerNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.segundoNombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoPaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(per.apellidoMaterno) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR per.celular LIKE CONCAT('%', :termino, '%') " +
           "OR pe.correo LIKE CONCAT('%', :termino, '%')")
    List<PacienteExterno> buscarPorTermino(@Param("termino") String termino);
}