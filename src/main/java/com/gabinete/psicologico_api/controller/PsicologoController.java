package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psicologos")
@CrossOrigin(origins = "http://localhost:3000")
public class PsicologoController {

    @Autowired
    private PsicologoRepository psicologoRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos() {
        List<Psicologo> psicologos = psicologoRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", psicologos);
        return ResponseEntity.ok(response);
    }
}