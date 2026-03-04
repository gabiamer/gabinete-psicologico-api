//src/main/java/com/gabinete/psicologico_api/dto/AntecedentesDTO.java
package com.gabinete.psicologico_api.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AntecedentesDTO {
    // Campo único para motivo y antecedentes
    private String motivoConsulta;
    
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
    private String numeroHermanos;
    private String relatoHermanos;
    
    // Sintomatologías
    private Map<String, Object> sintomas;
    private Integer totalScoreEstres;
    private Integer totalScoreAnsiedad;
    private Integer totalScoreDepresion;

    // Universidad
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

    // Evaluación (paso 7)
    private String gravedad;
    private List<String> tipologias;
    
    // Acuerdos (paso 6) - CAMPOS ELIMINADOS: notasSesion, objetivosSesion
    private String acuerdosEstablecidos;
    private String proximaSesionFecha;
    private String proximaSesionHora;
    private String historiaClinica;
}