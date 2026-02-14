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
        sesionPacienteRepository.save(sesion);
        System.out.println("=== PASO 2 OK: Sesion id=" + sesion.getId());

        System.out.println("=== PASO 3: Preparando datos");
        Map<String, Object> funcionesOrganicas = new HashMap<>();
        funcionesOrganicas.put("sueno", dto.getSueno());
        funcionesOrganicas.put("apetito", dto.getApetito());
        funcionesOrganicas.put("sed", dto.getSed());
        funcionesOrganicas.put("defecacion", dto.getDefecacion());
        String habitosJson = objectMapper.writeValueAsString(funcionesOrganicas);

        System.out.println("=== PASO 4: Guardando entrevista");
        EntrevistaPsicologica entrevista = new EntrevistaPsicologica();
        entrevista.setSesionPaciente(sesion);
        entrevista.setAntecedentes(
            "Última vez bien: " + dto.getUltimaVezBien() + "\n\n" +
            "Desarrollo síntomas: " + dto.getDesarrolloSintomas() + "\n\n" +
            "Antecedentes familiares: " + dto.getAntecedentesFamiliares()
        );
        entrevista.setHabitos(habitosJson);
        entrevistaPsicologicaRepository.save(entrevista);
        System.out.println("=== PASO 4 OK: Entrevista id=" + entrevista.getId());

        return entrevista.getId();
    }
}