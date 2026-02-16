package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.OrientacionVocacionalDTO;
import com.gabinete.psicologico_api.model.OrientacionVocacional;
import com.gabinete.psicologico_api.service.OrientacionVocacionalService;
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
public class OrientacionVocacionalController {

    @Autowired
    private OrientacionVocacionalService orientacionService;

    // Guardar entrevista de orientación vocacional
    @PostMapping("/{id}/orientacion-vocacional")
    public ResponseEntity<?> guardarEntrevista(
            @PathVariable Long id,
            @RequestBody OrientacionVocacionalDTO dto) {
        
        try {
            OrientacionVocacional orientacion = orientacionService.guardarEntrevista(id, dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Entrevista de orientación vocacional guardada exitosamente");
            response.put("data", orientacion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al guardar entrevista: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Obtener todas las entrevistas de un paciente externo
    @GetMapping("/{id}/orientacion-vocacional")
    public ResponseEntity<?> obtenerEntrevistas(@PathVariable Long id) {
        try {
            List<OrientacionVocacional> entrevistas = orientacionService.obtenerEntrevistasPorPaciente(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", entrevistas);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener entrevistas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Obtener última entrevista
    @GetMapping("/{id}/orientacion-vocacional/ultima")
    public ResponseEntity<?> obtenerUltimaEntrevista(@PathVariable Long id) {
        try {
            OrientacionVocacional orientacion = orientacionService.obtenerUltimaEntrevista(id);
            
            if (orientacion == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No se encontró entrevista");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orientacion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener entrevista: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}