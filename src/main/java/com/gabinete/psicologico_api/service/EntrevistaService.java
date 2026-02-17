//src/main/java/com/gabinete/psicologico_api/service/EntrevistaService.java
package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.dto.AntecedentesDTO;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EntrevistaService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

    @Autowired
    private EntrevistaPsicologicaRepository entrevistaPsicologicaRepository;

    @Transactional
    public Long guardarAntecedentes(Long pacienteUniversitarioId, AntecedentesDTO dto) throws Exception {

        System.out.println("=== PASO 1: Buscando paciente " + pacienteUniversitarioId);
        PacienteUniversitario pu = pacienteUniversitarioRepository
                .findById(pacienteUniversitarioId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + pacienteUniversitarioId));

        System.out.println("=== PASO 2: Creando sesion");
        SesionPaciente sesion = new SesionPaciente();
        sesion.setPacienteUniversitario(pu);
        sesion.setPsicologo(pu.getPsicologo());
        sesion.setFecha(LocalDateTime.now());
        sesion.setTipo("Primera Sesión");
        
        // **Crear objeto con todos los datos de la entrevista inicial**
        Map<String, Object> acuerdosCompletos = new HashMap<>();
        
        // Datos básicos
        acuerdosCompletos.put("ultimaVezBien", dto.getUltimaVezBien());
        acuerdosCompletos.put("desarrolloSintomas", dto.getDesarrolloSintomas());
        acuerdosCompletos.put("antecedentesFamiliares", dto.getAntecedentesFamiliares());
        
        // Funciones orgánicas
        acuerdosCompletos.put("sueno", dto.getSueno());
        acuerdosCompletos.put("apetito", dto.getApetito());
        acuerdosCompletos.put("sed", dto.getSed());
        acuerdosCompletos.put("defecacion", dto.getDefecacion());
        
        // Situación actual
        acuerdosCompletos.put("conQuienVive", dto.getConQuienVive());
        acuerdosCompletos.put("personaReferencia", dto.getPersonaReferencia());
        acuerdosCompletos.put("celularReferencia", dto.getCelularReferencia());
        
        // Padre
        acuerdosCompletos.put("nombrePadre", dto.getNombrePadre());
        acuerdosCompletos.put("ocupacionPadre", dto.getOcupacionPadre());
        acuerdosCompletos.put("enfermedadPadre", dto.getEnfermedadPadre());
        acuerdosCompletos.put("relacionPadre", dto.getRelacionPadre());
        
        // Madre
        acuerdosCompletos.put("nombreMadre", dto.getNombreMadre());
        acuerdosCompletos.put("ocupacionMadre", dto.getOcupacionMadre());
        acuerdosCompletos.put("enfermedadMadre", dto.getEnfermedadMadre());
        acuerdosCompletos.put("relacionMadre", dto.getRelacionMadre());
        
        // Hermanos
        acuerdosCompletos.put("numeroHermanos", dto.getNumeroHermanos());
        acuerdosCompletos.put("relatoHermanos", dto.getRelatoHermanos());
        
        // Sintomatologías
        acuerdosCompletos.put("sintomatologias", dto.getSintomas());
        acuerdosCompletos.put("totalScoreEstres", dto.getTotalScoreEstres());
        acuerdosCompletos.put("totalScoreAnsiedad", dto.getTotalScoreAnsiedad());
        acuerdosCompletos.put("totalScoreDepresion", dto.getTotalScoreDepresion());
        
        // Universidad
        acuerdosCompletos.put("nivelSatisfaccion", dto.getNivelSatisfaccion());
        acuerdosCompletos.put("rendimiento", dto.getRendimiento());
        acuerdosCompletos.put("estresUniversitario", dto.getEstresUniversitario());
        acuerdosCompletos.put("interaccionSocial", dto.getInteraccionSocial());
        acuerdosCompletos.put("cambioCarreras", dto.getCambioCarreras());
        acuerdosCompletos.put("motivosCambio", dto.getMotivosCambio());
        acuerdosCompletos.put("relatoUniversidad", dto.getRelatoUniversidad());
        
        // Hábitos
        acuerdosCompletos.put("consumoAlcohol", dto.getConsumoAlcohol());
        acuerdosCompletos.put("frecuenciaAlcohol", dto.getFrecuenciaAlcohol());
        acuerdosCompletos.put("consumoTabaco", dto.getConsumoTabaco());
        acuerdosCompletos.put("frecuenciaTabaco", dto.getFrecuenciaTabaco());
        acuerdosCompletos.put("consumoDrogas", dto.getConsumoDrogas());
        acuerdosCompletos.put("frecuenciaDrogas", dto.getFrecuenciaDrogas());
        acuerdosCompletos.put("relatoAcusacionDetencion", dto.getRelatoAcusacionDetencion());
        
        // Evaluación (paso 6)
        acuerdosCompletos.put("gravedad", dto.getGravedad());
        acuerdosCompletos.put("tipologias", dto.getTipologias());
        
        // Sesión inicial (paso 7)
        acuerdosCompletos.put("notasSesion", dto.getNotasSesion());
        acuerdosCompletos.put("objetivosSesion", dto.getObjetivosSesion());
        acuerdosCompletos.put("acuerdosEstablecidos", dto.getAcuerdosEstablecidos());
        acuerdosCompletos.put("proximaSesionFecha", dto.getProximaSesionFecha());
        acuerdosCompletos.put("proximaSesionHora", dto.getProximaSesionHora());
        
        // Convertir a JSON y guardar en el campo acuerdos
        String acuerdosJson = objectMapper.writeValueAsString(acuerdosCompletos);
        sesion.setAcuerdos(acuerdosJson);
        
        sesionPacienteRepository.save(sesion);
        System.out.println("=== PASO 2 OK: Sesion id=" + sesion.getId() + " con acuerdos completos");

        // El resto del código para guardar en EntrevistaPsicologica permanece igual...
        System.out.println("=== PASO 3: Preparando datos para EntrevistaPsicologica");
        
        // Funciones orgánicas
        Map<String, Object> funcionesOrganicas = new HashMap<>();
        funcionesOrganicas.put("sueno", dto.getSueno());
        funcionesOrganicas.put("apetito", dto.getApetito());
        funcionesOrganicas.put("sed", dto.getSed());
        funcionesOrganicas.put("defecacion", dto.getDefecacion());
        String habitosJson = objectMapper.writeValueAsString(funcionesOrganicas);

        // Historia familiar
        Map<String, Object> familia = new HashMap<>();
        familia.put("conQuienVive", dto.getConQuienVive());
        familia.put("personaReferencia", dto.getPersonaReferencia());
        familia.put("celularReferencia", dto.getCelularReferencia());
        
        Map<String, Object> padre = new HashMap<>();
        padre.put("nombre", dto.getNombrePadre());
        padre.put("ocupacion", dto.getOcupacionPadre());
        padre.put("enfermedad", dto.getEnfermedadPadre());
        padre.put("relacion", dto.getRelacionPadre());
        familia.put("padre", padre);
        
        Map<String, Object> madre = new HashMap<>();
        madre.put("nombre", dto.getNombreMadre());
        madre.put("ocupacion", dto.getOcupacionMadre());
        madre.put("enfermedad", dto.getEnfermedadMadre());
        madre.put("relacion", dto.getRelacionMadre());
        familia.put("madre", madre);
        
        Map<String, Object> hermanos = new HashMap<>();
        hermanos.put("numero", dto.getNumeroHermanos());
        hermanos.put("relato", dto.getRelatoHermanos());
        familia.put("hermanos", hermanos);
        
        String familiaJson = objectMapper.writeValueAsString(familia);

        // Sintomas
        String sintomasJson = null;
        if (dto.getSintomas() != null) {
            sintomasJson = objectMapper.writeValueAsString(dto.getSintomas());
        }

        System.out.println("=== PASO 4: Guardando entrevista");
        EntrevistaPsicologica entrevista = new EntrevistaPsicologica();
        entrevista.setSesionPaciente(sesion);
        entrevista.setAntecedentes(
            "Última vez bien: " + dto.getUltimaVezBien() + "\n\n" +
            "Desarrollo síntomas: " + dto.getDesarrolloSintomas() + "\n\n" +
            "Antecedentes familiares: " + dto.getAntecedentesFamiliares()
        );
        entrevista.setHabitos(habitosJson);
        entrevista.setDatosFamilia(familiaJson);
        
        entrevista.setSintomas(sintomasJson);
        entrevista.setTotalScoreEstres(dto.getTotalScoreEstres());
        entrevista.setTotalScoreAnsiedad(dto.getTotalScoreAnsiedad());
        entrevista.setTotalScoreDepresion(dto.getTotalScoreDepresion());
        
        entrevista.setRelatoUniversidad(dto.getRelatoUniversidad());

        // Hábitos completos
        Map<String, Object> habitos = new HashMap<>();
        Map<String, Object> alcohol = new HashMap<>();
        alcohol.put("descripcion", dto.getConsumoAlcohol());
        alcohol.put("frecuencia", dto.getFrecuenciaAlcohol());
        habitos.put("alcohol", alcohol);

        Map<String, Object> tabaco = new HashMap<>();
        tabaco.put("descripcion", dto.getConsumoTabaco());
        tabaco.put("frecuencia", dto.getFrecuenciaTabaco());
        habitos.put("tabaco", tabaco);

        Map<String, Object> drogas = new HashMap<>();
        drogas.put("descripcion", dto.getConsumoDrogas());
        drogas.put("frecuencia", dto.getFrecuenciaDrogas());
        habitos.put("drogas", drogas);

        habitos.put("relatoAcusacionDetencion", dto.getRelatoAcusacionDetencion());
        habitos.put("nivelSatisfaccion", dto.getNivelSatisfaccion());
        habitos.put("rendimiento", dto.getRendimiento());
        habitos.put("estresUniversitario", dto.getEstresUniversitario());
        habitos.put("interaccionSocial", dto.getInteraccionSocial());
        habitos.put("cambioCarreras", dto.getCambioCarreras());
        habitos.put("motivosCambio", dto.getMotivosCambio());

        String habitosCompletos = objectMapper.writeValueAsString(habitos);
        entrevista.setHabitos(habitosCompletos);

        entrevistaPsicologicaRepository.save(entrevista);
        System.out.println("=== PASO 4 OK: Entrevista id=" + entrevista.getId());

        return entrevista.getId();
    }
}