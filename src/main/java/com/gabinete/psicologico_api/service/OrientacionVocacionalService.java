package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.dto.OrientacionVocacionalDTO;
import com.gabinete.psicologico_api.model.OrientacionVocacional;
import com.gabinete.psicologico_api.model.PacienteExterno;
import com.gabinete.psicologico_api.repository.OrientacionVocacionalRepository;
import com.gabinete.psicologico_api.repository.PacienteExternoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrientacionVocacionalService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrientacionVocacionalRepository orientacionRepository;

    @Autowired
    private PacienteExternoRepository pacienteExternoRepository;

    @Transactional
    public OrientacionVocacional guardarEntrevista(Long pacienteExternoId, OrientacionVocacionalDTO dto) throws Exception {
        
        System.out.println("=== Guardando Orientación Vocacional para paciente externo: " + pacienteExternoId);
        
        PacienteExterno paciente = pacienteExternoRepository.findById(pacienteExternoId)
                .orElseThrow(() -> new RuntimeException("Paciente externo no encontrado: " + pacienteExternoId));

        // Convertir DTO a JSON
        Map<String, Object> respuestasMap = new HashMap<>();
        
        // Datos personales
        respuestasMap.put("motivoConsulta", dto.getMotivoConsulta());
        respuestasMap.put("actividadesHobbies", dto.getActividadesHobbies());
        respuestasMap.put("cualidad1", dto.getCualidad1());
        respuestasMap.put("cualidad2", dto.getCualidad2());
        respuestasMap.put("cualidad3", dto.getCualidad3());
        respuestasMap.put("defecto1", dto.getDefecto1());
        respuestasMap.put("defecto2", dto.getDefecto2());
        respuestasMap.put("defecto3", dto.getDefecto3());
        respuestasMap.put("temasInteres", dto.getTemasInteres());
        respuestasMap.put("ocupacionMadre", dto.getOcupacionMadre());
        respuestasMap.put("ocupacionPadre", dto.getOcupacionPadre());
        respuestasMap.put("ocupacionOtros", dto.getOcupacionOtros());
        
        // Estudios
        respuestasMap.put("mismaPrimaria", dto.getMismaPrimaria());
        respuestasMap.put("motivoCambioPrimaria", dto.getMotivoCambioPrimaria());
        respuestasMap.put("mismaSecundaria", dto.getMismaSecundaria());
        respuestasMap.put("motivoCambioSecundaria", dto.getMotivoCambioSecundaria());
        respuestasMap.put("materiaInteresante1", dto.getMateriaInteresante1());
        respuestasMap.put("materiaInteresante2", dto.getMateriaInteresante2());
        respuestasMap.put("materiaInteresante3", dto.getMateriaInteresante3());
        respuestasMap.put("motivoMateriasInteresantes", dto.getMotivoMateriasInteresantes());
        respuestasMap.put("materiaDesinteresante1", dto.getMateriaDesinteresante1());
        respuestasMap.put("materiaDesinteresante2", dto.getMateriaDesinteresante2());
        respuestasMap.put("materiaDesinteresante3", dto.getMateriaDesinteresante3());
        respuestasMap.put("motivoMateriasDesinteresantes", dto.getMotivoMateriasDesinteresantes());
        respuestasMap.put("satisfaccionesEscuela", dto.getSatisfaccionesEscuela());
        respuestasMap.put("relacionCompaneros", dto.getRelacionCompaneros());
        respuestasMap.put("relacionProfesores", dto.getRelacionProfesores());
        
        // Objetivos profesionales
        respuestasMap.put("planDespuesPreparatoria", dto.getPlanDespuesPreparatoria());
        respuestasMap.put("motivoPlanFuturo", dto.getMotivoPlanFuturo());
        respuestasMap.put("carreraInteres1", dto.getCarreraInteres1());
        respuestasMap.put("carreraInteres2", dto.getCarreraInteres2());
        respuestasMap.put("carreraInteres3", dto.getCarreraInteres3());
        respuestasMap.put("carreraNoInteres1", dto.getCarreraNoInteres1());
        respuestasMap.put("carreraNoInteres2", dto.getCarreraNoInteres2());
        respuestasMap.put("carreraNoInteres3", dto.getCarreraNoInteres3());
        respuestasMap.put("factorEconomico", dto.getFactorEconomico());
        respuestasMap.put("apoyoFamiliar", dto.getApoyoFamiliar());
        respuestasMap.put("visionCincoAnos", dto.getVisionCincoAnos());
        respuestasMap.put("tipoTrabajosDeseados", dto.getTipoTrabajosDeseados());
        
        String respuestasJson = objectMapper.writeValueAsString(respuestasMap);
        
        OrientacionVocacional orientacion = new OrientacionVocacional();
        orientacion.setPacienteExterno(paciente);
        orientacion.setRespuestas(respuestasJson);
        orientacion.setObservaciones(dto.getObservacionesEntrevistador());
        
        OrientacionVocacional guardado = orientacionRepository.save(orientacion);
        System.out.println("=== Orientación Vocacional guardada con ID: " + guardado.getId());
        
        return guardado;
    }

    public List<OrientacionVocacional> obtenerEntrevistasPorPaciente(Long pacienteExternoId) {
        return orientacionRepository.findByPacienteExternoId(pacienteExternoId);
    }

    public OrientacionVocacional obtenerUltimaEntrevista(Long pacienteExternoId) {
        return orientacionRepository.findTopByPacienteExternoIdOrderByFechaDesc(pacienteExternoId)
                .orElse(null);
    }
}