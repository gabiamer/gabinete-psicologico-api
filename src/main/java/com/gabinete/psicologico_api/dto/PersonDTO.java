package com.gabinete.psicologico_api.dto;

import lombok.Data;

@Data
public class PersonDTO {
    private String primerNombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String celular;
}