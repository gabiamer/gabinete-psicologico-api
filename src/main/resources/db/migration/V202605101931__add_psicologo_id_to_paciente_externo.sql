ALTER TABLE paciente_externo
ADD COLUMN psicologo_id BIGINT,
ADD CONSTRAINT fk_paciente_externo_psicologo FOREIGN KEY (psicologo_id) REFERENCES psicologo(id);
