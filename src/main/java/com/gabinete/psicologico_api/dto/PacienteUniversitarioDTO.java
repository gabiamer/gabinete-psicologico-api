package com.gabinete.psicologico_api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PacienteUniversitarioDTO {
    private PersonDTO person;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private Integer genero;
    private String domicilio;
    private Integer estadoCivil;
    private Integer semestre;
    private String derivadoPor;
    private Long carreraId;
    private Long psicologoId;
}