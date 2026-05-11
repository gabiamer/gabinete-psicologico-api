package com.gabinete.psicologico_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static PsicologoPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PsicologoPrincipal p) {
            return p;
        }
        return null;
    }

    public static Long getCurrentPsicologoId() {
        PsicologoPrincipal p = getCurrentPrincipal();
        return p != null ? p.psicologoId() : null;
    }

    public static boolean isAdmin() {
        PsicologoPrincipal p = getCurrentPrincipal();
        return p != null && p.isAdmin();
    }
}
