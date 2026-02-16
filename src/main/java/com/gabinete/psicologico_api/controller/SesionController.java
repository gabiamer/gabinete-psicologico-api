// src/main/java/com/gabinete/psicologico_api/controller/SesionController.java
package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.SesionPaciente;
import com.gabinete.psicologico_api.repository.SesionPacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SesionController {

    @Autowired
    private SesionPacienteRepository sesionPacienteRepository;

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
            // Por ahora retornar un mensaje simulado
            // Implementarás la lógica completa después
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sesión creada exitosamente");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
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