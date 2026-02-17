//src/main/java/com/gabinete/psicologico_api/service/PacienteService.java

package com.gabinete.psicologico_api.service;

import com.gabinete.psicologico_api.dto.PacienteUniversitarioDTO;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private EstudianteCarreraRepository estudianteCarreraRepository;
    
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
        
        // Guardar primero el PacienteUniversitario
        pu = pacienteUniversitarioRepository.save(pu);
        
        // 5. Guardar relación estudiante-carrera
        if (dto.getCarreraId() != null) {
            Carrera carrera = carreraRepository.findById(dto.getCarreraId())
                    .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
            
            EstudianteCarrera ec = new EstudianteCarrera();
            ec.setPacienteUniversitario(pu);
            ec.setCarrera(carrera);
            estudianteCarreraRepository.save(ec);
        }
    
        return pu;
    }

    @Transactional
    public PacienteUniversitario actualizarPacienteUniversitario(Long id, PacienteUniversitarioDTO dto) {
        System.out.println("=== Actualizando Paciente Universitario ID: " + id);
        
        // Buscar el paciente universitario existente
        PacienteUniversitario pu = pacienteUniversitarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente universitario no encontrado: " + id));
        
        // Actualizar Person
        Person person = pu.getPaciente().getPerson();
        person.setPrimerNombre(dto.getPerson().getPrimerNombre());
        person.setSegundoNombre(dto.getPerson().getSegundoNombre());
        person.setApellidoPaterno(dto.getPerson().getApellidoPaterno());
        person.setApellidoMaterno(dto.getPerson().getApellidoMaterno());
        person.setCelular(dto.getPerson().getCelular());
        personRepository.save(person);
        
        // Actualizar Paciente
        Paciente paciente = pu.getPaciente();
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEdad(dto.getEdad());
        paciente.setGenero(dto.getGenero());
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        pacienteRepository.save(paciente);
        
        // Actualizar PacienteUniversitario
        pu.setSemestre(dto.getSemestre());
        pu.setDerivadoPor(dto.getDerivadoPor());
        
        // Actualizar psicólogo si cambió
        if (dto.getPsicologoId() != null) {
            psicologoRepository.findById(dto.getPsicologoId())
                    .ifPresent(pu::setPsicologo);
        }
        
        PacienteUniversitario actualizado = pacienteUniversitarioRepository.save(pu);
        System.out.println("=== Paciente actualizado correctamente");
        
        return actualizado;
    }

    public List<PacienteUniversitario> buscarPacientes(String termino) {
        return pacienteUniversitarioRepository.buscarPorTermino(termino);
    }

    public List<PacienteUniversitario> buscarPorFecha(LocalDate fecha) {
        return pacienteUniversitarioRepository.buscarPorFechaSesion(fecha);
    }

    public List<PacienteUniversitario> buscarPorTerminoYFecha(String termino, LocalDate fecha) {
        return pacienteUniversitarioRepository.buscarPorTerminoYFecha(termino, fecha);
    }

    public PacienteUniversitario obtenerPorId(Long id) {
        return pacienteUniversitarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
    }
}