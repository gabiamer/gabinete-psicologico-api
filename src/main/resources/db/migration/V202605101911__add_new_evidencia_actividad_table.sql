-- Tabla para múltiples imágenes por actividad
CREATE TABLE actividad_evidencia (
    id                      BIGSERIAL PRIMARY KEY,
    actividad_psicologo_id  BIGINT NOT NULL REFERENCES actividad_psicologo(id) ON DELETE CASCADE,
    imagen                  BYTEA NOT NULL,
    nombre                  VARCHAR(255) NOT NULL
);

-- Quitar columnas de evidencia única de la tabla principal
ALTER TABLE actividad_psicologo DROP COLUMN IF EXISTS evidencia;
ALTER TABLE actividad_psicologo DROP COLUMN IF EXISTS evidencia_nombre;
