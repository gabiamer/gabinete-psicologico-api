//src/main/java/com/gabinete/psicologico_api/service/PacienteService.java

package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.dto.EntrevistaCompletaDTO;
import com.gabinete.psicologico_api.dto.PacienteUniversitarioDTO;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.model.SesionPaciente;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

    @Autowired
    private EntrevistaPsicologicaRepository entrevistaPsicologicaRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;
    
    @Transactional
    public PacienteUniversitario crearPacienteUniversitario(PacienteUniversitarioDTO dto) {
        
        // VALIDACIÓN: Al menos un apellido es obligatorio
        if ((dto.getPerson().getApellidoPaterno() == null || dto.getPerson().getApellidoPaterno().trim().isEmpty()) 
            && (dto.getPerson().getApellidoMaterno() == null || dto.getPerson().getApellidoMaterno().trim().isEmpty())) {
            throw new RuntimeException("Debe proporcionar al menos un apellido (paterno o materno)");
        }
            
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
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setGenero(dto.getGenero());
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
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setGenero(dto.getGenero());
        pacienteRepository.save(paciente);
        
        // Actualizar PacienteUniversitario
        pu.setSemestre(dto.getSemestre());
        pu.setDerivadoPor(dto.getDerivadoPor());
        
        // Actualizar psicólogo si cambió
        if (dto.getPsicologoId() != null) {
            psicologoRepository.findById(dto.getPsicologoId())
                    .ifPresent(pu::setPsicologo);
        }
        
        pu = pacienteUniversitarioRepository.save(pu);

        // Actualizar carrera si se proporcionó
        if (dto.getCarreraId() != null) {
            Carrera carrera = carreraRepository.findById(dto.getCarreraId())
                    .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
            EstudianteCarrera ec = estudianteCarreraRepository
                    .findByPacienteUniversitarioId(pu.getId())
                    .orElse(new EstudianteCarrera());
            ec.setPacienteUniversitario(pu);
            ec.setCarrera(carrera);
            estudianteCarreraRepository.save(ec);
        }

        System.out.println("=== Paciente actualizado correctamente");
        return pu;
    }

    @Transactional
    public void eliminarPacienteCompleto(Long id) {
        PacienteUniversitario pu = pacienteUniversitarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));

        Paciente paciente = pu.getPaciente();
        Person person = paciente.getPerson();

        // 1. Eliminar HistorialClinico y EntrevistaPsicologica por cada sesion
        List<SesionPaciente> sesiones = sesionPacienteRepository.findByPacienteUniversitarioId(id);
        for (SesionPaciente sesion : sesiones) {
            historialClinicoRepository.findBySesionPacienteId(sesion.getId())
                    .ifPresent(historialClinicoRepository::delete);
            entrevistaPsicologicaRepository.findBySesionPacienteId(sesion.getId())
                    .ifPresent(entrevistaPsicologicaRepository::delete);
        }

        // 2. Eliminar sesiones
        sesionPacienteRepository.deleteAll(sesiones);

        // 3. Eliminar relacion estudiante-carrera
        estudianteCarreraRepository.findByPacienteUniversitarioId(id)
                .ifPresent(estudianteCarreraRepository::delete);

        // 4. Eliminar PacienteUniversitario
        pacienteUniversitarioRepository.delete(pu);

        // 5. Eliminar Paciente y Person
        pacienteRepository.delete(paciente);
        personRepository.delete(person);
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

    /** Crea paciente + sesión + entrevista psicológica + historial clínico en una sola transacción */
    @Transactional
    public PacienteUniversitario crearEntrevistaCompleta(EntrevistaCompletaDTO dto) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // ── 1. Person ─────────────────────────────────────────────────────────
        Person person = new Person();
        person.setPrimerNombre(dto.getPerson().getPrimerNombre());
        person.setSegundoNombre(dto.getPerson().getSegundoNombre());
        person.setApellidoPaterno(dto.getPerson().getApellidoPaterno());
        person.setApellidoMaterno(dto.getPerson().getApellidoMaterno());
        person.setCelular(dto.getPerson().getCelular());
        person = personRepository.save(person);

        // ── 2. Paciente ───────────────────────────────────────────────────────
        Paciente paciente = new Paciente();
        paciente.setPerson(person);
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEdad(dto.getEdad());
        paciente.setDomicilio(dto.getDomicilio());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setGenero(dto.getGenero());
        paciente.setTipoPaciente(1);
        paciente = pacienteRepository.save(paciente);

        // ── 3. PacienteUniversitario ──────────────────────────────────────────
        PacienteUniversitario pu = new PacienteUniversitario();
        pu.setPaciente(paciente);
        pu.setSemestre(dto.getSemestre());
        pu.setDerivadoPor(dto.getDerivadoPor());
        if (dto.getPsicologoId() != null) {
            psicologoRepository.findById(dto.getPsicologoId()).ifPresent(pu::setPsicologo);
        }
        pu = pacienteUniversitarioRepository.save(pu);

        // ── 4. EstudianteCarrera ──────────────────────────────────────────────
        if (dto.getCarreraId() != null) {
            Carrera carrera = carreraRepository.findById(dto.getCarreraId())
                    .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
            EstudianteCarrera ec = new EstudianteCarrera();
            ec.setPacienteUniversitario(pu);
            ec.setCarrera(carrera);
            estudianteCarreraRepository.save(ec);
        }

        // ── 5. SesionPaciente (primera sesión) ────────────────────────────────
        SesionPaciente sesion = new SesionPaciente();
        sesion.setPacienteUniversitario(pu);
        sesion.setPsicologo(pu.getPsicologo());
        sesion.setFecha(LocalDateTime.now());
        sesion = sesionPacienteRepository.save(sesion);

        // ── 6. Historia familiar ──────────────────────────────────────────────
        Map<String, Object> familia = new HashMap<>();
        familia.put("conQuienVive", dto.getConQuienVive());
        familia.put("personaReferencia", dto.getPersonaReferencia());
        familia.put("celularReferencia", dto.getCelularReferencia());
        Map<String, Object> padre = new HashMap<>();
        padre.put("nombre", dto.getNombrePadre()); padre.put("ocupacion", dto.getOcupacionPadre());
        padre.put("enfermedad", dto.getEnfermedadPadre()); padre.put("relacion", dto.getRelacionPadre());
        familia.put("padre", padre);
        Map<String, Object> madre = new HashMap<>();
        madre.put("nombre", dto.getNombreMadre()); madre.put("ocupacion", dto.getOcupacionMadre());
        madre.put("enfermedad", dto.getEnfermedadMadre()); madre.put("relacion", dto.getRelacionMadre());
        familia.put("madre", madre);
        Map<String, Object> hermanos = new HashMap<>();
        hermanos.put("numero", dto.getNumeroHermanos()); hermanos.put("relato", dto.getRelatoHermanos());
        familia.put("hermanos", hermanos);

        // ── 7. Universidad ────────────────────────────────────────────────────
        Map<String, Object> universidad = new HashMap<>();
        universidad.put("cambioCarreras", dto.getCambioCarreras());
        universidad.put("motivosCambio", dto.getMotivosCambio());
        universidad.put("relatoGeneral", dto.getRelatoUniversidad());

        // ── 8. Hábitos ────────────────────────────────────────────────────────
        Map<String, Object> habitos = new HashMap<>();
        Map<String, Object> alcohol = new HashMap<>();
        alcohol.put("descripcion", dto.getConsumoAlcohol()); alcohol.put("frecuencia", dto.getFrecuenciaAlcohol());
        habitos.put("alcohol", alcohol);
        Map<String, Object> tabaco = new HashMap<>();
        tabaco.put("descripcion", dto.getConsumoTabaco()); tabaco.put("frecuencia", dto.getFrecuenciaTabaco());
        habitos.put("tabaco", tabaco);
        Map<String, Object> drogas = new HashMap<>();
        drogas.put("descripcion", dto.getConsumoDrogas()); drogas.put("frecuencia", dto.getFrecuenciaDrogas());
        habitos.put("drogas", drogas);
        habitos.put("relatoAcusacionDetencion", dto.getRelatoAcusacionDetencion());

        // ── 9. Acuerdos ───────────────────────────────────────────────────────
        Map<String, Object> acuerdos = new HashMap<>();
        acuerdos.put("acuerdosEstablecidos", dto.getAcuerdosEstablecidos());
        acuerdos.put("proximaSesionFecha", dto.getProximaSesionFecha());
        acuerdos.put("proximaSesionHora", dto.getProximaSesionHora());

        // ── 10. EntrevistaPsicologica ─────────────────────────────────────────
        EntrevistaPsicologica entrevista = new EntrevistaPsicologica();
        entrevista.setSesionPaciente(sesion);
        entrevista.setAntecedentes(dto.getMotivoConsulta());
        entrevista.setHistoriaFamiliar(mapper.writeValueAsString(familia));
        entrevista.setRelatoUniversidad(mapper.writeValueAsString(universidad));
        entrevista.setSintomas(dto.getSintomas() != null ? mapper.writeValueAsString(dto.getSintomas()) : null);
        entrevista.setTotalScoreEstres(dto.getTotalScoreEstres());
        entrevista.setTotalScoreAnsiedad(dto.getTotalScoreAnsiedad());
        entrevista.setTotalScoreDepresion(dto.getTotalScoreDepresion());
        entrevista.setHabitos(mapper.writeValueAsString(habitos));
        entrevista.setAcuerdos(mapper.writeValueAsString(acuerdos));
        entrevistaPsicologicaRepository.save(entrevista);

        // ── 11. HistorialClinico ──────────────────────────────────────────────
        HistorialClinico historial = new HistorialClinico();
        historial.setSesionPaciente(sesion);
        historial.setNroSesion(1);
        historial.setHistoria(dto.getHistoriaClinica());
        historial.setGravedad(dto.getGravedad());
        if (dto.getTipologias() != null) {
            historial.setTipologia(mapper.writeValueAsString(dto.getTipologias()));
        }
        historialClinicoRepository.save(historial);

        return pu;
    }
}