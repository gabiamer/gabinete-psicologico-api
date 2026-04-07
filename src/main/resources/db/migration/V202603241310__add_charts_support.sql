-- Columna en sesion_paciente para soporte de graficas de horas
ALTER TABLE sesion_paciente
  ADD COLUMN IF NOT EXISTS duracion_minutos INT;

-- Tabla para registrar horas designadas AP por mes/turno (registro administrativo)
-- turno: 'manana' | 'tarde'
CREATE TABLE IF NOT EXISTS horas_designadas_ap (
  id            BIGSERIAL PRIMARY KEY,
  psicologo_id  BIGINT       NOT NULL REFERENCES psicologo(id),
  anio          INT          NOT NULL,
  mes           INT          NOT NULL CHECK (mes BETWEEN 1 AND 12),
  turno         VARCHAR(20)  NOT NULL,
  horas         INT          NOT NULL DEFAULT 0,
  CONSTRAINT uq_horas_ap UNIQUE (psicologo_id, anio, mes, turno)
);