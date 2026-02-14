package com.gabinete.psicologico_api.service;

import com.gabinete.psicologico_api.dto.PacienteUniversitarioDTO;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {
    
    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;
    
    @Transactional
    public PacienteUniversitario crearPacienteUniversitario(PacienteUniversitarioDTO dto) {
        // 1. Crear Person
        Person person = new Person();
        person.setPrimerNombre(dto.getPerson().getPrimerNombre());
        person.setSegundoNombre(dto.getPerson().getSegundoNombre());
        person.setApellidoPaterno(dto.getPerson().getApellidoPaterno());
        person.setApellidoMaterno(dto.getPerson().getApellidoMaterno());
        person.setCelular(dto.getPerson().getCelular());
        person = personRepository.save(person);
        
        // 2. Crear Paciente
        Paciente paciente = new Paciente();
        paciente.setPerson(person);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEdad(dto.getEdad());
        paciente.setGenero(dto.getGenero());
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setTipoPaciente(1);
        paciente = pacienteRepository.save(paciente);
        
        // 3. Crear PacienteUniversitario
        PacienteUniversitario pu = new PacienteUniversitario();
        pu.setPaciente(paciente);
        pu.setSemestre(dto.getSemestre());
        pu.setDerivadoPor(dto.getDerivadoPor());

        // 4. Asignar psicólogo
        if (dto.getPsicologoId() != null) {
            psicologoRepository.findById(dto.getPsicologoId())
                    .ifPresent(pu::setPsicologo);
        }
        
        return pacienteUniversitarioRepository.save(pu);
    }
}