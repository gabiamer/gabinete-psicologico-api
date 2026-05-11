package com.gabinete.psicologico_api.service;

import com.gabinete.psicologico_api.dto.LoginRequest;
import com.gabinete.psicologico_api.dto.LoginResponse;
import com.gabinete.psicologico_api.model.Usuario;
import com.gabinete.psicologico_api.repository.UsuarioRepository;
import com.gabinete.psicologico_api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authenticate(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("Usuario desactivado");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        String token = jwtService.generateToken(usuario);

        Long psicologoId = usuario.getPsicologo() != null ? usuario.getPsicologo().getId() : null;
        String psicologoNombre = null;
        if (usuario.getPsicologo() != null && usuario.getPsicologo().getPerson() != null) {
            psicologoNombre = usuario.getPsicologo().getPerson().getPrimerNombre()
                    + " " + usuario.getPsicologo().getPerson().getApellidoPaterno();
        }

        return new LoginResponse(token, usuario.getUsername(), psicologoId, psicologoNombre, usuario.getRol());
    }
}
