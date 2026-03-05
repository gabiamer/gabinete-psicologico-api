package com.gabinete.psicologico_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.model.EntrevistaPsicologica;
import com.gabinete.psicologico_api.model.HistorialClinico;
import com.gabinete.psicologico_api.model.PacienteUniversitario;
import com.gabinete.psicologico_api.model.SesionPaciente;
import com.gabinete.psicologico_api.repository.EntrevistaPsicologicaRepository;
import com.gabinete.psicologico_api.repository.HistorialClinicoRepository;
import com.gabinete.psicologico_api.repository.PacienteUniversitarioRepository;
import com.gabinete.psicologico_api.repository.SesionPacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SesionController {

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

    @Autowired
    private EntrevistaPsicologicaRepository entrevistaPsicologicaRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Obtener sesiones de un paciente
    @GetMapping("/pacientes/{id}/sesiones")
    public ResponseEntity<?> obtenerSesionesPorPaciente(@PathVariable Long id) {
        try {
            List<SesionPaciente> sesiones = sesionPacienteRepository.findByPacienteUniversitarioId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", sesiones);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener sesiones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Crear nueva sesion de seguimiento
    @PostMapping("/pacientes/{id}/sesiones")
    public ResponseEntity<?> crearSesion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> sesionData) {

        try {
            PacienteUniversitario paciente = pacienteUniversitarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));

            SesionPaciente sesion = new SesionPaciente();
            sesion.setPacienteUniversitario(paciente);
            sesion.setPsicologo(paciente.getPsicologo());

            // Parsear fecha
            String fechaStr = (String) sesionData.get("fecha");
            if (fechaStr != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    sesion.setFecha(LocalDateTime.parse(fechaStr, formatter));
                } catch (Exception e) {
                    sesion.setFecha(LocalDateTime.now());
                }
            } else {
                sesion.setFecha(LocalDateTime.now());
            }

            sesion = sesionPacienteRepository.save(sesion);

            // Crear historial clinico para esta sesion
            @SuppressWarnings("unchecked")
            Map<String, Object> historialData = (Map<String, Object>) sesionData.get("historialClinico");
            if (historialData != null) {
                HistorialClinico historial = new HistorialClinico();
                historial.setSesionPaciente(sesion);

                Object nroSesionObj = historialData.get("nroSesion");
                if (nroSesionObj instanceof Number) {
                    historial.setNroSesion(((Number) nroSesionObj).intValue());
                }

                historial.setHistoria((String) historialData.get("historia"));
                historial.setGravedad((String) historialData.get("gravedad"));

                Object tipologiasObj = historialData.get("tipologias");
                if (tipologiasObj != null) {
                    historial.setTipologia(objectMapper.writeValueAsString(tipologiasObj));
                }

                historialClinicoRepository.save(historial);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sesion creada exitosamente");
            response.put("data", sesion);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear sesion: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Obtener sesion por ID
    @GetMapping("/sesiones/{id}")
    public ResponseEntity<?> obtenerSesion(@PathVariable Long id) {
        try {
            SesionPaciente sesion = sesionPacienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sesion no encontrada"));

            Map<String, Object> sesionMap = new HashMap<>();
            sesionMap.put("id", sesion.getId());
            sesionMap.put("fecha", sesion.getFecha());
            sesionMap.put("psicologo", sesion.getPsicologo());
            sesionMap.put("pacienteUniversitario", sesion.getPacienteUniversitario());

            // Buscar entrevista psicologica (primera sesion)
            Optional<EntrevistaPsicologica> entrevista =
                    entrevistaPsicologicaRepository.findBySesionPacienteId(id);
            if (entrevista.isPresent()) {
                sesionMap.put("entrevista", entrevista.get());
            }

            // Buscar historial clinico
            Optional<HistorialClinico> historial =
                    historialClinicoRepository.findBySesionPacienteId(id);
            if (historial.isPresent()) {
                sesionMap.put("historialClinico", historial.get());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", sesionMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Sesion no encontrada: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
