package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.EntrevistaPsicologica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntrevistaPsicologicaRepository extends JpaRepository<EntrevistaPsicologica, Long> {
}