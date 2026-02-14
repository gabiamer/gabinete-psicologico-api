//src/main/java/com/gabinete/psicologico_api/repository/PsicologoRepository.java

package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.Psicologo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, Long> {
}