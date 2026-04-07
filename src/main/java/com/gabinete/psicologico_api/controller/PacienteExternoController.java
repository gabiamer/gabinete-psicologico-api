package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.OrientacionCompletaDTO;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPacienteExterno(@PathVariable Long id) {
        try {
            pacienteExternoService.eliminarPacienteExternoCompleto(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Paciente externo eliminado exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ── ENDPOINT UNIFICADO: crea paciente externo + orientación en una sola llamada ──
    @PostMapping("/orientacion-completa")
    public ResponseEntity<?> crearOrientacionCompleta(@RequestBody OrientacionCompletaDTO dto) {
        try {
            PacienteExterno pe = pacienteExternoService.crearOrientacionCompleta(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orientación vocacional registrada exitosamente");
            response.put("pacienteId", pe.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
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