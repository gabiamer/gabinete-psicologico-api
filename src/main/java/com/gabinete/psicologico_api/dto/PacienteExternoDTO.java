package com.gabinete.psicologico_api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PacienteExternoDTO {
    private PersonDTO person;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private Integer genero;
    private String domicilio;
    private Integer estadoCivil;
    private String escuela;
    private Integer anio;
    private String correo;
}