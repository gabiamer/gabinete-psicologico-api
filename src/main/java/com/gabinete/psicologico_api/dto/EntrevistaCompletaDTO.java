package com.gabinete.psicologico_api.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class EntrevistaCompletaDTO {

    // ── Datos del paciente (PacienteUniversitarioDTO) ─────────────────────
    private PersonDTO person;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String domicilio;
    private Integer estadoCivil;
    private Integer genero;
    private Integer semestre;
    private String derivadoPor;
    private Long carreraId;
    private Long psicologoId;

    // ── Antecedentes / entrevista (AntecedentesDTO) ───────────────────────
    private String motivoConsulta;
    private String conQuienVive;
    private String personaReferencia;
    private String celularReferencia;

    private String nombrePadre;
    private String ocupacionPadre;
    private String enfermedadPadre;
    private String relacionPadre;

    private String nombreMadre;
    private String ocupacionMadre;
    private String enfermedadMadre;
    private String relacionMadre;

    private String numeroHermanos;
    private String relatoHermanos;

    private Map<String, Object> sintomas;
    private Integer totalScoreEstres;
    private Integer totalScoreAnsiedad;
    private Integer totalScoreDepresion;

    private String cambioCarreras;
    private String motivosCambio;
    private String relatoUniversidad;

    private String consumoAlcohol;
    private Integer frecuenciaAlcohol;
    private String consumoTabaco;
    private Integer frecuenciaTabaco;
    private String consumoDrogas;
    private Integer frecuenciaDrogas;
    private String relatoAcusacionDetencion;

    private String gravedad;
    private List<String> tipologias;

    private String acuerdosEstablecidos;
    private String proximaSesionFecha;
    private String proximaSesionHora;
    private String historiaClinica;
}
