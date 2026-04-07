-- Cambiar columna departamento en carrera de INT a VARCHAR
ALTER TABLE carrera
  ALTER COLUMN departamento TYPE VARCHAR(100) USING departamento::VARCHAR;
