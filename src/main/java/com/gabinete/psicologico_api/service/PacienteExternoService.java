//src/main/java/com/gabinete/psicologico_api/service/PacienteExternoService.java
package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.dto.OrientacionCompletaDTO;
import com.gabinete.psicologico_api.dto.PacienteExternoDTO;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PacienteExternoService {
    
    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private PacienteExternoRepository pacienteExternoRepository;

    @Autowired
    private OrientacionVocacionalRepository orientacionVocacionalRepository;
    
    @Transactional
    public PacienteExterno crearPacienteExterno(PacienteExternoDTO dto) {
        System.out.println("=== Creando Paciente Externo ===");
        
        // 1. Crear Person
        Person person = new Person();
        person.setPrimerNombre(dto.getPerson().getPrimerNombre());
        person.setSegundoNombre(dto.getPerson().getSegundoNombre());
        person.setApellidoPaterno(dto.getPerson().getApellidoPaterno());
        person.setApellidoMaterno(dto.getPerson().getApellidoMaterno());
        person.setCelular(dto.getPerson().getCelular());
        person = personRepository.save(person);
        System.out.println("Person guardado con ID: " + person.getId());
        
        // 2. Crear Paciente
        Paciente paciente = new Paciente();
        paciente.setPerson(person);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEdad(dto.getEdad());
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setTipoPaciente(2); // 2 = Paciente Externo
        paciente = pacienteRepository.save(paciente);
        System.out.println("Paciente guardado con ID: " + paciente.getId());
        
        // 3. Crear PacienteExterno
        PacienteExterno pe = new PacienteExterno();
        pe.setPaciente(paciente);
        pe.setEscuela(dto.getEscuela());
        pe.setAnio(dto.getAnio());
        pe.setCorreo(dto.getCorreo());
        pe = pacienteExternoRepository.save(pe);
        System.out.println("PacienteExterno guardado con ID: " + pe.getId());
        
        return pe;
    }

    @Transactional
    public void eliminarPacienteExternoCompleto(Long id) {
        PacienteExterno pe = pacienteExternoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente externo no encontrado: " + id));

        Paciente paciente = pe.getPaciente();
        Person person = paciente.getPerson();

        // 1. Eliminar orientaciones vocacionales
        orientacionVocacionalRepository.deleteAll(
                orientacionVocacionalRepository.findByPacienteExternoId(id));

        // 2. Eliminar PacienteExterno
        pacienteExternoRepository.delete(pe);

        // 3. Eliminar Paciente y Person
        pacienteRepository.delete(paciente);
        personRepository.delete(person);
    }

    public List<PacienteExterno> buscarPacientes(String termino) {
        return pacienteExternoRepository.buscarPorTermino(termino);
    }

    public PacienteExterno obtenerPorId(Long id) {
        return pacienteExternoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente externo no encontrado: " + id));
    }

    /** Crea paciente externo + orientación vocacional en una sola transacción */
    @Transactional
    public PacienteExterno crearOrientacionCompleta(OrientacionCompletaDTO dto) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // ── 1. Person ──────────────────────────────────────────────────────────
        Person person = new Person();
        person.setPrimerNombre(dto.getPerson().getPrimerNombre());
        person.setSegundoNombre(dto.getPerson().getSegundoNombre());
        person.setApellidoPaterno(dto.getPerson().getApellidoPaterno());
        person.setApellidoMaterno(dto.getPerson().getApellidoMaterno());
        person.setCelular(dto.getPerson().getCelular());
        person = personRepository.save(person);

        // ── 2. Paciente ────────────────────────────────────────────────────────
        Paciente paciente = new Paciente();
        paciente.setPerson(person);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEdad(dto.getEdad());
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setGenero(dto.getGenero());
        paciente.setTipoPaciente(2);
        paciente = pacienteRepository.save(paciente);

        // ── 3. PacienteExterno ─────────────────────────────────────────────────
        PacienteExterno pe = new PacienteExterno();
        pe.setPaciente(paciente);
        pe.setEscuela(dto.getEscuela());
        pe.setAnio(dto.getAnio());
        pe.setCorreo(dto.getCorreo());
        pe = pacienteExternoRepository.save(pe);

        // ── 4. OrientacionVocacional ───────────────────────────────────────────
        Map<String, Object> respuestas = new HashMap<>();
        respuestas.put("motivoConsulta", dto.getMotivoConsulta());
        respuestas.put("actividadesHobbies", dto.getActividadesHobbies());
        respuestas.put("cualidad1", dto.getCualidad1()); respuestas.put("cualidad2", dto.getCualidad2()); respuestas.put("cualidad3", dto.getCualidad3());
        respuestas.put("defecto1", dto.getDefecto1()); respuestas.put("defecto2", dto.getDefecto2()); respuestas.put("defecto3", dto.getDefecto3());
        respuestas.put("temasInteres", dto.getTemasInteres());
        respuestas.put("ocupacionMadre", dto.getOcupacionMadre()); respuestas.put("ocupacionPadre", dto.getOcupacionPadre()); respuestas.put("ocupacionOtros", dto.getOcupacionOtros());
        respuestas.put("mismaPrimaria", dto.getMismaPrimaria()); respuestas.put("motivoCambioPrimaria", dto.getMotivoCambioPrimaria());
        respuestas.put("mismaSecundaria", dto.getMismaSecundaria()); respuestas.put("motivoCambioSecundaria", dto.getMotivoCambioSecundaria());
        respuestas.put("materiaInteresante1", dto.getMateriaInteresante1()); respuestas.put("materiaInteresante2", dto.getMateriaInteresante2()); respuestas.put("materiaInteresante3", dto.getMateriaInteresante3());
        respuestas.put("motivoMateriasInteresantes", dto.getMotivoMateriasInteresantes());
        respuestas.put("materiaDesinteresante1", dto.getMateriaDesinteresante1()); respuestas.put("materiaDesinteresante2", dto.getMateriaDesinteresante2()); respuestas.put("materiaDesinteresante3", dto.getMateriaDesinteresante3());
        respuestas.put("motivoMateriasDesinteresantes", dto.getMotivoMateriasDesinteresantes());
        respuestas.put("satisfaccionesEscuela", dto.getSatisfaccionesEscuela());
        respuestas.put("relacionCompaneros", dto.getRelacionCompaneros()); respuestas.put("relacionProfesores", dto.getRelacionProfesores());
        respuestas.put("planDespuesPreparatoria", dto.getPlanDespuesPreparatoria()); respuestas.put("motivoPlanFuturo", dto.getMotivoPlanFuturo());
        respuestas.put("carreraInteres1", dto.getCarreraInteres1()); respuestas.put("carreraInteres2", dto.getCarreraInteres2()); respuestas.put("carreraInteres3", dto.getCarreraInteres3());
        respuestas.put("carreraNoInteres1", dto.getCarreraNoInteres1()); respuestas.put("carreraNoInteres2", dto.getCarreraNoInteres2()); respuestas.put("carreraNoInteres3", dto.getCarreraNoInteres3());
        respuestas.put("factorEconomico", dto.getFactorEconomico()); respuestas.put("apoyoFamiliar", dto.getApoyoFamiliar());
        respuestas.put("visionCincoAnos", dto.getVisionCincoAnos()); respuestas.put("tipoTrabajosDeseados", dto.getTipoTrabajosDeseados());

        OrientacionVocacional orientacion = new OrientacionVocacional();
        orientacion.setPacienteExterno(pe);
        orientacion.setRespuestas(mapper.writeValueAsString(respuestas));
        orientacion.setObservaciones(dto.getObservacionesEntrevistador());
        orientacionVocacionalRepository.save(orientacion);

        return pe;
    }
}