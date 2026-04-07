package com.gabinete.psicologico_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabinete.psicologico_api.model.EntrevistaPsicologica;
import com.gabinete.psicologico_api.model.HistorialClinico;
import com.gabinete.psicologico_api.model.PacienteUniversitario;
import com.gabinete.psicologico_api.repository.EntrevistaPsicologicaRepository;
import com.gabinete.psicologico_api.repository.HistorialClinicoRepository;
import com.gabinete.psicologico_api.repository.PacienteUniversitarioRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ResumenIAService {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private PacienteUniversitarioRepository pacienteUniversitarioRepository;

    @Autowired
    private EntrevistaPsicologicaRepository entrevistaRepository;

    public Map<String, String> generarResumen(Long pacienteUniversitarioId) {
        PacienteUniversitario paciente = pacienteUniversitarioRepository.findById(pacienteUniversitarioId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        List<HistorialClinico> historiales = historialClinicoRepository
                .findBySesionPaciente_PacienteUniversitarioId(pacienteUniversitarioId);

        List<EntrevistaPsicologica> entrevistas = entrevistaRepository
                .findBySesionPaciente_PacienteUniversitarioId(pacienteUniversitarioId);

        if (historiales.isEmpty() && entrevistas.isEmpty()) {
            throw new RuntimeException("El paciente no tiene historial clínico ni entrevista registrada");
        }

        // ── Sección entrevista inicial ──────────────────────────────────────
        StringBuilder entrevistaText = new StringBuilder();
        if (!entrevistas.isEmpty()) {
            EntrevistaPsicologica ep = entrevistas.get(0);
            entrevistaText.append("=== ENTREVISTA PSICOLÓGICA INICIAL ===\n");
            if (ep.getAntecedentes() != null && !ep.getAntecedentes().isBlank())
                entrevistaText.append("Antecedentes: ").append(ep.getAntecedentes()).append("\n");
            if (ep.getHistoriaFamiliar() != null && !ep.getHistoriaFamiliar().isBlank())
                entrevistaText.append("Historia familiar: ").append(ep.getHistoriaFamiliar()).append("\n");
            if (ep.getRelatoUniversidad() != null && !ep.getRelatoUniversidad().isBlank())
                entrevistaText.append("Relato universidad: ").append(ep.getRelatoUniversidad()).append("\n");
            if (ep.getHabitos() != null && !ep.getHabitos().isBlank())
                entrevistaText.append("Hábitos: ").append(ep.getHabitos()).append("\n");
            if (ep.getSintomas() != null && !ep.getSintomas().isBlank())
                entrevistaText.append("Síntomas: ").append(ep.getSintomas()).append("\n");
            if (ep.getTotalScoreEstres() != null)
                entrevistaText.append("Score estrés: ").append(ep.getTotalScoreEstres()).append("\n");
            if (ep.getTotalScoreAnsiedad() != null)
                entrevistaText.append("Score ansiedad: ").append(ep.getTotalScoreAnsiedad()).append("\n");
            if (ep.getTotalScoreDepresion() != null)
                entrevistaText.append("Score depresión: ").append(ep.getTotalScoreDepresion()).append("\n");
            entrevistaText.append("\n");
        }

        // ── Sección notas de sesiones ───────────────────────────────────────
        StringBuilder historiasText = new StringBuilder();
        if (!historiales.isEmpty()) {
            historiales.sort((a, b) -> {
                if (a.getFecha() == null) return 1;
                if (b.getFecha() == null) return -1;
                return a.getFecha().compareTo(b.getFecha());
            });
            historiasText.append("=== NOTAS DE SESIONES ===\n");
            for (int i = 0; i < historiales.size(); i++) {
                HistorialClinico hc = historiales.get(i);
                int nro = hc.getNroSesion() != null ? hc.getNroSesion() : (i + 1);
                historiasText.append("--- Sesión ").append(nro).append(" ---\n");
                if (hc.getHistoria() != null && !hc.getHistoria().isBlank()) {
                    historiasText.append(hc.getHistoria()).append("\n\n");
                }
            }
        }

        String prompt = """
                Eres un psicólogo clínico experto en análisis de historias clínicas.
                A continuación se presenta la información clínica completa de un paciente universitario:

                %s
                %s

                Basándote en toda la información anterior, genera:
                1. Una "descripcion" breve del paciente (máximo 2 oraciones) que resuma su situación general.
                2. La "principal_problematica" que presenta el paciente (máximo 1 oración concisa).

                Responde ÚNICAMENTE con un JSON válido, sin markdown, sin texto adicional:
                {"descripcion": "...", "principal_problematica": "..."}
                """.formatted(entrevistaText.toString(), historiasText.toString());

        String aiResponse = chatModel.call(new Prompt(prompt))
                .getResult().getOutput().getText();

        String jsonStr = aiResponse.trim();
        int start = jsonStr.indexOf('{');
        int end = jsonStr.lastIndexOf('}');
        if (start >= 0 && end > start) {
            jsonStr = jsonStr.substring(start, end + 1);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonStr);
            String descripcion = json.has("descripcion") ? json.get("descripcion").asText() : "";
            String principalProblematica = json.has("principal_problematica") ? json.get("principal_problematica").asText() : "";

            paciente.setDescripcion(descripcion);
            paciente.setPrincipalProblematica(principalProblematica);
            pacienteUniversitarioRepository.save(paciente);

            return Map.of("descripcion", descripcion, "principalProblematica", principalProblematica);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar respuesta de IA: " + e.getMessage());
        }
    }
}
