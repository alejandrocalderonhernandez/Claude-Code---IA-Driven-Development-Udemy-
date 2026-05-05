-- =============================================================
-- Job Board API — Seed data
-- Cargado desde ~/data/jobs.sql y ~/data/applications.sql
-- =============================================================

-- -------------------------------------------------------------
-- ~/data/jobs.sql
-- -------------------------------------------------------------
INSERT INTO jobs (title, description, company, location, status) VALUES
('Backend Engineer',
 'Diseñar e implementar APIs REST con Node.js y PostgreSQL.',
 'Acme Corp',
 'Remoto',
 'open'),

('Frontend Developer',
 'Construir interfaces reactivas con React y TypeScript.',
 'Globex Inc',
 'Ciudad de México, MX',
 'open'),

('DevOps Engineer',
 'Gestionar infraestructura en AWS con Terraform y Kubernetes.',
 'Initech',
 'Guadalajara, MX',
 'open'),

('Data Analyst',
 'Análisis de datos de negocio con Python y SQL.',
 'Umbrella LLC',
 'Remoto',
 'closed'),

('QA Engineer',
 'Diseño y ejecución de pruebas automatizadas con Playwright.',
 'Acme Corp',
 'Remoto',
 'open');

-- -------------------------------------------------------------
-- ~/data/applications.sql
-- candidate_id referencia a JSONPlaceholder /users/{id} (IDs válidos: 1-10)
-- -------------------------------------------------------------
INSERT INTO applications (candidate_id, job_id, status) VALUES
(1, 1, 'pending'),    -- Usuario 1 → Backend Engineer
(2, 1, 'accepted'),   -- Usuario 2 → Backend Engineer
(3, 1, 'rejected'),   -- Usuario 3 → Backend Engineer
(1, 2, 'pending'),    -- Usuario 1 → Frontend Developer  (RN-001: distinta oferta, permitido)
(4, 2, 'pending'),    -- Usuario 4 → Frontend Developer
(5, 3, 'pending'),    -- Usuario 5 → DevOps Engineer
(6, 3, 'accepted'),   -- Usuario 6 → DevOps Engineer
(7, 5, 'pending'),    -- Usuario 7 → QA Engineer
(8, 5, 'rejected');   -- Usuario 8 → QA Engineer
-- Nota: job_id=4 (Data Analyst) está 'closed' → no se generan postulaciones nuevas (RN-002)