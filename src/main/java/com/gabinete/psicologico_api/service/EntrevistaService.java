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

        // Sintomas (convertir Map a JSON)
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
        
        // Guardar sintomas y scores
        entrevista.setSintomas(sintomasJson);
        entrevista.setTotalScoreEstres(dto.getTotalScoreEstres());
        entrevista.setTotalScoreAnsiedad(dto.getTotalScoreAnsiedad());
        entrevista.setTotalScoreDepresion(dto.getTotalScoreDepresion());
        
        // Relato Universidad (Sección VI)
        entrevista.setRelatoUniversidad(dto.getRelatoUniversidad());

        // Hábitos
        Map<String, Object> habitos = new HashMap<>();

        // Consumo de sustancias con sus frecuencias
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

        // Universidad
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