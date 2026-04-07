package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.Person;
import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.repository.PersonRepository;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psicologos")
public class PsicologoController {

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PersonRepository personRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos() {
        List<Psicologo> psicologos = psicologoRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", psicologos);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Person person = new Person();
            person.setPrimerNombre((String) body.get("primerNombre"));
            person.setSegundoNombre((String) body.get("segundoNombre"));
            person.setApellidoPaterno((String) body.get("apellidoPaterno"));
            person.setApellidoMaterno((String) body.get("apellidoMaterno"));
            person.setCelular((String) body.get("celular"));
            person = personRepository.save(person);

            Psicologo psicologo = new Psicologo();
            psicologo.setPerson(person);
            psicologo.setOcupacion((String) body.get("ocupacion"));
            psicologo = psicologoRepository.save(psicologo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", psicologo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear psicólogo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Psicologo psicologo = psicologoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

            Person person = psicologo.getPerson();
            if (body.containsKey("primerNombre"))   person.setPrimerNombre((String) body.get("primerNombre"));
            if (body.containsKey("segundoNombre"))  person.setSegundoNombre((String) body.get("segundoNombre"));
            if (body.containsKey("apellidoPaterno")) person.setApellidoPaterno((String) body.get("apellidoPaterno"));
            if (body.containsKey("apellidoMaterno")) person.setApellidoMaterno((String) body.get("apellidoMaterno"));
            if (body.containsKey("celular"))        person.setCelular((String) body.get("celular"));
            personRepository.save(person);

            if (body.containsKey("ocupacion")) psicologo.setOcupacion((String) body.get("ocupacion"));
            psicologoRepository.save(psicologo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", psicologo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al actualizar psicólogo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            Psicologo psicologo = psicologoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));
            psicologoRepository.delete(psicologo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Psicólogo eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar psicólogo: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}