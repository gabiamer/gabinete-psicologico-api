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

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Transactional
    public Long guardarAntecedentes(Long pacienteUniversitarioId, AntecedentesDTO dto) throws Exception {

        PacienteUniversitario pu = pacienteUniversitarioRepository
                .findById(pacienteUniversitarioId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + pacienteUniversitarioId));

        // 1. Crear sesion
        SesionPaciente sesion = new SesionPaciente();
        sesion.setPacienteUniversitario(pu);
        sesion.setPsicologo(pu.getPsicologo());
        sesion.setFecha(LocalDateTime.now());
        sesionPacienteRepository.save(sesion);

        // 2. Historia familiar (JSONB)
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

        String historiaFamiliarJson = objectMapper.writeValueAsString(familia);

        // 3. Relato universidad (JSONB)
        Map<String, Object> universidad = new HashMap<>();
        universidad.put("cambioCarreras", dto.getCambioCarreras());
        universidad.put("motivosCambio", dto.getMotivosCambio());
        universidad.put("relatoGeneral", dto.getRelatoUniversidad());
        String relatoUniversidadJson = objectMapper.writeValueAsString(universidad);

        // 4. Sintomas
        String sintomasJson = null;
        if (dto.getSintomas() != null) {
            sintomasJson = objectMapper.writeValueAsString(dto.getSintomas());
        }

        // 5. Habitos (solo consumo y situaciones legales, sin datos de carrera)
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
        String habitosJson = objectMapper.writeValueAsString(habitos);

        // 6. Acuerdos (JSONB)
        Map<String, Object> acuerdos = new HashMap<>();
        acuerdos.put("acuerdosEstablecidos", dto.getAcuerdosEstablecidos());
        acuerdos.put("proximaSesionFecha", dto.getProximaSesionFecha());
        acuerdos.put("proximaSesionHora", dto.getProximaSesionHora());
        String acuerdosJson = objectMapper.writeValueAsString(acuerdos);

        // 7. Guardar entrevista psicologica
        EntrevistaPsicologica entrevista = new EntrevistaPsicologica();
        entrevista.setSesionPaciente(sesion);
        entrevista.setAntecedentes(dto.getMotivoConsulta());
        entrevista.setHistoriaFamiliar(historiaFamiliarJson);
        entrevista.setRelatoUniversidad(relatoUniversidadJson);
        entrevista.setSintomas(sintomasJson);
        entrevista.setTotalScoreEstres(dto.getTotalScoreEstres());
        entrevista.setTotalScoreAnsiedad(dto.getTotalScoreAnsiedad());
        entrevista.setTotalScoreDepresion(dto.getTotalScoreDepresion());
        entrevista.setHabitos(habitosJson);
        entrevista.setAcuerdos(acuerdosJson);
        entrevistaPsicologicaRepository.save(entrevista);

        // 8. Guardar historial clinico (primera sesion)
        HistorialClinico historial = new HistorialClinico();
        historial.setSesionPaciente(sesion);
        historial.setNroSesion(1);
        historial.setHistoria(dto.getHistoriaClinica());
        historial.setGravedad(dto.getGravedad());
        if (dto.getTipologias() != null) {
            historial.setTipologia(objectMapper.writeValueAsString(dto.getTipologias()));
        }
        historialClinicoRepository.save(historial);

        return entrevista.getId();
    }
}
