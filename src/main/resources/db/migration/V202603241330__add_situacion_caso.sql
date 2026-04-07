ALTER TABLE paciente_universitario
  ADD COLUMN IF NOT EXISTS situacion_caso VARCHAR(100) DEFAULT 'Acompañamiento psicológico';