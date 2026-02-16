// src/main/java/com/gabinete/psicologico_api/controller/CarreraController.java
package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.Carrera;
import com.gabinete.psicologico_api.repository.CarreraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carreras")
@CrossOrigin(origins = "http://localhost:3000")
public class CarreraController {

    @Autowired
    private CarreraRepository carreraRepository;

    @GetMapping
    public ResponseEntity<?> obtenerTodasCarreras() {
        try {
            List<Carrera> carreras = carreraRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", carreras);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener carreras: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> crearCarrera(@RequestBody Map<String, Object> carreraData) {
        try {
            Carrera carrera = new Carrera();
            carrera.setCarrera((String) carreraData.get("carrera"));
            carrera.setDepartamento((Integer) carreraData.get("departamento"));
            
            Carrera guardada = carreraRepository.save(carrera);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Carrera creada exitosamente");
            response.put("data", guardada);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear carrera: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}