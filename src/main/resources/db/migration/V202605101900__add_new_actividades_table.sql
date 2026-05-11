CREATE TABLE actividad_psicologo (
    id              BIGSERIAL PRIMARY KEY,
    psicologo_id    BIGINT NOT NULL REFERENCES psicologo(id),
    titulo          VARCHAR(255) NOT NULL,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    objetivo        TEXT NOT NULL,
    poblacion       VARCHAR(255) NOT NULL,
    num_asistentes  INT NOT NULL,
    resumen         TEXT NOT NULL,
    resultados      TEXT NOT NULL,
    evidencia       BYTEA,
    evidencia_nombre VARCHAR(255),
    created_at      TIMESTAMP DEFAULT NOW()
);