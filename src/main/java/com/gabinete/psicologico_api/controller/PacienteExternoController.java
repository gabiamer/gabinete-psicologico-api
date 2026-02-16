// src/main/java/com/gabinete/psicologico_api/controller/PacienteExternoController.java
package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.PacienteExternoDTO;
import com.gabinete.psicologico_api.model.PacienteExterno;
import com.gabinete.psicologico_api.service.PacienteExternoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes-externos")
@CrossOrigin(origins = "http://localhost:3000")
public class PacienteExternoController {

    @Autowired
    private PacienteExternoService pacienteExternoService;

    @PostMapping
    public ResponseEntity<?> crearPacienteExterno(@RequestBody PacienteExternoDTO dto) {
        try {
            PacienteExterno paciente = pacienteExternoService.crearPacienteExterno(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paciente externo creado exitosamente");
            response.put("data", paciente);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPaciente(@PathVariable Long id) {
        try {
            PacienteExterno paciente = pacienteExternoService.obtenerPorId(id);
            
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

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPacientes(@RequestParam String q) {
        try {
            List<PacienteExterno> pacientes = pacienteExternoService.buscarPacientes(q);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pacientes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}