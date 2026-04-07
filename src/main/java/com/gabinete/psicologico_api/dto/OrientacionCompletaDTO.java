package com.gabinete.psicologico_api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OrientacionCompletaDTO {

    // ── Datos del paciente externo (PacienteExternoDTO) ───────────────────
    private PersonDTO person;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private Integer genero;
    private String domicilio;
    private Integer estadoCivil;
    private String escuela;
    private Integer anio;
    private String correo;

    // ── Orientación vocacional (OrientacionVocacionalDTO) ─────────────────
    private String motivoConsulta;
    private String actividadesHobbies;
    private String cualidad1;
    private String cualidad2;
    private String cualidad3;
    private String defecto1;
    private String defecto2;
    private String defecto3;
    private String temasInteres;
    private String ocupacionMadre;
    private String ocupacionPadre;
    private String ocupacionOtros;

    private String mismaPrimaria;
    private String motivoCambioPrimaria;
    private String mismaSecundaria;
    private String motivoCambioSecundaria;
    private String materiaInteresante1;
    private String materiaInteresante2;
    private String materiaInteresante3;
    private String motivoMateriasInteresantes;
    private String materiaDesinteresante1;
    private String materiaDesinteresante2;
    private String materiaDesinteresante3;
    private String motivoMateriasDesinteresantes;
    private String satisfaccionesEscuela;
    private String relacionCompaneros;
    private String relacionProfesores;

    private String planDespuesPreparatoria;
    private String motivoPlanFuturo;
    private String carreraInteres1;
    private String carreraInteres2;
    private String carreraInteres3;
    private String carreraNoInteres1;
    private String carreraNoInteres2;
    private String carreraNoInteres3;
    private String factorEconomico;
    private String apoyoFamiliar;
    private String visionCincoAnos;
    private String tipoTrabajosDeseados;
    private String observacionesEntrevistador;
}
