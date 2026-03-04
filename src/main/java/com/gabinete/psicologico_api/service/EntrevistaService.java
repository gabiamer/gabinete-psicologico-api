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
        // CAMPO ELIMINADO: sesion.setTipo("Primera Sesión")

        sesionPacienteRepository.save(sesion);
        System.out.println("=== PASO 2 OK: Sesion id=" + sesion.getId());

        System.out.println("=== PASO 3: Preparando datos para EntrevistaPsicologica");

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

        // Hábitos
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
        habitos.put("cambioCarreras", dto.getCambioCarreras());
        habitos.put("motivosCambio", dto.getMotivosCambio());

        String habitosJson = objectMapper.writeValueAsString(habitos);

        // Acuerdos completos (toda la info de la entrevista en JSON)
        Map<String, Object> acuerdosCompletos = new HashMap<>();
        
        acuerdosCompletos.put("historiaClinica", dto.getHistoriaClinica());
        acuerdosCompletos.put("motivoConsulta", dto.getMotivoConsulta());
        acuerdosCompletos.put("conQuienVive", dto.getConQuienVive());
        acuerdosCompletos.put("personaReferencia", dto.getPersonaReferencia());
        acuerdosCompletos.put("celularReferencia", dto.getCelularReferencia());
        acuerdosCompletos.put("nombrePadre", dto.getNombrePadre());
        acuerdosCompletos.put("ocupacionPadre", dto.getOcupacionPadre());
        acuerdosCompletos.put("enfermedadPadre", dto.getEnfermedadPadre());
        acuerdosCompletos.put("relacionPadre", dto.getRelacionPadre());
        acuerdosCompletos.put("nombreMadre", dto.getNombreMadre());
        acuerdosCompletos.put("ocupacionMadre", dto.getOcupacionMadre());
        acuerdosCompletos.put("enfermedadMadre", dto.getEnfermedadMadre());
        acuerdosCompletos.put("relacionMadre", dto.getRelacionMadre());
        acuerdosCompletos.put("numeroHermanos", dto.getNumeroHermanos());
        acuerdosCompletos.put("relatoHermanos", dto.getRelatoHermanos());
        acuerdosCompletos.put("sintomatologias", dto.getSintomas());
        acuerdosCompletos.put("totalScoreEstres", dto.getTotalScoreEstres());
        acuerdosCompletos.put("totalScoreAnsiedad", dto.getTotalScoreAnsiedad());
        acuerdosCompletos.put("totalScoreDepresion", dto.getTotalScoreDepresion());
        acuerdosCompletos.put("cambioCarreras", dto.getCambioCarreras());
        acuerdosCompletos.put("motivosCambio", dto.getMotivosCambio());
        acuerdosCompletos.put("relatoUniversidad", dto.getRelatoUniversidad());
        acuerdosCompletos.put("consumoAlcohol", dto.getConsumoAlcohol());
        acuerdosCompletos.put("frecuenciaAlcohol", dto.getFrecuenciaAlcohol());
        acuerdosCompletos.put("consumoTabaco", dto.getConsumoTabaco());
        acuerdosCompletos.put("frecuenciaTabaco", dto.getFrecuenciaTabaco());
        acuerdosCompletos.put("consumoDrogas", dto.getConsumoDrogas());
        acuerdosCompletos.put("frecuenciaDrogas", dto.getFrecuenciaDrogas());
        acuerdosCompletos.put("relatoAcusacionDetencion", dto.getRelatoAcusacionDetencion());
        acuerdosCompletos.put("gravedad", dto.getGravedad());
        acuerdosCompletos.put("tipologias", dto.getTipologias());
        // CAMPOS ELIMINADOS: notasSesion, objetivosSesion
        acuerdosCompletos.put("acuerdosEstablecidos", dto.getAcuerdosEstablecidos());
        acuerdosCompletos.put("proximaSesionFecha", dto.getProximaSesionFecha());
        acuerdosCompletos.put("proximaSesionHora", dto.getProximaSesionHora());

        String acuerdosJson = objectMapper.writeValueAsString(acuerdosCompletos);

        System.out.println("=== PASO 4: Guardando entrevista");
        EntrevistaPsicologica entrevista = new EntrevistaPsicologica();
        entrevista.setSesionPaciente(sesion);
        entrevista.setAntecedentes(dto.getMotivoConsulta());
        entrevista.setDatosFamilia(familiaJson);
        entrevista.setSintomas(sintomasJson);
        entrevista.setTotalScoreEstres(dto.getTotalScoreEstres());
        entrevista.setTotalScoreAnsiedad(dto.getTotalScoreAnsiedad());
        entrevista.setTotalScoreDepresion(dto.getTotalScoreDepresion());
        entrevista.setRelatoUniversidad(dto.getRelatoUniversidad());
        entrevista.setHabitos(habitosJson);
        entrevista.setAcuerdos(acuerdosJson);

        entrevistaPsicologicaRepository.save(entrevista);
        System.out.println("=== PASO 4 OK: Entrevista id=" + entrevista.getId());

        return entrevista.getId();
    }
}