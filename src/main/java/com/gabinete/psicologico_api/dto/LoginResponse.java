package com.gabinete.psicologico_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private Long psicologoId;
    private String psicologoNombre;
    private String rol;
}
