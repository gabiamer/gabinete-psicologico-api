//src/main/java/com/gabinete/psicologico_api/controller/SesionController.java
package com.gabinete.psicologico_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.model.PacienteUniversitario;
import com.gabinete.psicologico_api.model.SesionPaciente;
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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SesionController {

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

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

    // Crear nueva sesión
    @PostMapping("/pacientes/{id}/sesiones")
    public ResponseEntity<?> crearSesion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> sesionData) {
        
        try {
            System.out.println("=== CREANDO NUEVA SESIÓN ===");
            System.out.println("Paciente ID: " + id);
            System.out.println("Datos recibidos: " + sesionData);
            
            // Buscar el paciente universitario
            PacienteUniversitario paciente = pacienteUniversitarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
            
            // Crear la sesión
            SesionPaciente sesion = new SesionPaciente();
            sesion.setPacienteUniversitario(paciente);
            sesion.setPsicologo(paciente.getPsicologo());
            
            // Parsear la fecha
            String fechaStr = (String) sesionData.get("fecha");
            if (fechaStr != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    sesion.setFecha(LocalDateTime.parse(fechaStr, formatter));
                } catch (Exception e) {
                    System.err.println("Error parseando fecha, usando fecha actual: " + e.getMessage());
                    sesion.setFecha(LocalDateTime.now());
                }
            } else {
                sesion.setFecha(LocalDateTime.now());
            }
            
            // Tipo de sesión
            sesion.setTipo((String) sesionData.get("tipo"));
            
            // Acuerdos - convertir a JSON string
            Object acuerdosObj = sesionData.get("acuerdos");
            if (acuerdosObj != null) {
                String acuerdosJson = objectMapper.writeValueAsString(acuerdosObj);
                sesion.setAcuerdos(acuerdosJson);
                System.out.println("Acuerdos JSON: " + acuerdosJson);
            }
            
            // Guardar en la base de datos
            sesion = sesionPacienteRepository.save(sesion);
            
            System.out.println("=== SESIÓN GUARDADA CON ID: " + sesion.getId() + " ===");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sesión creada exitosamente");
            response.put("data", sesion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("ERROR al crear sesión: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear sesión: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Obtener sesión por ID
    @GetMapping("/sesiones/{id}")
    public ResponseEntity<?> obtenerSesion(@PathVariable Long id) {
        try {
            SesionPaciente sesion = sesionPacienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", sesion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Sesión no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}