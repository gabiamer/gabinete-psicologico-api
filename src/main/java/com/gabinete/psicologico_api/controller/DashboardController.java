package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

    @Autowired
    private PacienteExternoRepository pacienteExternoRepository;

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private EstudianteCarreraRepository estudianteCarreraRepository;

    @Autowired
    private OrientacionVocacionalRepository orientacionVocacionalRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @GetMapping("/entrevistas")
    public ResponseEntity<?> obtenerEntrevistas() {
        try {
            List<PacienteUniversitario> pacientes = pacienteUniversitarioRepository.findAll();
            List<Map<String, Object>> resultado = new ArrayList<>();

            for (PacienteUniversitario pu : pacientes) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("pacienteUniversitarioId", pu.getId());

                // Nombre formateado: K. Guzman Marca
                Person person = pu.getPaciente().getPerson();
                String inicial = person.getPrimerNombre() != null && !person.getPrimerNombre().isEmpty()
                        ? person.getPrimerNombre().substring(0, 1).toUpperCase() + "."
                        : "";
                String apellidos = "";
                if (person.getApellidoPaterno() != null) apellidos += person.getApellidoPaterno();
                if (person.getApellidoMaterno() != null) apellidos += " " + person.getApellidoMaterno();
                fila.put("estudianteNombre", (inicial + " " + apellidos).trim());
                fila.put("nombreCompleto", ((person.getPrimerNombre() != null ? person.getPrimerNombre() : "") + " " +
                        (person.getSegundoNombre() != null ? person.getSegundoNombre() : "") + " " +
                        (person.getApellidoPaterno() != null ? person.getApellidoPaterno() : "") + " " +
                        (person.getApellidoMaterno() != null ? person.getApellidoMaterno() : "")).trim());

                // Derivado por
                fila.put("derivadoPor", pu.getDerivadoPor());

                // Resumen IA
                fila.put("descripcion", pu.getDescripcion());
                fila.put("principalProblematica", pu.getPrincipalProblematica());

                // Situacion del caso
                fila.put("situacionCaso", pu.getSituacionCaso() != null ? pu.getSituacionCaso() : "Acompañamiento psicológico");

                // Carrera
                Optional<EstudianteCarrera> ec = estudianteCarreraRepository.findByPacienteUniversitarioId(pu.getId());
                fila.put("carrera", ec.map(e -> e.getCarrera().getCarrera()).orElse("Sin carrera"));

                // Psicologo
                if (pu.getPsicologo() != null) {
                    Person psicPerson = pu.getPsicologo().getPerson();
                    fila.put("psicologoId", pu.getPsicologo().getId());
                    fila.put("psicologoNombre", (psicPerson.getPrimerNombre() != null ? psicPerson.getPrimerNombre() : "") + " " +
                            (psicPerson.getApellidoPaterno() != null ? psicPerson.getApellidoPaterno() : ""));
                } else {
                    fila.put("psicologoId", null);
                    fila.put("psicologoNombre", "Sin asignar");
                }

                // Sesiones
                List<SesionPaciente> sesiones = sesionPacienteRepository.findByPacienteUniversitarioId(pu.getId());
                fila.put("numeroSesiones", sesiones.size());

                // Fecha de ultima sesion
                if (!sesiones.isEmpty()) {
                    sesiones.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
                    fila.put("ultimaSesionFecha", sesiones.get(0).getFecha());
                } else {
                    fila.put("ultimaSesionFecha", null);
                }

                // Gravedad (ultimo historial clinico)
                String gravedad = "Sin evaluar";
                if (!sesiones.isEmpty()) {
                    for (SesionPaciente sesion : sesiones) {
                        Optional<HistorialClinico> hc = historialClinicoRepository.findBySesionPacienteId(sesion.getId());
                        if (hc.isPresent() && hc.get().getGravedad() != null) {
                            gravedad = hc.get().getGravedad();
                            break;
                        }
                    }
                }
                fila.put("gravedad", gravedad);

                resultado.add(fila);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resultado);
            response.put("total", resultado.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener entrevistas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/orientaciones")
    public ResponseEntity<?> obtenerOrientaciones() {
        try {
            List<PacienteExterno> pacientes = pacienteExternoRepository.findAll();
            List<Map<String, Object>> resultado = new ArrayList<>();

            for (PacienteExterno pe : pacientes) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("pacienteExternoId", pe.getId());

                // Nombre formateado
                Person person = pe.getPaciente().getPerson();
                String inicial = person.getPrimerNombre() != null && !person.getPrimerNombre().isEmpty()
                        ? person.getPrimerNombre().substring(0, 1).toUpperCase() + "."
                        : "";
                String apellidos = "";
                if (person.getApellidoPaterno() != null) apellidos += person.getApellidoPaterno();
                if (person.getApellidoMaterno() != null) apellidos += " " + person.getApellidoMaterno();
                fila.put("estudianteNombre", (inicial + " " + apellidos).trim());
                fila.put("nombreCompleto", ((person.getPrimerNombre() != null ? person.getPrimerNombre() : "") + " " +
                        (person.getSegundoNombre() != null ? person.getSegundoNombre() : "") + " " +
                        (person.getApellidoPaterno() != null ? person.getApellidoPaterno() : "") + " " +
                        (person.getApellidoMaterno() != null ? person.getApellidoMaterno() : "")).trim());

                fila.put("escuela", pe.getEscuela());
                fila.put("anio", pe.getAnio());
                fila.put("correo", pe.getCorreo());
                fila.put("edad", pe.getPaciente().getEdad());

                // Orientaciones
                List<OrientacionVocacional> orientaciones = orientacionVocacionalRepository.findByPacienteExternoId(pe.getId());
                fila.put("numeroOrientaciones", orientaciones.size());

                if (!orientaciones.isEmpty()) {
                    orientaciones.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
                    fila.put("ultimaOrientacionFecha", orientaciones.get(0).getFecha());
                } else {
                    fila.put("ultimaOrientacionFecha", null);
                }

                resultado.add(fila);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resultado);
            response.put("total", resultado.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener orientaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> stats = new HashMap<>();

            long totalUniversitarios = pacienteUniversitarioRepository.count();
            long totalExternos = pacienteExternoRepository.count();
            long totalSesiones = sesionPacienteRepository.count();
            long totalOrientaciones = orientacionVocacionalRepository.count();

            stats.put("totalPacientesUniversitarios", totalUniversitarios);
            stats.put("totalPacientesExternos", totalExternos);
            stats.put("totalSesiones", totalSesiones);
            stats.put("totalOrientaciones", totalOrientaciones);

            // Gravedad distribution
            List<HistorialClinico> historiales = historialClinicoRepository.findAll();
            Map<String, Long> gravedadCount = new HashMap<>();
            gravedadCount.put("leve", 0L);
            gravedadCount.put("moderado", 0L);
            gravedadCount.put("grave", 0L);
            for (HistorialClinico hc : historiales) {
                if (hc.getGravedad() != null && gravedadCount.containsKey(hc.getGravedad())) {
                    gravedadCount.put(hc.getGravedad(), gravedadCount.get(hc.getGravedad()) + 1);
                }
            }
            stats.put("gravedadDistribucion", gravedadCount);

            // Situacion del caso distribution
            List<PacienteUniversitario> todos = pacienteUniversitarioRepository.findAll();
            Map<String, Long> situacionCount = new LinkedHashMap<>();
            situacionCount.put("Acompañamiento psicológico", 0L);
            situacionCount.put("Buen proceso", 0L);
            situacionCount.put("Proceso terminado", 0L);
            situacionCount.put("Orientación vocacional", 0L);
            situacionCount.put("Derivado a consultorio externo", 0L);
            for (PacienteUniversitario pu : todos) {
                String sit = pu.getSituacionCaso() != null ? pu.getSituacionCaso() : "Acompañamiento psicológico";
                situacionCount.merge(sit, 1L, Long::sum);
            }
            stats.put("situacionDistribucion", situacionCount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener estadisticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
