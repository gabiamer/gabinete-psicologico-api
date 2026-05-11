package com.gabinete.psicologico_api.security;

public record PsicologoPrincipal(String username, Long psicologoId, String rol) {
    public boolean isAdmin() {
        return "ADMIN".equals(rol);
    }
}
