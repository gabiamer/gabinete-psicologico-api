package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.model.HorasDesignadasAp;
import com.gabinete.psicologico_api.model.Psicologo;
import com.gabinete.psicologico_api.repository.HorasDesignadasApRepository;
import com.gabinete.psicologico_api.repository.PsicologoRepository;
import com.gabinete.psicologico_api.security.SecurityUtils;
import com.gabinete.psicologico_api.service.ReportesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReportesController {

    @Autowired private ReportesService reportesService;
    @Autowired private HorasDesignadasApRepository horasDesignadasRepo;
    @Autowired private PsicologoRepository psicologoRepo;

    // ── Helper para parsear rango desde/hasta ────────────────────────────────

    /** Si no se pasan desde/hasta, usa el año en curso completo */
    private LocalDate[] rango(String desde, String hasta) {
        LocalDate hoy = LocalDate.now();
        LocalDate d = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : hoy.withDayOfYear(1);
        LocalDate h = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : hoy.withDayOfMonth(hoy.lengthOfMonth()).withMonth(12).withDayOfMonth(31);
        return new LocalDate[]{d, h};
    }

    // ── Endpoints con filtro de rango ────────────────────────────────────────

    @GetMapping("/sesiones-por-mes")
    public ResponseEntity<?> sesionesPorMes(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.sesionesPorMes(r[0], r[1]));
    }

    @GetMapping("/sesiones-turno")
    public ResponseEntity<?> sesionesPorTurno(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.sesionesPorTurno(r[0], r[1]));
    }

    @GetMapping("/sesiones-por-psicologo")
    public ResponseEntity<?> sesionesPorPsicologo(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.sesionesPorPsicologo(r[0], r[1]));
    }

    @GetMapping("/horas-departamento")
    public ResponseEntity<?> sesionesporDepartamento(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.sesionesporDepartamento(r[0], r[1]));
    }

    @GetMapping("/score-promedio")
    public ResponseEntity<?> scorePromedio(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.scorePromedio(r[0], r[1]));
    }

    @GetMapping("/casos-gravedad")
    public ResponseEntity<?> casosPorGravedad(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.casosPorGravedad(r[0], r[1]));
    }

    @GetMapping("/tipologias-genero")
    public ResponseEntity<?> tipologiasPorGenero(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.tipologiasPorGenero(r[0], r[1]));
    }

    @GetMapping("/nuevos-pacientes-por-mes")
    public ResponseEntity<?> nuevosPacientesPorMes(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        LocalDate[] r = rango(desde, hasta);
        return ok(reportesService.nuevosPacientesPorMes(r[0], r[1]));
    }

    // ── Endpoints acumulados (sin filtro temporal) ───────────────────────────

    @GetMapping("/pacientes-por-psicologo")
    public ResponseEntity<?> pacientesPorPsicologo() {
        return ok(reportesService.pacientesPorPsicologo());
    }

    @GetMapping("/casos-situacion")
    public ResponseEntity<?> casosPorSituacion() {
        return ok(reportesService.casosPorSituacion());
    }

    @GetMapping("/participantes-carrera")
    public ResponseEntity<?> participantesPorCarrera() {
        return ok(reportesService.participantesPorCarrera());
    }

    @GetMapping("/semestres")
    public ResponseEntity<?> semestres() {
        return ok(reportesService.semestres());
    }

    @GetMapping("/distribucion-genero")
    public ResponseEntity<?> distribucionGenero() {
        return ok(reportesService.distribucionGenero());
    }

    @GetMapping("/distribucion-edad")
    public ResponseEntity<?> distribucionEdad() {
        return ok(reportesService.distribucionEdad());
    }

    // ── Horas designadas (mantiene filtro por año) ───────────────────────────

    @GetMapping("/horas-designadas")
    public ResponseEntity<?> listarHorasDesignadas(@RequestParam(defaultValue = "0") int anio) {
        int year = anio > 0 ? anio : LocalDate.now().getYear();
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        if (psicologoId != null) return ok(horasDesignadasRepo.findByAnioAndPsicologoId(year, psicologoId));
        return ok(horasDesignadasRepo.findByAnio(year));
    }

    @PutMapping("/horas-designadas")
    public ResponseEntity<?> upsertHorasDesignadas(@RequestBody Map<String, Object> body) {
        try {
            Long psicologoId = Long.valueOf(body.get("psicologoId").toString());
            Integer anio  = (Integer) body.get("anio");
            Integer mes   = (Integer) body.get("mes");
            String  turno = (String)  body.get("turno");
            Integer horas = (Integer) body.get("horas");

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
