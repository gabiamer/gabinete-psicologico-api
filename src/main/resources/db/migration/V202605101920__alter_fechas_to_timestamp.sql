ALTER TABLE actividad_psicologo ALTER COLUMN fecha_inicio TYPE TIMESTAMP USING fecha_inicio::TIMESTAMP;
ALTER TABLE actividad_psicologo ALTER COLUMN fecha_fin TYPE TIMESTAMP USING fecha_fin::TIMESTAMP;
