-- =============================================================
-- Job Board API — Schema
-- Basado en PRD v1. Entidades: jobs, applications
-- =============================================================

-- Extensión para generación de UUIDs (disponible en pg >= 13 sin extensión adicional)
-- Usamos SERIAL / BIGSERIAL por simplicidad en dev; en producción considerar uuid_generate_v4()

-- -------------------------------------------------------------
-- ENUM: estado de una oferta de trabajo
-- PRD: status open | closed
-- -------------------------------------------------------------
CREATE TYPE job_status AS ENUM ('open', 'closed');

-- -------------------------------------------------------------
-- ENUM: estado de una postulación
-- PRD: status pending | accepted | rejected
-- -------------------------------------------------------------
CREATE TYPE application_status AS ENUM ('pending', 'accepted', 'rejected');

-- -------------------------------------------------------------
-- TABLE: jobs — Ofertas de trabajo
-- -------------------------------------------------------------
CREATE TABLE jobs (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(255)    NOT NULL,
    description TEXT            NOT NULL,
    company     VARCHAR(255)    NOT NULL,
    location    VARCHAR(255)    NOT NULL,
    status      job_status      NOT NULL DEFAULT 'open',
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- TABLE: applications — Postulaciones
-- -------------------------------------------------------------
CREATE TABLE applications (
    id            BIGSERIAL           PRIMARY KEY,
    candidate_id  BIGINT              NOT NULL,           -- ID externo: JSONPlaceholder /users/{id}
    job_id        BIGINT              NOT NULL
                  REFERENCES jobs(id) ON DELETE RESTRICT, -- no borrar oferta con postulaciones
    status        application_status  NOT NULL DEFAULT 'pending',
    applied_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW(),

    -- RN-001: un candidato solo puede postular una vez a la misma oferta
    CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id)
);

-- -------------------------------------------------------------
-- ÍNDICES
-- -------------------------------------------------------------

-- Filtrar / listar ofertas por estado (FEAT-005)
CREATE INDEX idx_jobs_status ON jobs (status);

-- Listar ofertas ordenadas por fecha (FEAT-005 — paginación por cursor)
CREATE INDEX idx_jobs_created_at ON jobs (created_at DESC);

-- Reporte de postulaciones por oferta (FEAT-006)
CREATE INDEX idx_applications_job_id ON applications (job_id);

-- Consultar historial de postulaciones de un candidato externo
CREATE INDEX idx_applications_candidate_id ON applications (candidate_id);

-- Filtrar postulaciones por estado dentro de una oferta (FEAT-006)
CREATE INDEX idx_applications_job_status ON applications (job_id, status);

-- -------------------------------------------------------------
-- TRIGGER: mantener updated_at actualizado automáticamente
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_applications_updated_at
BEFORE UPDATE ON applications
FOR EACH ROW EXECUTE FUNCTION set_updated_at();