package com.gabinete.psicologico_api.repository;

import com.gabinete.psicologico_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByPsicologoId(Long psicologoId);
    boolean existsByPsicologoIdAndIdNot(Long psicologoId, Long id);
}
