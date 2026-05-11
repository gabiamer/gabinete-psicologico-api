package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.model.Usuario;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import com.gabinete.psicologico_api.repository.UsuarioRepository;
import com.gabinete.psicologico_api.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PsicologoRepository psicologoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             PsicologoRepository psicologoRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.psicologoRepository = psicologoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        if (!SecurityUtils.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<Usuario> usuarios = usuarioRepository.findAll();
        // No exponer passwords
        List<Map<String, Object>> data = usuarios.stream().map(this::toMap).toList();
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        if (!SecurityUtils.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            String username = (String) body.get("username");
            String password = (String) body.get("password");
            String rol      = (String) body.getOrDefault("rol", "PSICOLOGO");

            if (username == null || username.isBlank())
                return error("El nombre de usuario es obligatorio");
            if (password == null || password.isBlank() || password.length() < 6)
                return error("La contraseña debe tener al menos 6 caracteres");
            if (usuarioRepository.existsByUsername(username))
                return error("El nombre de usuario '" + username + "' ya está en uso");

            Usuario usuario = new Usuario();
            usuario.setUsername(username.trim());
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setRol(rol);
            usuario.setActivo(true);

            if (body.containsKey("psicologoId") && body.get("psicologoId") != null) {
                Long psicologoId = ((Number) body.get("psicologoId")).longValue();
                if (usuarioRepository.existsByPsicologoId(psicologoId))
                    return error("Este psicólogo ya tiene un usuario asignado");
                Psicologo psicologo = psicologoRepository.findById(psicologoId)
                        .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));
                usuario.setPsicologo(psicologo);
            }

            usuario = usuarioRepository.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", toMap(usuario)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityUtils.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Actualizar username
            if (body.containsKey("username")) {
                String nuevoUsername = ((String) body.get("username")).trim();
                if (nuevoUsername.isBlank())
                    return error("El nombre de usuario no puede estar vacío");
                if (usuarioRepository.existsByUsernameAndIdNot(nuevoUsername, id))
                    return error("El nombre de usuario '" + nuevoUsername + "' ya está en uso");
                usuario.setUsername(nuevoUsername);
            }

            // Actualizar contraseña
            if (body.containsKey("password") && body.get("password") != null) {
                String nuevaPassword = (String) body.get("password");
                if (nuevaPassword.length() < 6)
                    return error("La contraseña debe tener al menos 6 caracteres");
                usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            }

            if (body.containsKey("rol")) usuario.setRol((String) body.get("rol"));
            if (body.containsKey("activo")) usuario.setActivo((Boolean) body.get("activo"));

            // Actualizar psicólogo vinculado
            if (body.containsKey("psicologoId")) {
                if (body.get("psicologoId") == null) {
                    usuario.setPsicologo(null);
                } else {
                    Long psicologoId = ((Number) body.get("psicologoId")).longValue();
                    if (usuarioRepository.existsByPsicologoIdAndIdNot(psicologoId, id))
                        return error("Este psicólogo ya tiene un usuario asignado");
                    usuario.setPsicologo(psicologoRepository.findById(psicologoId).orElse(null));
                }
            }

            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("success", true, "data", toMap(usuario)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        try {
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("success", true, "message", "Usuario desactivado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private ResponseEntity<?> error(String msg) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", msg));
    }

    private Map<String, Object> toMap(Usuario u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("rol", u.getRol());
        m.put("activo", u.getActivo());
        m.put("psicologoId", u.getPsicologo() != null ? u.getPsicologo().getId() : null);
        if (u.getPsicologo() != null && u.getPsicologo().getPerson() != null) {
            m.put("psicologoNombre", u.getPsicologo().getPerson().getPrimerNombre()
                    + " " + u.getPsicologo().getPerson().getApellidoPaterno());
        } else {
            m.put("psicologoNombre", null);
        }
        return m;
    }
}
