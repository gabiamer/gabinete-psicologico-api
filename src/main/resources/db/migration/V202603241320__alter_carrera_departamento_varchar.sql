DO $$
BEGIN
  IF EXISTS (
    SELECT 1 
    FROM information_schema.columns
    WHERE table_name = 'carrera'
      AND column_name = 'departamento'
      AND data_type = 'integer'
  ) THEN
    ALTER TABLE carrera
      ALTER COLUMN departamento TYPE VARCHAR(100)
      USING departamento::VARCHAR;
  END IF;
END $$;