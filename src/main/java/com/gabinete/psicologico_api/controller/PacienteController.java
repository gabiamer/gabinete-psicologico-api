//src/main/java/com/gabinete/psicologico_api/controller/PacienteController.java
package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.PacienteUniversitarioDTO;
import com.gabinete.psicologico_api.model.PacienteUniversitario;
import com.gabinete.psicologico_api.repository.PacienteUniversitarioRepository;
import com.gabinete.psicologico_api.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gabinete.psicologico_api.dto.AntecedentesDTO;
import com.gabinete.psicologico_api.model.EntrevistaPsicologica;
import com.gabinete.psicologico_api.service.EntrevistaService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:3000")
public class PacienteController {
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private EntrevistaService entrevistaService;

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;
    
    // Endpoint de búsqueda combinada (por término y/o fecha)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPacientes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fecha) {
        try {
            List<PacienteUniversitario> pacientes;
            
            // Si hay fecha, buscar por fecha (con o sin término)
            if (fecha != null && !fecha.trim().isEmpty()) {
                LocalDate fechaBusqueda = LocalDate.parse(fecha);
                
                if (q != null && !q.trim().isEmpty()) {
                    // Buscar por término Y fecha
                    pacientes = pacienteService.buscarPorTerminoYFecha(q, fechaBusqueda);
                } else {
                    // Buscar solo por fecha
                    pacientes = pacienteService.buscarPorFecha(fechaBusqueda);
                }
            } else if (q != null && !q.trim().isEmpty()) {
                // Buscar solo por término (comportamiento actual)
                pacientes = pacienteService.buscarPacientes(q);
            } else {
                // Sin parámetros, devolver lista vacía
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
            List<PacienteUniversitario> pacientes = pacienteUniversitarioRepository.findAll();
            
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
    
    // DELETE - Eliminar paciente
    @DeleteMapping("/universitario/{id}")
    public ResponseEntity<Map<String, Object>> eliminarPaciente(@PathVariable Long id) {
        try {
            if (!pacienteUniversitarioRepository.existsById(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Paciente no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            pacienteUniversitarioRepository.deleteById(id);
            
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
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Endpoint de pacientes funcionando");
        return ResponseEntity.ok(response);
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