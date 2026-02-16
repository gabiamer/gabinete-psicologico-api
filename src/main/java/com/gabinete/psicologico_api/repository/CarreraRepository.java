// src/main/java/com/gabinete/psicologico_api/repository/CarreraRepository.java
package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Long> {
}