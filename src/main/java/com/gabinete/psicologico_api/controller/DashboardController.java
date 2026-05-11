package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import com.gabinete.psicologico_api.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            Long psicologoId = SecurityUtils.getCurrentPsicologoId();
            List<PacienteUniversitario> pacientes = psicologoId == null
                    ? pacienteUniversitarioRepository.findAll()
                    : pacienteUniversitarioRepository.findByPsicologoId(psicologoId);
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

    /**
     * Entrevistas filtradas por período: incluye pacientes que tuvieron AL MENOS UNA sesión
     * en [desde, hasta]. numeroSesiones = sesiones en ese período (no el total acumulado).
     */
    @GetMapping("/entrevistas-periodo")
    public ResponseEntity<?> obtenerEntrevistasPeriodo(
            @RequestParam String desde,
            @RequestParam String hasta) {
        try {
            Long psicologoId = SecurityUtils.getCurrentPsicologoId();
            LocalDateTime ini = LocalDate.parse(desde).atStartOfDay();
            LocalDateTime fin = LocalDate.parse(hasta).atTime(23, 59, 59);

            // Sesiones en el período
            List<SesionPaciente> sesionesEnPeriodo = psicologoId == null
                    ? sesionPacienteRepository.findByFechaBetween(ini, fin)
                    : sesionPacienteRepository.findByFechaBetweenAndPsicologoId(ini, fin, psicologoId);

            // Agrupar por paciente universitario
            Map<Long, List<SesionPaciente>> porPaciente = new LinkedHashMap<>();
            for (SesionPaciente s : sesionesEnPeriodo) {
                if (s.getPacienteUniversitario() == null) continue;
                porPaciente.computeIfAbsent(s.getPacienteUniversitario().getId(), k -> new ArrayList<>()).add(s);
            }

            List<Map<String, Object>> resultado = new ArrayList<>();

            for (Map.Entry<Long, List<SesionPaciente>> entry : porPaciente.entrySet()) {
                Long puId = entry.getKey();
                List<SesionPaciente> sesionesPac = entry.getValue();

                Optional<PacienteUniversitario> puOpt = pacienteUniversitarioRepository.findById(puId);
                if (puOpt.isEmpty()) continue;
                PacienteUniversitario pu = puOpt.get();

                Map<String, Object> fila = new HashMap<>();
                fila.put("pacienteUniversitarioId", pu.getId());

                Person person = pu.getPaciente().getPerson();
                String inicial = person.getPrimerNombre() != null && !person.getPrimerNombre().isEmpty()
                        ? person.getPrimerNombre().substring(0, 1).toUpperCase() + "." : "";
                String apellidos = "";
                if (person.getApellidoPaterno() != null) apellidos += person.getApellidoPaterno();
                if (person.getApellidoMaterno() != null) apellidos += " " + person.getApellidoMaterno();
                fila.put("estudianteNombre", (inicial + " " + apellidos).trim());
                fila.put("nombreCompleto", ((person.getPrimerNombre() != null ? person.getPrimerNombre() : "") + " " +
                        (person.getSegundoNombre() != null ? person.getSegundoNombre() : "") + " " +
                        (person.getApellidoPaterno() != null ? person.getApellidoPaterno() : "") + " " +
                        (person.getApellidoMaterno() != null ? person.getApellidoMaterno() : "")).trim());

                fila.put("derivadoPor", pu.getDerivadoPor());
                fila.put("descripcion", pu.getDescripcion());
                fila.put("principalProblematica", pu.getPrincipalProblematica());
                fila.put("situacionCaso", pu.getSituacionCaso() != null ? pu.getSituacionCaso() : "Acompañamiento psicológico");

                Optional<EstudianteCarrera> ec = estudianteCarreraRepository.findByPacienteUniversitarioId(puId);
                fila.put("carrera", ec.map(e -> e.getCarrera().getCarrera()).orElse("Sin carrera"));

                if (pu.getPsicologo() != null) {
                    Person psicPerson = pu.getPsicologo().getPerson();
                    fila.put("psicologoId", pu.getPsicologo().getId());
                    fila.put("psicologoNombre", (psicPerson.getPrimerNombre() != null ? psicPerson.getPrimerNombre() : "") + " " +
                            (psicPerson.getApellidoPaterno() != null ? psicPerson.getApellidoPaterno() : ""));
                } else {
                    fila.put("psicologoId", null);
                    fila.put("psicologoNombre", "Sin asignar");
                }

                // Sesiones en el período (no total acumulado)
                fila.put("numeroSesiones", sesionesPac.size());

                // Última sesión dentro del período
                sesionesPac.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
                fila.put("ultimaSesionFecha", sesionesPac.get(0).getFecha());

                // Gravedad (historial más reciente del período)
                String gravedad = "Sin evaluar";
                for (SesionPaciente s : sesionesPac) {
                    Optional<HistorialClinico> hc = historialClinicoRepository.findBySesionPacienteId(s.getId());
                    if (hc.isPresent() && hc.get().getGravedad() != null) {
                        gravedad = hc.get().getGravedad();
                        break;
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
            error.put("message", "Error al obtener entrevistas por período: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/orientaciones")
    public ResponseEntity<?> obtenerOrientaciones() {
        try {
            Long psicologoId = SecurityUtils.getCurrentPsicologoId();
            List<PacienteExterno> pacientes = psicologoId == null
                    ? pacienteExternoRepository.findAll()
                    : pacienteExternoRepository.findByPsicologoId(psicologoId);
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
            Long psicologoId = SecurityUtils.getCurrentPsicologoId();
            Map<String, Object> stats = new HashMap<>();

            List<PacienteUniversitario> universitarios = psicologoId == null
                    ? pacienteUniversitarioRepository.findAll()
                    : pacienteUniversitarioRepository.findByPsicologoId(psicologoId);
            List<PacienteExterno> externos = psicologoId == null
                    ? pacienteExternoRepository.findAll()
                    : pacienteExternoRepository.findByPsicologoId(psicologoId);

            stats.put("totalPacientesUniversitarios", (long) universitarios.size());
            stats.put("totalPacientesExternos", (long) externos.size());

            // Sesiones del psicologo
            long totalSesiones = psicologoId == null
                    ? sesionPacienteRepository.count()
                    : sesionPacienteRepository.findByPacienteUniversitarioPsicologoId(psicologoId).size();
            stats.put("totalSesiones", totalSesiones);
            stats.put("totalOrientaciones", (long) externos.stream()
                    .mapToLong(pe -> orientacionVocacionalRepository.findByPacienteExternoId(pe.getId()).size())
                    .sum());

            // Gravedad distribution
            Set<Long> puIds = universitarios.stream().map(PacienteUniversitario::getId).collect(Collectors.toSet());
            List<HistorialClinico> historiales = historialClinicoRepository.findAll();
            Map<String, Long> gravedadCount = new HashMap<>();
            gravedadCount.put("leve", 0L);
            gravedadCount.put("moderado", 0L);
            gravedadCount.put("grave", 0L);
            for (HistorialClinico hc : historiales) {
                if (hc.getGravedad() == null || !gravedadCount.containsKey(hc.getGravedad())) continue;
                try {
                    Long puId = hc.getSesionPaciente().getPacienteUniversitario().getId();
                    if (psicologoId != null && !puIds.contains(puId)) continue;
                } catch (Exception ignored) { continue; }
                gravedadCount.put(hc.getGravedad(), gravedadCount.get(hc.getGravedad()) + 1);
            }
            stats.put("gravedadDistribucion", gravedadCount);

            // Situacion del caso distribution
            List<PacienteUniversitario> todos = universitarios;
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
