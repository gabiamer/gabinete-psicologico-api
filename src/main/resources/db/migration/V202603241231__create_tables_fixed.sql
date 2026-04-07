-- 1. Tablas independientes (Sin llaves foráneas)
CREATE TABLE IF NOT EXISTS person (
    id BIGSERIAL PRIMARY KEY,
    primer_nombre VARCHAR(100),
    segundo_nombre VARCHAR(100),
    apellido_paterno VARCHAR(100),
    apellido_materno VARCHAR(100),
    celular VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS carrera (
    id BIGSERIAL PRIMARY KEY,
    carrera VARCHAR(255),
    departamento VARCHAR(255)
);

-- 2. Tablas de primer nivel de dependencia
CREATE TABLE IF NOT EXISTS psicologo (
    id BIGSERIAL PRIMARY KEY,
    ocupacion VARCHAR(255),
    person_id BIGINT,
    CONSTRAINT fk_psicologo_person FOREIGN KEY (person_id) REFERENCES person(id)
);

CREATE TABLE IF NOT EXISTS paciente (
    id BIGSERIAL PRIMARY KEY,
    domicilio VARCHAR(255),
    edad INT,
    estado_civil VARCHAR(50),
    fecha_nacimiento DATE,
    tipo_paciente VARCHAR(50),
    person_id BIGINT,
    CONSTRAINT fk_paciente_person FOREIGN KEY (person_id) REFERENCES person(id)
);

-- 3. Tablas de segundo nivel de dependencia
CREATE TABLE IF NOT EXISTS paciente_universitario (
    id BIGSERIAL PRIMARY KEY,
    derivado_por VARCHAR(255),
    semestre INT,
    paciente_id BIGINT,
    psicologo_id BIGINT,
    CONSTRAINT fk_univ_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id),
    CONSTRAINT fk_univ_psicologo FOREIGN KEY (psicologo_id) REFERENCES psicologo(id)
);

CREATE TABLE IF NOT EXISTS paciente_externo (
    id BIGSERIAL PRIMARY KEY,
    anio INT,
    correo VARCHAR(255),
    escuela VARCHAR(255),
    paciente_id BIGINT,
    CONSTRAINT fk_ext_paciente FOREIGN KEY (paciente_id) REFERENCES paciente(id)
);

-- 4. Tablas dependientes de los tipos de paciente
CREATE TABLE IF NOT EXISTS estudiante_carrera (
    id BIGSERIAL PRIMARY KEY,
    carrera_id BIGINT,
    paciente_universitario_id BIGINT,
    CONSTRAINT fk_ec_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(id),
    CONSTRAINT fk_ec_paciente_univ FOREIGN KEY (paciente_universitario_id) REFERENCES paciente_universitario(id)
);

CREATE TABLE IF NOT EXISTS orientacion_vocacional (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP,
    observaciones TEXT,
    respuestas JSONB,
    paciente_externo_id BIGINT,
    CONSTRAINT fk_ov_paciente_ext FOREIGN KEY (paciente_externo_id) REFERENCES paciente_externo(id)
);

CREATE TABLE IF NOT EXISTS sesion_paciente (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP,
    tipo VARCHAR(100),
    paciente_universitario_id BIGINT,
    psicologo_id BIGINT,
    CONSTRAINT fk_sesion_pac_univ FOREIGN KEY (paciente_universitario_id) REFERENCES paciente_universitario(id),
    CONSTRAINT fk_sesion_psicologo FOREIGN KEY (psicologo_id) REFERENCES psicologo(id)
);

-- 5. Tablas dependientes de las sesiones
CREATE TABLE IF NOT EXISTS historial_clinico (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP,
    historia TEXT,
    nro_sesion INT,
    tipologia JSONB,
    gravedad VARCHAR(100),
    sesion_paciente_id BIGINT,
    CONSTRAINT fk_hc_sesion FOREIGN KEY (sesion_paciente_id) REFERENCES sesion_paciente(id)
);

CREATE TABLE IF NOT EXISTS entrevista_psicologica (
    id BIGSERIAL PRIMARY KEY,
    antecedentes TEXT,
    fecha TIMESTAMP,
    habitos JSONB,
    sintomas JSONB,
    total_score_ansiedad INT,
    total_score_depresion INT,
    total_score_estres INT,
    version INT,
    sesion_paciente_id BIGINT,
    historia_familiar JSONB,
    relato_universidad JSONB,
    acuerdos JSONB,
    CONSTRAINT fk_ep_sesion FOREIGN KEY (sesion_paciente_id) REFERENCES sesion_paciente(id)
);