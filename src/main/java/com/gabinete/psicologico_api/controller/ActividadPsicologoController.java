package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.ActividadEvidencia;
import com.gabinete.psicologico_api.model.ActividadPsicologo;
import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.repository.ActividadEvidenciaRepository;
import com.gabinete.psicologico_api.repository.ActividadPsicologoRepository;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/actividades")
public class ActividadPsicologoController {

    @Autowired
    private ActividadPsicologoRepository actividadRepository;

    @Autowired
    private ActividadEvidenciaRepository evidenciaRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodas() {
        List<ActividadPsicologo> actividades = actividadRepository.findAllByOrderByFechaInicioDesc();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", actividades);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return actividadRepository.findById(id)
                .map(a -> {
                    response.put("success", true);
                    response.put("data", a);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Actividad no encontrada");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @GetMapping("/rango")
    public ResponseEntity<Map<String, Object>> obtenerPorRango(
            @RequestParam String desde,
            @RequestParam String hasta) {
        LocalDateTime fechaDesde = LocalDateTime.parse(desde + "T00:00:00");
        LocalDateTime fechaHasta = LocalDateTime.parse(hasta + "T23:59:59");
        List<ActividadPsicologo> actividades = actividadRepository
                .findByFechaInicioGreaterThanEqualAndFechaFinLessThanEqual(fechaDesde, fechaHasta);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", actividades);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crear(
            @RequestParam Long psicologoId,
            @RequestParam String titulo,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam String objetivo,
            @RequestParam String poblacion,
            @RequestParam Integer numAsistentes,
            @RequestParam String resumen,
            @RequestParam String resultados,
            @RequestPart(required = false) List<MultipartFile> evidencias) {
        try {
            Psicologo psicologo = psicologoRepository.findById(psicologoId)
                    .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

            ActividadPsicologo actividad = new ActividadPsicologo();
            actividad.setPsicologo(psicologo);
            actividad.setTitulo(titulo);
            actividad.setFechaInicio(LocalDateTime.parse(fechaInicio));
            actividad.setFechaFin(LocalDateTime.parse(fechaFin));
            actividad.setObjetivo(objetivo);
            actividad.setPoblacion(poblacion);
            actividad.setNumAsistentes(numAsistentes);
            actividad.setResumen(resumen);
            actividad.setResultados(resultados);

            actividad = actividadRepository.save(actividad);

            if (evidencias != null) {
                for (MultipartFile file : evidencias) {
                    if (file != null && !file.isEmpty()) {
                        ActividadEvidencia ev = new ActividadEvidencia();
                        ev.setActividadPsicologo(actividad);
                        ev.setImagen(file.getBytes());
                        ev.setNombre(file.getOriginalFilename());
                        evidenciaRepository.save(ev);
                    }
                }
            }

            // Recargar para incluir evidencias en la respuesta
            actividad = actividadRepository.findById(actividad.getId()).orElse(actividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", actividad);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al crear actividad: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestParam Long psicologoId,
            @RequestParam String titulo,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam String objetivo,
            @RequestParam String poblacion,
            @RequestParam Integer numAsistentes,
            @RequestParam String resumen,
            @RequestParam String resultados,
            @RequestPart(required = false) List<MultipartFile> evidencias,
            @RequestParam(required = false) List<Long> eliminarEvidenciaIds) {
        try {
            ActividadPsicologo actividad = actividadRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

            Psicologo psicologo = psicologoRepository.findById(psicologoId)
                    .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

            actividad.setPsicologo(psicologo);
            actividad.setTitulo(titulo);
            actividad.setFechaInicio(LocalDateTime.parse(fechaInicio));
            actividad.setFechaFin(LocalDateTime.parse(fechaFin));
            actividad.setObjetivo(objetivo);
            actividad.setPoblacion(poblacion);
            actividad.setNumAsistentes(numAsistentes);
            actividad.setResumen(resumen);
            actividad.setResultados(resultados);

            actividad = actividadRepository.save(actividad);

            // Eliminar evidencias marcadas
            if (eliminarEvidenciaIds != null) {
                for (Long evId : eliminarEvidenciaIds) {
                    evidenciaRepository.deleteById(evId);
                }
            }

            // Agregar nuevas evidencias
            if (evidencias != null) {
                for (MultipartFile file : evidencias) {
                    if (file != null && !file.isEmpty()) {
                        ActividadEvidencia ev = new ActividadEvidencia();
                        ev.setActividadPsicologo(actividad);
                        ev.setImagen(file.getBytes());
                        ev.setNombre(file.getOriginalFilename());
                        evidenciaRepository.save(ev);
                    }
                }
            }

            actividad = actividadRepository.findById(actividad.getId()).orElse(actividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", actividad);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al actualizar actividad: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            ActividadPsicologo actividad = actividadRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
            actividadRepository.delete(actividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Actividad eliminada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar actividad: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/evidencia/{evidenciaId}")
    public ResponseEntity<?> descargarEvidencia(@PathVariable Long evidenciaId) {
        ActividadEvidencia ev = evidenciaRepository.findById(evidenciaId).orElse(null);
        if (ev == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + ev.getNombre() + "\"")
                .header("Content-Type", "application/octet-stream")
                .body(ev.getImagen());
    }

    @GetMapping("/{id}/evidencias")
    public ResponseEntity<Map<String, Object>> listarEvidencias(@PathVariable Long id) {
        List<ActividadEvidencia> evidencias = evidenciaRepository.findByActividadPsicologoId(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", evidencias);
        return ResponseEntity.ok(response);
    }
}
