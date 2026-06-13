//src/main/java/com/gabinete/psicologico_api/controller/PacienteController.java
package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.EntrevistaCompletaDTO;
import com.gabinete.psicologico_api.dto.PacienteUniversitarioDTO;
import com.gabinete.psicologico_api.model.HistorialClinico;
import com.gabinete.psicologico_api.model.PacienteUniversitario;
import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.model.SesionPaciente;
import com.gabinete.psicologico_api.repository.EntrevistaPsicologicaRepository;
import com.gabinete.psicologico_api.repository.HistorialClinicoRepository;
import com.gabinete.psicologico_api.repository.PacienteUniversitarioRepository;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import com.gabinete.psicologico_api.repository.SesionPacienteRepository;
import com.gabinete.psicologico_api.security.SecurityUtils;
import com.gabinete.psicologico_api.service.PacienteService;
import com.gabinete.psicologico_api.service.ResumenIAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gabinete.psicologico_api.dto.AntecedentesDTO;
import com.gabinete.psicologico_api.model.EntrevistaPsicologica;
import com.gabinete.psicologico_api.service.EntrevistaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private EntrevistaService entrevistaService;

    @Autowired
    private ResumenIAService resumenIAService;

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private EntrevistaPsicologicaRepository entrevistaPsicologicaRepository;

    // Endpoint de búsqueda combinada (por término y/o fecha)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPacientes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fecha) {
        try {
            Long psicologoId = SecurityUtils.getCurrentPsicologoId();
            List<PacienteUniversitario> pacientes;

            if (fecha != null && !fecha.trim().isEmpty()) {
                LocalDate fechaBusqueda = LocalDate.parse(fecha);
                if (q != null && !q.trim().isEmpty()) {
                    pacientes = psicologoId == null
                            ? pacienteService.buscarPorTerminoYFecha(q, fechaBusqueda)
                            : pacienteUniversitarioRepository.buscarPorTerminoYFechaYPsicologo(q, fechaBusqueda, psicologoId);
                } else {
                    pacientes = psicologoId == null
                            ? pacienteService.buscarPorFecha(fechaBusqueda)
                            : pacienteUniversitarioRepository.buscarPorFechaSesionYPsicologo(fechaBusqueda, psicologoId);
                }
            } else if (q != null && !q.trim().isEmpty()) {
                pacientes = psicologoId == null
                        ? pacienteService.buscarPacientes(q)
                        : pacienteUniversitarioRepository.buscarPorTerminoYPsicologo(q, psicologoId);
            } else {
                pacientes = List.of();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pacientes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al buscar pacientes: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Obtiene paciente por ID (para el frontend)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPaciente(@PathVariable Long id) {
        try {
            PacienteUniversitario paciente = pacienteService.obtenerPorId(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", paciente);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Paciente no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // CREATE - Crear paciente universitario
    @PostMapping("/universitario")
    public ResponseEntity<Map<String, Object>> crearPacienteUniversitario(
            @RequestBody PacienteUniversitarioDTO dto) {
        
        try {
            PacienteUniversitario paciente = pacienteService.crearPacienteUniversitario(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paciente creado exitosamente");
            response.put("data", paciente);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear paciente: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // READ - Obtener todos los pacientes universitarios
    @GetMapping("/universitario")
    public ResponseEntity<Map<String, Object>> obtenerTodosPacientes() {
        try {
            Long psicologoId = SecurityUtils.getCurrentPsicologoId();
            List<PacienteUniversitario> pacientes = psicologoId != null
                    ? pacienteUniversitarioRepository.findByPsicologoId(psicologoId)
                    : pacienteUniversitarioRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pacientes);
            response.put("total", pacientes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener pacientes: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // READ - Obtener un paciente por ID
    @GetMapping("/universitario/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPacientePorId(@PathVariable Long id) {
        try {
            PacienteUniversitario paciente = pacienteUniversitarioRepository.findById(id)
                    .orElse(null);
            
            if (paciente == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Paciente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", paciente);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener paciente: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // DELETE - Eliminar paciente universitario y todos sus datos relacionados
    @DeleteMapping("/universitario/{id}")
    public ResponseEntity<Map<String, Object>> eliminarPaciente(@PathVariable Long id) {
        try {
            pacienteService.eliminarPacienteCompleto(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paciente eliminado exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar paciente: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Actualizar paciente universitario
    @PutMapping("/universitario/{id}")
    public ResponseEntity<Map<String, Object>> actualizarPacienteUniversitario(
            @PathVariable Long id,
            @RequestBody PacienteUniversitarioDTO dto) {
        
        try {
            PacienteUniversitario paciente = pacienteService.actualizarPacienteUniversitario(id, dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paciente actualizado exitosamente");
            response.put("data", paciente);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar paciente: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // ACTUALIZAR SITUACION DEL CASO
    @PatchMapping("/universitario/{id}/situacion")
    public ResponseEntity<Map<String, Object>> actualizarSituacion(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            PacienteUniversitario pu = pacienteUniversitarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            pu.setSituacionCaso(body.get("situacionCaso"));
            pacienteUniversitarioRepository.save(pu);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("situacionCaso", pu.getSituacionCaso());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar situacion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ACTUALIZAR DESCRIPCION Y PROBLEMATICA MANUALMENTE
    @PatchMapping("/universitario/{id}/textos")
    public ResponseEntity<Map<String, Object>> actualizarTextos(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            PacienteUniversitario pu = pacienteUniversitarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            if (body.containsKey("descripcion"))           pu.setDescripcion(body.get("descripcion"));
            if (body.containsKey("principalProblematica")) pu.setPrincipalProblematica(body.get("principalProblematica"));
            pacienteUniversitarioRepository.save(pu);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("descripcion", pu.getDescripcion());
            response.put("principalProblematica", pu.getPrincipalProblematica());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar textos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // GENERAR RESUMEN CON IA
    @PostMapping("/universitario/{id}/generar-resumen")
    public ResponseEntity<Map<String, Object>> generarResumenIA(@PathVariable Long id) {
        try {
            Map<String, String> resumen = resumenIAService.generarResumen(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("descripcion", resumen.get("descripcion"));
            response.put("principalProblematica", resumen.get("principalProblematica"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al generar resumen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // EXTRAER PROBLEMÁTICAS FRECUENTES CON IA
    @PostMapping("/problematicas-frecuentes")
    public ResponseEntity<Map<String, Object>> extraerProblematicasFrecuentes(@RequestBody List<String> problematicas) {
        try {
            List<String> resultado = resumenIAService.extraerProblematicasFrecuentes(problematicas);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resultado);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al extraer problemáticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ── ENDPOINT UNIFICADO: crea paciente + entrevista en una sola llamada ──
    @PostMapping("/entrevista-completa")
    public ResponseEntity<Map<String, Object>> crearEntrevistaCompleta(
            @RequestBody EntrevistaCompletaDTO dto) {
        try {
            PacienteUniversitario pu = pacienteService.crearEntrevistaCompleta(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entrevista registrada exitosamente");
            response.put("pacienteId", pu.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al registrar entrevista: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Endpoint de pacientes funcionando");
        return ResponseEntity.ok(response);
    }

    // TRANSFERIR PACIENTE a otro psicologo
    @PostMapping("/universitario/{id}/transferir")
    public ResponseEntity<Map<String, Object>> transferirPaciente(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long nuevoPsicologoId = body.get("nuevoPsicologoId");
            if (nuevoPsicologoId == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "nuevoPsicologoId es requerido");
                return ResponseEntity.badRequest().body(err);
            }

            PacienteUniversitario pu = pacienteUniversitarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            // Verificar ownership (solo el psicologo asignado o admin puede transferir)
            Long currentPsicologoId = SecurityUtils.getCurrentPsicologoId();
            if (currentPsicologoId != null) {
                Long asignado = pu.getPsicologo() != null ? pu.getPsicologo().getId() : null;
                if (!currentPsicologoId.equals(asignado)) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("success", false);
                    err.put("message", "No autorizado para transferir este paciente");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
                }
            }

            Psicologo nuevoPsicologo = psicologoRepository.findById(nuevoPsicologoId)
                    .orElseThrow(() -> new RuntimeException("Psicologo destino no encontrado"));

            pu.setPsicologo(nuevoPsicologo);
            pacienteUniversitarioRepository.save(pu);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paciente transferido exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al transferir paciente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // PSICOLOGO DE LA ULTIMA SESION
    @GetMapping("/universitario/{id}/psicologo-ultima-sesion")
    public ResponseEntity<Map<String, Object>> psicologoUltimaSesion(@PathVariable Long id) {
        try {
            Map<String, Object> response = new HashMap<>();
            sesionPacienteRepository.findTopByPacienteUniversitarioIdOrderByFechaDesc(id)
                    .ifPresentOrElse(sesion -> {
                        if (sesion.getPsicologo() != null) {
                            response.put("success", true);
                            response.put("psicologoId", sesion.getPsicologo().getId());
                            response.put("nombre", sesion.getPsicologo().getPerson().getPrimerNombre()
                                    + " " + sesion.getPsicologo().getPerson().getApellidoPaterno());
                            response.put("ocupacion", sesion.getPsicologo().getOcupacion());
                        } else {
                            response.put("success", true);
                            response.put("psicologoId", null);
                            response.put("nombre", "Sin asignar");
                        }
                    }, () -> {
                        response.put("success", true);
                        response.put("psicologoId", null);
                        response.put("nombre", "Sin sesiones");
                    });
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    // REPORTE INDIVIDUAL UNIFICADO — devuelve datos personales + sesiones + historial en una sola llamada
    @GetMapping("/universitario/{id}/reporte")
    public ResponseEntity<Map<String, Object>> obtenerReporte(
            @PathVariable Long id,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        try {
            PacienteUniversitario pu = pacienteUniversitarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            // Obtener sesiones — filtradas por rango si se proporcionan
            List<SesionPaciente> sesiones;
            if (desde != null && hasta != null) {
                LocalDateTime ini = LocalDateTime.parse(desde + "T00:00:00");
                LocalDateTime fin = LocalDateTime.parse(hasta + "T23:59:59");
                sesiones = sesionPacienteRepository
                        .findByPacienteUniversitarioIdAndFechaBetweenOrderByFechaAsc(id, ini, fin);
            } else {
                sesiones = sesionPacienteRepository.findByPacienteUniversitarioId(id);
                sesiones.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
            }

            // Para la entrevista: buscar en TODAS las sesiones del paciente (no solo las del rango)
            List<SesionPaciente> todasLasSesiones = sesionPacienteRepository.findByPacienteUniversitarioId(id);
            todasLasSesiones.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));

            // Buscar entrevista en la primera sesión con entrevista (histórico completo)
            EntrevistaPsicologica entrevista = null;
            for (SesionPaciente s : todasLasSesiones) {
                Optional<EntrevistaPsicologica> ep = entrevistaPsicologicaRepository.findBySesionPacienteId(s.getId());
                if (ep.isPresent()) {
                    entrevista = ep.get();
                    break;
                }
            }

            // Mapear sesiones del período con su historial clínico
            List<Map<String, Object>> sesionesMapeadas = new java.util.ArrayList<>();
            for (SesionPaciente s : sesiones) {
                Optional<HistorialClinico> hOpt = historialClinicoRepository.findBySesionPacienteId(s.getId());
                Map<String, Object> sesionMap = new HashMap<>();
                sesionMap.put("id", s.getId());
                sesionMap.put("fecha", s.getFecha());
                sesionMap.put("horaInicio", s.getHoraInicio());
                sesionMap.put("horaFin", s.getHoraFin());
                sesionMap.put("duracionMinutos", s.getDuracionMinutos());
                sesionMap.put("historialClinico", hOpt.orElse(null));
                sesionesMapeadas.add(sesionMap);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("pacienteUniversitario", pu);
            data.put("entrevista", entrevista);
            data.put("sesiones", sesionesMapeadas);

            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // GUARDAR ANTECEDENTES
    @PostMapping("/universitario/{id}/historia-clinica")
    public ResponseEntity<Map<String, Object>> guardarHistoriaClinica(
            @PathVariable Long id,
            @RequestBody AntecedentesDTO antecedentes) {
        
        try {
            Long entrevistaId = entrevistaService.guardarAntecedentes(id, antecedentes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Antecedentes guardados exitosamente");
            response.put("entrevistaId", entrevistaId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}