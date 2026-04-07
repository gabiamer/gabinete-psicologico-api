package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.model.*;
import com.gabinete.psicologico_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportesService {

    private static final String[] NOMBRE_MES = {
        "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    @Autowired private SesionPacienteRepository sesionRepo;
    @Autowired private HistorialClinicoRepository historialRepo;
    @Autowired private EstudianteCarreraRepository estudianteCarreraRepo;
    @Autowired private HorasDesignadasApRepository horasDesignadasRepo;
    @Autowired private EntrevistaPsicologicaRepository entrevistaRepo;
    @Autowired private PacienteUniversitarioRepository pacienteUniversitarioRepo;
    @Autowired private OrientacionVocacionalRepository orientacionRepo;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private ObjectMapper objectMapper;

    // ── Gráfica 1: Horas de uso AP por turno y mes ──────────────────────────

    public List<Map<String, Object>> horasPorTurno(int anio) {
        List<SesionPaciente> sesiones = sesionRepo.findByFechaBetween(
            LocalDateTime.of(anio, 1, 1, 0, 0),
            LocalDateTime.of(anio, 12, 31, 23, 59)
        );

        // Inicializar estructura mensual
        Map<Integer, Map<String, Double>> data = new TreeMap<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Double> fila = new HashMap<>();
            fila.put("designadas_manana", 0.0);
            fila.put("designadas_tarde", 0.0);
            fila.put("usadas_manana", 0.0);
            fila.put("usadas_tarde", 0.0);
            data.put(m, fila);
        }

        // Acumular horas usadas desde sesiones
        for (SesionPaciente s : sesiones) {
            if (s.getDuracionMinutos() == null || s.getDuracionMinutos() <= 0) continue;
            int mes = s.getFecha().getMonthValue();
            String turno = s.getFecha().getHour() < 12 ? "manana" : "tarde";
            double horas = s.getDuracionMinutos() / 60.0;
            data.get(mes).merge("usadas_" + turno, horas, Double::sum);
        }

        // Cargar horas designadas registradas administrativamente
        horasDesignadasRepo.findByAnio(anio).forEach(h ->
            data.get(h.getMes()).put("designadas_" + h.getTurno(), (double) h.getHoras())
        );

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, valores) -> {
            Map<String, Object> row = new HashMap<>(valores);
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 2: Horas usadas AP por género y mes ─────────────────────────

    public List<Map<String, Object>> horasPorGenero(int anio) {
        List<SesionPaciente> sesiones = sesionRepo.findByFechaBetween(
            LocalDateTime.of(anio, 1, 1, 0, 0),
            LocalDateTime.of(anio, 12, 31, 23, 59)
        );

        Map<Integer, Map<String, Double>> data = new TreeMap<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Double> fila = new HashMap<>();
            fila.put("mujeres", 0.0);
            fila.put("varones", 0.0);
            data.put(m, fila);
        }

        for (SesionPaciente s : sesiones) {
            if (s.getDuracionMinutos() == null || s.getDuracionMinutos() <= 0) continue;
            Integer genero = s.getPacienteUniversitario().getPaciente().getGenero();
            if (genero == null) continue;
            int mes = s.getFecha().getMonthValue();
            double horas = s.getDuracionMinutos() / 60.0;
            // 1 = masculino, 2 = femenino
            String key = genero == 2 ? "mujeres" : "varones";
            data.get(mes).merge(key, horas, Double::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, valores) -> {
            Map<String, Object> row = new HashMap<>(valores);
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 3: Horas por departamento académico (carrera.departamento) ───

    public List<Map<String, Object>> horasPorDepartamento(int anio) {
        // Construir mapa puId -> departamento a partir de EstudianteCarrera
        Map<Long, String> puDepartamento = new HashMap<>();
        estudianteCarreraRepo.findAll().forEach(ec -> {
            if (ec.getCarrera() != null && ec.getCarrera().getDepartamento() != null) {
                puDepartamento.put(ec.getPacienteUniversitario().getId(),
                                   ec.getCarrera().getDepartamento());
            }
        });

        List<SesionPaciente> sesiones = sesionRepo.findByFechaBetween(
            LocalDateTime.of(anio, 1, 1, 0, 0),
            LocalDateTime.of(anio, 12, 31, 23, 59)
        );

        // Acumular horas: mes -> (departamento -> horas)
        Map<Integer, Map<String, Double>> data = new TreeMap<>();
        for (int m = 1; m <= 12; m++) {
            data.put(m, new HashMap<>());
        }

        for (SesionPaciente s : sesiones) {
            if (s.getDuracionMinutos() == null || s.getDuracionMinutos() <= 0) continue;
            Long puId = s.getPacienteUniversitario().getId();
            String dep = puDepartamento.getOrDefault(puId, "Sin departamento");
            int mes = s.getFecha().getMonthValue();
            double horas = s.getDuracionMinutos() / 60.0;
            data.get(mes).merge(dep, horas, Double::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, valores) -> {
            Map<String, Object> row = new HashMap<>(valores);
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 4: Número de casos por gravedad ─────────────────────────────

    public List<Map<String, Object>> casosPorGravedad() {
        List<HistorialClinico> historiales = historialRepo.findAll();
        Map<String, Long> conteo = new LinkedHashMap<>();
        conteo.put("leve", 0L);
        conteo.put("moderado", 0L);
        conteo.put("grave", 0L);
        conteo.put("muy grave", 0L);
        conteo.put("riesgo de vida", 0L);

        for (HistorialClinico hc : historiales) {
            if (hc.getGravedad() == null) continue;
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

    // ── Gráfica 5: Tipologías de atención por género ─────────────────────────

    public List<Map<String, Object>> tipologiasPorGenero() {
        List<HistorialClinico> historiales = historialRepo.findAll();
        Map<String, long[]> conteo = new TreeMap<>(); // [masculino, femenino]

        for (HistorialClinico hc : historiales) {
            if (hc.getTipologia() == null) continue;
            Integer genero = null;
            try {
                genero = hc.getSesionPaciente()
                            .getPacienteUniversitario()
                            .getPaciente()
                            .getGenero();
            } catch (Exception ignored) {}

            if (genero == null) continue;

            List<String> tipologias = parseTipologias(hc.getTipologia());
            for (String tip : tipologias) {
                String key = tip.trim();
                conteo.computeIfAbsent(key, k -> new long[]{0L, 0L});
                if (genero == 2) conteo.get(key)[1]++;  // femenino
                else             conteo.get(key)[0]++;  // masculino
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

    // ── Gráfica 6: Participantes por carrera ────────────────────────────────

    public List<Map<String, Object>> participantesPorCarrera() {
        Map<String, Long> conteo = new TreeMap<>();
        estudianteCarreraRepo.findAll().forEach(ec -> {
            if (ec.getCarrera() == null) return;
            String carrera = ec.getCarrera().getCarrera();
            conteo.merge(carrera, 1L, Long::sum);
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

    // ── Gráfica 7: Horas ejecutadas vs designadas (totales por mes) ─────────

    public List<Map<String, Object>> horasEjecutadasVsDesignadas(int anio) {
        List<SesionPaciente> sesiones = sesionRepo.findByFechaBetween(
            LocalDateTime.of(anio, 1, 1, 0, 0),
            LocalDateTime.of(anio, 12, 31, 23, 59)
        );

        Map<Integer, double[]> data = new TreeMap<>(); // [designadas, ejecutadas]
        for (int m = 1; m <= 12; m++) data.put(m, new double[]{0.0, 0.0});

        for (SesionPaciente s : sesiones) {
            if (s.getDuracionMinutos() == null || s.getDuracionMinutos() <= 0) continue;
            int mes = s.getFecha().getMonthValue();
            data.get(mes)[1] += s.getDuracionMinutos() / 60.0;
        }

        horasDesignadasRepo.findByAnio(anio).forEach(h ->
            data.get(h.getMes())[0] += h.getHoras()
        );

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            row.put("designadas", Math.round(v[0] * 10.0) / 10.0);
            row.put("ejecutadas", Math.round(v[1] * 10.0) / 10.0);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 8: Sesiones por mes ──────────────────────────────────────────

    public List<Map<String, Object>> sesionesPorMes(int anio) {
        List<SesionPaciente> sesiones = sesionRepo.findByFechaBetween(
            LocalDateTime.of(anio, 1, 1, 0, 0),
            LocalDateTime.of(anio, 12, 31, 23, 59)
        );

        Map<Integer, Long> conteo = new TreeMap<>();
        for (int m = 1; m <= 12; m++) conteo.put(m, 0L);
        for (SesionPaciente s : sesiones) conteo.merge(s.getFecha().getMonthValue(), 1L, Long::sum);

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((mes, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 9: Sesiones por psicólogo ────────────────────────────────────

    public List<Map<String, Object>> sesionesPorPsicologo(int anio) {
        List<SesionPaciente> sesiones = sesionRepo.findByFechaBetween(
            LocalDateTime.of(anio, 1, 1, 0, 0),
            LocalDateTime.of(anio, 12, 31, 23, 59)
        );

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

    // ── Gráfica 10: Score promedio (estrés/ansiedad/depresión) por mes ────────

    public List<Map<String, Object>> scorePromedio(int anio) {
        List<EntrevistaPsicologica> entrevistas = entrevistaRepo.findAll();

        Map<Integer, long[][]> data = new TreeMap<>(); // mes -> [[sum,count] x3]
        for (int m = 1; m <= 12; m++) data.put(m, new long[][]{{0,0},{0,0},{0,0}});

        for (EntrevistaPsicologica e : entrevistas) {
            if (e.getSesionPaciente() == null || e.getSesionPaciente().getFecha() == null) continue;
            if (e.getSesionPaciente().getFecha().getYear() != anio) continue;
            int mes = e.getSesionPaciente().getFecha().getMonthValue();
            if (e.getTotalScoreEstres()   != null) { data.get(mes)[0][0] += e.getTotalScoreEstres();   data.get(mes)[0][1]++; }
            if (e.getTotalScoreAnsiedad() != null) { data.get(mes)[1][0] += e.getTotalScoreAnsiedad(); data.get(mes)[1][1]++; }
            if (e.getTotalScoreDepresion()!= null) { data.get(mes)[2][0] += e.getTotalScoreDepresion();data.get(mes)[2][1]++; }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            row.put("estres",    v[0][1] > 0 ? Math.round((double) v[0][0] / v[0][1] * 10.0) / 10.0 : 0);
            row.put("ansiedad",  v[1][1] > 0 ? Math.round((double) v[1][0] / v[1][1] * 10.0) / 10.0 : 0);
            row.put("depresion", v[2][1] > 0 ? Math.round((double) v[2][0] / v[2][1] * 10.0) / 10.0 : 0);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 11: Semestre de pacientes universitarios ─────────────────────

    public List<Map<String, Object>> semestres() {
        Map<Integer, Long> conteo = new TreeMap<>();
        pacienteUniversitarioRepo.findAll().forEach(pu -> {
            if (pu.getSemestre() == null) return;
            conteo.merge(pu.getSemestre(), 1L, Long::sum);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((sem, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("semestre", "Sem. " + sem);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 12: Nuevos pacientes por mes ─────────────────────────────────

    public List<Map<String, Object>> nuevosPacientesPorMes(int anio) {
        Map<Integer, long[]> data = new TreeMap<>(); // [universitarios, externos]
        for (int m = 1; m <= 12; m++) data.put(m, new long[]{0, 0});

        entrevistaRepo.findAll().forEach(e -> {
            if (e.getFecha() == null || e.getFecha().getYear() != anio) return;
            data.get(e.getFecha().getMonthValue())[0]++;
        });

        orientacionRepo.findAll().forEach(o -> {
            if (o.getFecha() == null || o.getFecha().getYear() != anio) return;
            data.get(o.getFecha().getMonthValue())[1]++;
        });

        List<Map<String, Object>> result = new ArrayList<>();
        data.forEach((mes, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("mes", NOMBRE_MES[mes]);
            row.put("mesNumero", mes);
            row.put("universitarios", v[0]);
            row.put("externos", v[1]);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 13: Distribución por género ──────────────────────────────────

    public List<Map<String, Object>> distribucionGenero() {
        Map<String, Long> conteo = new LinkedHashMap<>();
        conteo.put("Masculino", 0L);
        conteo.put("Femenino", 0L);
        conteo.put("Sin especificar", 0L);

        pacienteRepo.findAll().forEach(p -> {
            if      (p.getGenero() == null) conteo.merge("Sin especificar", 1L, Long::sum);
            else if (p.getGenero() == 1)    conteo.merge("Masculino",       1L, Long::sum);
            else if (p.getGenero() == 2)    conteo.merge("Femenino",        1L, Long::sum);
            else                            conteo.merge("Sin especificar", 1L, Long::sum);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        conteo.forEach((genero, total) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("genero", genero);
            row.put("total", total);
            result.add(row);
        });
        return result;
    }

    // ── Gráfica 14: Distribución por edad (rangos) ───────────────────────────

    public List<Map<String, Object>> distribucionEdad() {
        String[] rangos = {"< 15", "15-17", "18-20", "21-23", "24-26", "27-30", "31+"};
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (String r : rangos) conteo.put(r, 0L);

        pacienteRepo.findAll().forEach(p -> {
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
            row.put("rango", rango);
            row.put("total", total);
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
