package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.PacienteUniversitarioDTO;
import com.gabinete.psicologico_api.model.PacienteUniversitario;
import com.gabinete.psicologico_api.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:3000")
public class PacienteController {
    
    @Autowired
    private PacienteService pacienteService;
    
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
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Endpoint de pacientes funcionando");
        return ResponseEntity.ok(response);
    }
}