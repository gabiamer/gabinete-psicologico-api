package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import com.gabinete.psicologico_api.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportesService {

    private static final String[] NOMBRE_MES = {
        "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    @Autowired private SesionPacienteRepository sesionRepo;
    @Autowired private HistorialClinicoRepository historialRepo;
    @Autowired private EstudianteCarreraRepository estudianteCarreraRepo;
    @Autowired private EntrevistaPsicologicaRepository entrevistaRepo;
    @Autowired private PacienteUniversitarioRepository pacienteUniversitarioRepo;
    @Autowired private OrientacionVocacionalRepository orientacionRepo;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private ObjectMapper objectMapper;

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Set<Long> getPacienteIdsScope(Long psicologoId) {
        if (psicologoId == null) return null;
        return pacienteUniversitarioRepo.findByPsicologoId(psicologoId)
                .stream().map(PacienteUniversitario::getId).collect(Collectors.toSet());
    }

    private List<SesionPaciente> getSesionesRango(LocalDate desde, LocalDate hasta, Long psicologoId) {
        LocalDateTime ini = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);
        if (psicologoId == null) return sesionRepo.findByFechaBetween(ini, fin);
        return sesionRepo.findByFechaBetweenAndPsicologoId(ini, fin, psicologoId);
    }

    /** Genera filas mes/año para el rango dado (garantiza que todos los meses aparezcan) */
    private List<int[]> mesesEnRango(LocalDate desde, LocalDate hasta) {
        List<int[]> meses = new ArrayList<>();
        LocalDate cursor = desde.withDayOfMonth(1);
        LocalDate limite = hasta.withDayOfMonth(1);
        while (!cursor.isAfter(limite)) {
            meses.add(new int[]{cursor.getYear(), cursor.getMonthValue()});
            cursor = cursor.plusMonths(1);
        }
        return meses;
    }

    /** Etiqueta legible para un mes: "Ene 2026" si el rango abarca más de un año, "Enero" si no */
    private String etiquetaMes(int anio, int mes, boolean multiAnio) {
        String nombre = NOMBRE_MES[mes];
        return multiAnio ? nombre.substring(0, 3) + " " + anio : nombre;
    }

    // ── Gráfica: Sesiones por mes ────────────────────────────────────────────

    public List<Map<String, Object>> sesionesPorMes(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<SesionPaciente> sesiones = getSesionesRango(desde, hasta, psicologoId);
        boolean multiAnio = desde.getYear() != hasta.getYear();

        Map<String, Long> conteo = new LinkedHashMap<>();
        for (int[] ym : mesesEnRango(desde, hasta))
            conteo.put(etiquetaMes(ym[0], ym[1], multiAnio), 0L);

        for (SesionPaciente s : sesiones) {
            String key = etiquetaMes(s.getFecha().getYear(), s.getFecha().getMonthValue(), multiAnio);
            conteo.merge(key, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((mes, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", mes);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Sesiones por turno (mañana/tarde) por mes ──────────────────

    public List<Map<String, Object>> sesionesPorTurno(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<SesionPaciente> sesiones = getSesionesRango(desde, hasta, psicologoId);
        boolean multiAnio = desde.getYear() != hasta.getYear();

        Map<String, long[]> data = new LinkedHashMap<>();
        for (int[] ym : mesesEnRango(desde, hasta))
            data.put(etiquetaMes(ym[0], ym[1], multiAnio), new long[]{0, 0});

        for (SesionPaciente s : sesiones) {
            String key = etiquetaMes(s.getFecha().getYear(), s.getFecha().getMonthValue(), multiAnio);
            int idx = s.getFecha().getHour() < 12 ? 0 : 1;
            data.get(key)[idx]++;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", mes);
            row.put("manana", v[0]);
            row.put("tarde", v[1]);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Sesiones por psicólogo (en rango) ──────────────────────────

    public List<Map<String, Object>> sesionesPorPsicologo(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<SesionPaciente> sesiones = getSesionesRango(desde, hasta, psicologoId);

        Map<String, Long> conteo = new TreeMap<>();
        for (SesionPaciente s : sesiones) {
            String nombre = "Sin asignar";
            if (s.getPsicologo() != null && s.getPsicologo().getPerson() != null) {
                Person p = s.getPsicologo().getPerson();
                nombre = (p.getPrimerNombre() + " " +
                          (p.getApellidoPaterno() != null ? p.getApellidoPaterno() : "")).trim();
            }
            conteo.merge(nombre, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((ps, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("psicologo", ps);
            row.put("total", total);
            result.add(row);
        });
        result.sort((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")));
        return result;
    }

    // ── Gráfica: Pacientes asignados por psicólogo (acumulado) ──────────────

    public List<Map<String, Object>> pacientesPorPsicologo() {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<PacienteUniversitario> pacientes = psicologoId == null
                ? pacienteUniversitarioRepo.findAll()
                : pacienteUniversitarioRepo.findByPsicologoId(psicologoId);

        Map<String, Long> conteo = new LinkedHashMap<>();
        for (PacienteUniversitario pu : pacientes) {
            if (pu.getPsicologo() == null || pu.getPsicologo().getPerson() == null) continue;
            String nombre = pu.getPsicologo().getPerson().getPrimerNombre()
                    + " " + pu.getPsicologo().getPerson().getApellidoPaterno();
            conteo.merge(nombre, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("psicologo", e.getKey());
                    row.put("total", e.getValue());
                    result.add(row);
                });
        return result;
    }

    // ── Gráfica: Sesiones por departamento académico (en rango) ─────────────

    public List<Map<String, Object>> sesionesporDepartamento(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        Set<Long> scope = getPacienteIdsScope(psicologoId);

        Map<Long, String> puDepartamento = new HashMap<>();
        estudianteCarreraRepo.findAll().forEach(ec -> {
            if (ec.getCarrera() != null && ec.getCarrera().getDepartamento() != null) {
                Long puId = ec.getPacienteUniversitario().getId();
                if (scope == null || scope.contains(puId))
                    puDepartamento.put(puId, ec.getCarrera().getDepartamento());
            }
        });

        List<SesionPaciente> sesiones = getSesionesRango(desde, hasta, psicologoId);
        boolean multiAnio = desde.getYear() != hasta.getYear();

        Map<String, Map<String, Long>> data = new LinkedHashMap<>();
        for (int[] ym : mesesEnRango(desde, hasta))
            data.put(etiquetaMes(ym[0], ym[1], multiAnio), new HashMap<>());

        for (SesionPaciente s : sesiones) {
            String mes = etiquetaMes(s.getFecha().getYear(), s.getFecha().getMonthValue(), multiAnio);
            String dep = puDepartamento.getOrDefault(s.getPacienteUniversitario().getId(), "Sin departamento");
            data.get(mes).merge(dep, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, valores) -> {
            Map<String, Object> row = new HashMap<>(valores);
            row.put("mes", mes);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Score promedio estrés/ansiedad/depresión (en rango) ─────────

    public List<Map<String, Object>> scorePromedio(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        Set<Long> scope = getPacienteIdsScope(psicologoId);
        boolean multiAnio = desde.getYear() != hasta.getYear();

        Map<String, long[][]> data = new LinkedHashMap<>();
        for (int[] ym : mesesEnRango(desde, hasta))
            data.put(etiquetaMes(ym[0], ym[1], multiAnio), new long[][]{{0,0},{0,0},{0,0}});

        for (EntrevistaPsicologica e : entrevistaRepo.findAll()) {
            if (e.getSesionPaciente() == null || e.getSesionPaciente().getFecha() == null) continue;
            LocalDate fechaSesion = e.getSesionPaciente().getFecha().toLocalDate();
            if (fechaSesion.isBefore(desde) || fechaSesion.isAfter(hasta)) continue;
            if (scope != null) {
                try {
                    Long puId = e.getSesionPaciente().getPacienteUniversitario().getId();
                    if (!scope.contains(puId)) continue;
                } catch (Exception ignored) { continue; }
            }
            String key = etiquetaMes(fechaSesion.getYear(), fechaSesion.getMonthValue(), multiAnio);
            long[][] v = data.get(key);
            if (v == null) continue;
            if (e.getTotalScoreEstres()    != null) { v[0][0] += e.getTotalScoreEstres();    v[0][1]++; }
            if (e.getTotalScoreAnsiedad()  != null) { v[1][0] += e.getTotalScoreAnsiedad();  v[1][1]++; }
            if (e.getTotalScoreDepresion() != null) { v[2][0] += e.getTotalScoreDepresion(); v[2][1]++; }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", mes);
            row.put("estres",    v[0][1] > 0 ? Math.round((double) v[0][0] / v[0][1] * 10.0) / 10.0 : 0);
            row.put("ansiedad",  v[1][1] > 0 ? Math.round((double) v[1][0] / v[1][1] * 10.0) / 10.0 : 0);
            row.put("depresion", v[2][1] > 0 ? Math.round((double) v[2][0] / v[2][1] * 10.0) / 10.0 : 0);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Casos por gravedad (en rango — usa fecha de sesión) ─────────

    public List<Map<String, Object>> casosPorGravedad(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        Set<Long> scope = getPacienteIdsScope(psicologoId);

        Map<String, Long> conteo = new LinkedHashMap<>();
        conteo.put("leve", 0L);
        conteo.put("moderado", 0L);
        conteo.put("grave", 0L);
        conteo.put("muy grave", 0L);
        conteo.put("riesgo de vida", 0L);

        for (HistorialClinico hc : historialRepo.findAll()) {
            if (hc.getGravedad() == null) continue;
            try {
                LocalDate fechaSesion = hc.getSesionPaciente().getFecha().toLocalDate();
                if (fechaSesion.isBefore(desde) || fechaSesion.isAfter(hasta)) continue;
                Long puId = hc.getSesionPaciente().getPacienteUniversitario().getId();
                if (scope != null && !scope.contains(puId)) continue;
            } catch (Exception ignored) { continue; }
            conteo.merge(hc.getGravedad().toLowerCase().trim(), 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((gravedad, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("gravedad", gravedad);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Tipologías por género (en rango) ────────────────────────────

    public List<Map<String, Object>> tipologiasPorGenero(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        Set<Long> scope = getPacienteIdsScope(psicologoId);

        Map<String, long[]> conteo = new TreeMap<>();

        for (HistorialClinico hc : historialRepo.findAll()) {
            if (hc.getTipologia() == null) continue;
            Integer genero = null;
            Long puId = null;
            try {
                LocalDate fechaSesion = hc.getSesionPaciente().getFecha().toLocalDate();
                if (fechaSesion.isBefore(desde) || fechaSesion.isAfter(hasta)) continue;
                puId = hc.getSesionPaciente().getPacienteUniversitario().getId();
                genero = hc.getSesionPaciente().getPacienteUniversitario().getPaciente().getGenero();
            } catch (Exception ignored) {}
            if (genero == null) continue;
            if (scope != null && (puId == null || !scope.contains(puId))) continue;

            for (String tip : parseTipologias(hc.getTipologia())) {
                String key = tip.trim();
                conteo.computeIfAbsent(key, k -> new long[]{0L, 0L});
                if (genero == 2) conteo.get(key)[1]++;
                else             conteo.get(key)[0]++;
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((tip, counts) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("tipologia", tip);
            row.put("masculino", counts[0]);
            row.put("femenino", counts[1]);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Casos por situación (acumulado — sin fecha) ────────────────

    public List<Map<String, Object>> casosPorSituacion() {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<PacienteUniversitario> pacientes = psicologoId == null
                ? pacienteUniversitarioRepo.findAll()
                : pacienteUniversitarioRepo.findByPsicologoId(psicologoId);

        String[] situaciones = {
            "Acompañamiento psicológico", "Buen proceso", "Proceso terminado",
            "Orientación vocacional", "Derivado a consultorio externo"
        };
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String s : situaciones) conteo.put(s, 0L);
        for (PacienteUniversitario pu : pacientes) {
            String sit = pu.getSituacionCaso() != null ? pu.getSituacionCaso() : "Acompañamiento psicológico";
            conteo.merge(sit, 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((situacion, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("situacion", situacion);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica: Nuevos pacientes por mes (en rango) ─────────────────────────

    public List<Map<String, Object>> nuevosPacientesPorMes(LocalDate desde, LocalDate hasta) {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        Set<Long> scope = getPacienteIdsScope(psicologoId);
        boolean multiAnio = desde.getYear() != hasta.getYear();

        Map<String, long[]> data = new LinkedHashMap<>();
        for (int[] ym : mesesEnRango(desde, hasta))
            data.put(etiquetaMes(ym[0], ym[1], multiAnio), new long[]{0, 0});

        entrevistaRepo.findAll().forEach(e -> {
            if (e.getFecha() == null) return;
            if (e.getFecha().isBefore(desde) || e.getFecha().isAfter(hasta)) return;
            if (scope != null) {
                try {
                    Long puId = e.getSesionPaciente().getPacienteUniversitario().getId();
                    if (!scope.contains(puId)) return;
                } catch (Exception ignored) { return; }
            }
            String key = etiquetaMes(e.getFecha().getYear(), e.getFecha().getMonthValue(), multiAnio);
            long[] v = data.get(key);
            if (v != null) v[0]++;
        });

        orientacionRepo.findAll().forEach(o -> {
            if (o.getFecha() == null) return;
            if (o.getFecha().isBefore(desde) || o.getFecha().isAfter(hasta)) return;
            String key = etiquetaMes(o.getFecha().getYear(), o.getFecha().getMonthValue(), multiAnio);
            long[] v = data.get(key);
            if (v != null) v[1]++;
        });

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", mes);
            row.put("universitarios", v[0]);
            row.put("externos", v[1]);
            result.add(row);
        });
        return result;
    }

    // ── Gráficas acumuladas (sin filtro temporal) ────────────────────────────

    public List<Map<String, Object>> participantesPorCarrera() {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        Set<Long> scope = getPacienteIdsScope(psicologoId);

        Map<String, Long> conteo = new TreeMap<>();
        estudianteCarreraRepo.findAll().forEach(ec -> {
            if (ec.getCarrera() == null) return;
            Long puId = ec.getPacienteUniversitario().getId();
            if (scope != null && !scope.contains(puId)) return;
            conteo.merge(ec.getCarrera().getCarrera(), 1L, Long::sum);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((carrera, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("carrera", carrera);
            row.put("total", total);
            result.add(row);
        });
        result.sort((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")));
        return result;
    }

    public List<Map<String, Object>> semestres() {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<PacienteUniversitario> pacientes = psicologoId == null
                ? pacienteUniversitarioRepo.findAll()
                : pacienteUniversitarioRepo.findByPsicologoId(psicologoId);

        Map<Integer, Long> conteo = new TreeMap<>();
        pacientes.forEach(pu -> { if (pu.getSemestre() != null) conteo.merge(pu.getSemestre(), 1L, Long::sum); });

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((sem, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("semestre", "Sem. " + sem);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    public List<Map<String, Object>> distribucionGenero() {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<Paciente> pacientes = psicologoId == null
                ? pacienteRepo.findAll()
                : pacienteUniversitarioRepo.findByPsicologoId(psicologoId)
                        .stream().map(PacienteUniversitario::getPaciente).collect(Collectors.toList());

        Map<String, Long> conteo = new LinkedHashMap<>();
        conteo.put("Masculino", 0L); conteo.put("Femenino", 0L); conteo.put("Sin especificar", 0L);
        pacientes.forEach(p -> {
            if      (p.getGenero() == null) conteo.merge("Sin especificar", 1L, Long::sum);
            else if (p.getGenero() == 1)    conteo.merge("Masculino",       1L, Long::sum);
            else if (p.getGenero() == 2)    conteo.merge("Femenino",        1L, Long::sum);
            else                            conteo.merge("Sin especificar", 1L, Long::sum);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((genero, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("genero", genero); row.put("total", total);
            result.add(row);
        });
        return result;
    }

    public List<Map<String, Object>> distribucionEdad() {
        Long psicologoId = SecurityUtils.getCurrentPsicologoId();
        List<Paciente> pacientes = psicologoId == null
                ? pacienteRepo.findAll()
                : pacienteUniversitarioRepo.findByPsicologoId(psicologoId)
                        .stream().map(PacienteUniversitario::getPaciente).collect(Collectors.toList());

        String[] rangos = {"< 15", "15-17", "18-20", "21-23", "24-26", "27-30", "31+"};
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String r : rangos) conteo.put(r, 0L);

        pacientes.forEach(p -> {
            if (p.getEdad() == null) return;
            int edad = p.getEdad();
            String rango;
            if      (edad < 15)  rango = "< 15";
            else if (edad <= 17) rango = "15-17";
            else if (edad <= 20) rango = "18-20";
            else if (edad <= 23) rango = "21-23";
            else if (edad <= 26) rango = "24-26";
            else if (edad <= 30) rango = "27-30";
            else                 rango = "31+";
            conteo.merge(rango, 1L, Long::sum);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((rango, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("rango", rango); row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Util ────────────────────────────────────────────────────────────────

    private List<String> parseTipologias(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
