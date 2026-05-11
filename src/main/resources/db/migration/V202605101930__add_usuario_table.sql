CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'PSICOLOGO',
    activo BOOLEAN NOT NULL DEFAULT true,
    psicologo_id BIGINT,
    CONSTRAINT fk_usuario_psicologo FOREIGN KEY (psicologo_id) REFERENCES psicologo(id),
    CONSTRAINT uq_usuario_psicologo UNIQUE (psicologo_id)
);
