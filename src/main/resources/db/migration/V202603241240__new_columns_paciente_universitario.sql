ALTER TABLE paciente_universitario
ADD COLUMN IF NOT EXISTS descripcion TEXT,
ADD COLUMN IF NOT EXISTS principal_problematica TEXT;