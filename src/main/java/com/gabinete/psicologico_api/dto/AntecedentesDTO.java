//src/main/java/com/gabinete/psicologico_api/dto/AntecedentesDTO.java
package com.gabinete.psicologico_api.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AntecedentesDTO {
    // Situación actual
    private String ultimaVezBien;
    private String desarrolloSintomas;
    private String antecedentesFamiliares;
    
    // Funciones orgánicas
    private String sueno;
    private String apetito;
    private String sed;
    private String defecacion;
    
    // Historia familiar
    private String conQuienVive;
    private String personaReferencia;
    private String celularReferencia;
    
    // Padre
    private String nombrePadre;
    private String ocupacionPadre;
    private String enfermedadPadre;
    private String relacionPadre;
    
    // Madre
    private String nombreMadre;
    private String ocupacionMadre;
    private String enfermedadMadre;
    private String relacionMadre;
    
    // Hermanos
    private Integer numeroHermanos;
    private String relatoHermanos;
    
    // Sintomatologías
    private Map<String, Object> sintomas; // Contiene estres, ansiedad, depresion (arrays)
    private Integer totalScoreEstres;
    private Integer totalScoreAnsiedad;
    private Integer totalScoreDepresion;

    // Universidad
    private String cursoActual;
    private Integer nivelSatisfaccion;
    private Integer rendimiento;
    private Integer estresUniversitario;
    private Integer interaccionSocial;
    private String cambioCarreras;
    private String motivosCambio;
    private String relatoUniversidad;
    
    // Hábitos
    private String consumoAlcohol;
    private Integer frecuenciaAlcohol;

    private String consumoTabaco;
    private Integer frecuenciaTabaco;

    private String consumoDrogas;
    private Integer frecuenciaDrogas;

    private String relatoAcusacionDetencion;

    // Acuerdos
    private String acuerdosEstablecidos;
    private String proximaSesionFecha;
    private String proximaSesionHora;
}