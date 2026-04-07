package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.HorasDesignadasAp;
import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.repository.HorasDesignadasApRepository;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import com.gabinete.psicologico_api.service.ReportesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reportes")
public class ReportesController {

    @Autowired private ReportesService reportesService;
    @Autowired private HorasDesignadasApRepository horasDesignadasRepo;
    @Autowired private PsicologoRepository psicologoRepo;

    // ── GET /api/reportes/horas-turno?anio=2026 ─────────────────────────────
    @GetMapping("/horas-turno")
    public ResponseEntity<?> horasPorTurno(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.horasPorTurno(year));
    }

    // ── GET /api/reportes/horas-genero?anio=2026 ────────────────────────────
    @GetMapping("/horas-genero")
    public ResponseEntity<?> horasPorGenero(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.horasPorGenero(year));
    }

    // ── GET /api/reportes/horas-departamento?anio=2026 ──────────────────────
    @GetMapping("/horas-departamento")
    public ResponseEntity<?> horasPorDepartamento(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.horasPorDepartamento(year));
    }

    // ── GET /api/reportes/casos-gravedad ────────────────────────────────────
    @GetMapping("/casos-gravedad")
    public ResponseEntity<?> casosPorGravedad() {
        return ok(reportesService.casosPorGravedad());
    }

    // ── GET /api/reportes/tipologias-genero ─────────────────────────────────
    @GetMapping("/tipologias-genero")
    public ResponseEntity<?> tipologiasPorGenero() {
        return ok(reportesService.tipologiasPorGenero());
    }

    // ── GET /api/reportes/participantes-carrera ─────────────────────────────
    @GetMapping("/participantes-carrera")
    public ResponseEntity<?> participantesPorCarrera() {
        return ok(reportesService.participantesPorCarrera());
    }

    // ── GET /api/reportes/horas-ejecutadas-vs-designadas?anio=2026 ─────────
    @GetMapping("/horas-ejecutadas-vs-designadas")
    public ResponseEntity<?> horasEjecutadasVsDesignadas(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.horasEjecutadasVsDesignadas(year));
    }

    // ── GET /api/reportes/sesiones-por-mes?anio=2026 ────────────────────────
    @GetMapping("/sesiones-por-mes")
    public ResponseEntity<?> sesionesPorMes(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.sesionesPorMes(year));
    }

    // ── GET /api/reportes/sesiones-por-psicologo?anio=2026 ──────────────────
    @GetMapping("/sesiones-por-psicologo")
    public ResponseEntity<?> sesionesPorPsicologo(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.sesionesPorPsicologo(year));
    }

    // ── GET /api/reportes/score-promedio?anio=2026 ──────────────────────────
    @GetMapping("/score-promedio")
    public ResponseEntity<?> scorePromedio(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.scorePromedio(year));
    }

    // ── GET /api/reportes/semestres ─────────────────────────────────────────
    @GetMapping("/semestres")
    public ResponseEntity<?> semestres() {
        return ok(reportesService.semestres());
    }

    // ── GET /api/reportes/nuevos-pacientes-por-mes?anio=2026 ────────────────
    @GetMapping("/nuevos-pacientes-por-mes")
    public ResponseEntity<?> nuevosPacientesPorMes(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(reportesService.nuevosPacientesPorMes(year));
    }

    // ── GET /api/reportes/distribucion-genero ───────────────────────────────
    @GetMapping("/distribucion-genero")
    public ResponseEntity<?> distribucionGenero() {
        return ok(reportesService.distribucionGenero());
    }

    // ── GET /api/reportes/distribucion-edad ─────────────────────────────────
    @GetMapping("/distribucion-edad")
    public ResponseEntity<?> distribucionEdad() {
        return ok(reportesService.distribucionEdad());
    }

    // ── GET /api/reportes/horas-designadas?anio=2026 ────────────────────────
    @GetMapping("/horas-designadas")
    public ResponseEntity<?> listarHorasDesignadas(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        return ok(horasDesignadasRepo.findByAnio(year));
    }

    // ── PUT /api/reportes/horas-designadas ──────────────────────────────────
    // Body: { "psicologoId": 1, "anio": 2026, "mes": 1, "turno": "manana", "horas": 69 }
    @PutMapping("/horas-designadas")
    public ResponseEntity<?> upsertHorasDesignadas(@RequestBody Map<String, Object> body) {
        try {
            Long psicologoId = Long.valueOf(body.get("psicologoId").toString());
            Integer anio     = (Integer) body.get("anio");
            Integer mes      = (Integer) body.get("mes");
            String  turno    = (String)  body.get("turno");
            Integer horas    = (Integer) body.get("horas");

            Psicologo psicologo = psicologoRepo.findById(psicologoId)
                    .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

            HorasDesignadasAp entity = horasDesignadasRepo
                    .findByPsicologo_IdAndAnioAndMesAndTurno(psicologoId, anio, mes, turno)
                    .orElseGet(HorasDesignadasAp::new);

            entity.setPsicologo(psicologo);
            entity.setAnio(anio);
            entity.setMes(mes);
            entity.setTurno(turno);
            entity.setHoras(horas);
            horasDesignadasRepo.save(entity);

            return ok(entity);
        } catch (Exception e) {
            return error("Error al guardar horas designadas: " + e.getMessage());
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> error(String msg) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
