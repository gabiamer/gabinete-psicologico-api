//src/main/java/com/gabinete/psicologico_api/service/PacienteExternoService.java
package com.gabinete.psicologico_api.service;

import com.gabinete.psicologico_api.dto.PacienteExternoDTO;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteExternoService {
    
    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private PacienteExternoRepository pacienteExternoRepository;
    
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
        paciente.setGenero(dto.getGenero());
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

    public List<PacienteExterno> buscarPacientes(String termino) {
        return pacienteExternoRepository.buscarPorTermino(termino);
    }

    public PacienteExterno obtenerPorId(Long id) {
        return pacienteExternoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente externo no encontrado: " + id));
    }
}