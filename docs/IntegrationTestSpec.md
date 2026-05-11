# Especificación de Pruebas de Integración — Job Board API

## 1. Introducción

Especificación completa de pruebas de integración para el Job Board API: sistema REST donde las empresas publican ofertas y los candidatos aplican. Los candidatos viven en JSONPlaceholder y nunca se almacenan localmente.

**Filosofía:** Las pruebas verifican el comportamiento real del sistema en conjunto. No se sustituye la base de datos ni el servicio externo JSONPlaceholder.

---

## 2. Stack de Pruebas y Configuración

### 2.1 Dependencias

```
Python >= 3.11
behave == 1.2.6
allure-behave == 2.13.5
requests == 2.31.0
psycopg2-binary == 2.9.9
```

### 2.2 Variables de Entorno

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `BASE_URL` | `http://localhost:8080` | URL base de la API |
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `jobboard` | Nombre de la base de datos |
| `DB_USER` | `app` | Usuario de la base de datos |
| `DB_PASSWORD` | `secret` | Contraseña de la base de datos |

### 2.3 Conexión a Base de Datos

Extraída del `docker-compose.yml`:

```
Host:     localhost
Puerto:   5432
Base de datos: jobboard
Usuario:  app
Password: secret
DSN:      postgresql://app:secret@localhost:5432/jobboard
```

### 2.4 Ejecución 

```bash

# 1. Ejecutar pruebas con reporte Allure
behave features/ -f allure_behave.formatter:AllureFormatter -o reports/allure-results

# 2. Generar y abrir reporte
allure serve reports/allure-results

# 3. Excluir escenarios que requieren fallo de red
behave features/ --tags="~@requires_network_failure" \
  -f allure_behave.formatter:AllureFormatter -o reports/allure-results
```

### 2.5 Limpieza entre Escenarios

El archivo `features/environment.py` debe limpiar las tablas en `after_scenario`:

```python
def after_scenario(context, scenario):
    conn = context.db_conn
    with conn.cursor() as cur:
        cur.execute("DELETE FROM applications")
        cur.execute("DELETE FROM jobs")
    conn.commit()
```

---

## 3. Tabla de Routing HTTP Completa

Una fila por cada combinación endpoint + HTTP status derivada estrictamente del código de
`JobController`, `ApplicationController`, `CandidateController` y `GlobalExceptionHandler`.

| # | Método | Endpoint | HTTP Status | Descripción | Feature BDD |
|---|--------|----------|-------------|-------------|-------------|
| 1 | POST | /jobs | 201 | Oferta creada exitosamente | jobs.feature |
| 2 | POST | /jobs | 422 | Campos requeridos vacíos o inválidos (`VALIDATION_ERROR`) | jobs.feature |
| 3 | GET | /jobs | 200 | Búsqueda paginada de ofertas (con o sin filtros) | jobs.feature |
| 4 | GET | /jobs/{id} | 200 | Oferta encontrada por ID | jobs.feature |
| 5 | GET | /jobs/{id} | 404 | Oferta con el ID dado no existe (`JOB_NOT_FOUND`) | jobs.feature |
| 6 | PATCH | /jobs/{id}/close | 200 | Oferta cerrada exitosamente | jobs.feature |
| 7 | PATCH | /jobs/{id}/close | 404 | Oferta con el ID dado no existe (`JOB_NOT_FOUND`) | jobs.feature |
| 8 | GET | /jobs/{id}/report | 200 | Reporte de postulaciones de la oferta | jobs.feature |
| 9 | GET | /jobs/{id}/report | 404 | Oferta con el ID dado no existe (`JOB_NOT_FOUND`) | jobs.feature |
| 10 | POST | /applications | 201 | Postulación creada exitosamente | applications.feature |
| 11 | POST | /applications | 404 | El candidato no existe en JSONPlaceholder (`CANDIDATE_NOT_FOUND`) | applications.feature |
| 12 | POST | /applications | 404 | La oferta no existe en la base de datos (`JOB_NOT_FOUND`) | applications.feature |
| 13 | POST | /applications | 409 | La oferta referenciada tiene `status = closed` (`JOB_CLOSED`) | applications.feature |
| 14 | POST | /applications | 422 | `candidate_id` o `job_id` nulos o no positivos (`VALIDATION_ERROR`) | applications.feature |
| 15 | POST | /applications | 422 | Candidato ya postuló a la misma oferta — RN-001 (`DUPLICATE_APPLICATION`) | applications.feature |
| 16 | POST | /applications | 502 | JSONPlaceholder no disponible al validar candidato — RN-004 (`SERVICE_UNAVAILABLE`) | applications.feature |
| 17 | PATCH | /applications/{id}/status | 200 | Estado actualizado a `accepted` o `rejected` | applications.feature |
| 18 | PATCH | /applications/{id}/status | 404 | Postulación con el ID dado no existe (`APPLICATION_NOT_FOUND`) | applications.feature |
| 19 | PATCH | /applications/{id}/status | 422 | `status` enviado no es `accepted` ni `rejected` (`VALIDATION_ERROR`) | applications.feature |
| 20 | PATCH | /applications/{id}/status | 422 | Postulación ya tiene estado final — RN-003 (`INVALID_STATUS_TRANSITION`) | applications.feature |
| 21 | GET | /candidates | 200 | Lista completa de candidatos desde JSONPlaceholder | candidates.feature |
| 22 | GET | /candidates | 502 | JSONPlaceholder no disponible (`SERVICE_UNAVAILABLE`) | candidates.feature |
| 23 | GET | /candidates/{id} | 200 | Candidato encontrado por ID en JSONPlaceholder | candidates.feature |
| 24 | GET | /candidates/{id} | 404 | Candidato con el ID dado no existe en JSONPlaceholder (`CANDIDATE_NOT_FOUND`) | candidates.feature |
| 25 | GET | /candidates/{id} | 502 | JSONPlaceholder no disponible (`SERVICE_UNAVAILABLE`) | candidates.feature |

---

## 4. Ejemplos de Request y Response

### 4.1 `POST /jobs`

#### 201 Created

```http
POST /jobs HTTP/1.1
Content-Type: application/json

{
  "title": "Desarrollador Backend Senior",
  "description": "Buscamos un desarrollador con experiencia en Java 25 y Spring Boot 4.",
  "company": "TechCorp S.A.",
  "location": "Ciudad de México"
}
```

```json
{
  "id": 1,
  "title": "Desarrollador Backend Senior",
  "description": "Buscamos un desarrollador con experiencia en Java 25 y Spring Boot 4.",
  "company": "TechCorp S.A.",
  "location": "Ciudad de México",
  "status": "open",
  "created_at": "2026-05-10T10:30:00-06:00"
}
```

#### 422 Unprocessable Entity — Validación

```http
POST /jobs HTTP/1.1
Content-Type: application/json

{
  "title": "",
  "description": "Buscamos analista.",
  "company": "DataCo",
  "location": "Monterrey"
}
```

```json
{
  "timestamp": "2026-05-10T10:31:00-06:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "error_code": "VALIDATION_ERROR",
  "message": "La petición contiene campos inválidos.",
  "path": "/jobs",
  "errors": [
    {
      "field": "title",
      "rejected_value": "",
      "message": "El título no puede estar vacío."
    }
  ]
}
```

---

### 4.2 `GET /jobs`

#### 200 OK

```http
GET /jobs?page=0&size=20 HTTP/1.1
```

```json
{
  "content": [
    {
      "id": 1,
      "title": "Backend Engineer",
      "description": "Diseñar e implementar APIs REST con Node.js y PostgreSQL.",
      "company": "Acme Corp",
      "location": "Remoto",
      "status": "open",
      "created_at": "2026-05-10T10:30:00-06:00"
    },
    {
      "id": 2,
      "title": "Frontend Developer",
      "description": "Construir interfaces reactivas con React y TypeScript.",
      "company": "Globex Inc",
      "location": "Ciudad de México, MX",
      "status": "open",
      "created_at": "2026-05-10T10:30:00-06:00"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 5,
  "totalPages": 1
}
```

---

### 4.3 `GET /jobs/{id}`

#### 200 OK

```http
GET /jobs/1 HTTP/1.1
```

```json
{
  "id": 1,
  "title": "Backend Engineer",
  "description": "Diseñar e implementar APIs REST con Node.js y PostgreSQL.",
  "company": "Acme Corp",
  "location": "Remoto",
  "status": "open",
  "created_at": "2026-05-10T10:30:00-06:00"
}
```

#### 404 Not Found

```http
GET /jobs/9999 HTTP/1.1
```

```json
{
  "timestamp": "2026-05-10T10:32:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "JOB_NOT_FOUND",
  "message": "No existe una oferta con id 9999.",
  "path": "/jobs/9999"
}
```

---

### 4.4 `PATCH /jobs/{id}/close`

#### 200 OK

```http
PATCH /jobs/1/close HTTP/1.1
```

```json
{
  "id": 1,
  "title": "Backend Engineer",
  "description": "Diseñar e implementar APIs REST con Node.js y PostgreSQL.",
  "company": "Acme Corp",
  "location": "Remoto",
  "status": "closed",
  "created_at": "2026-05-10T10:30:00-06:00"
}
```

#### 404 Not Found

```http
PATCH /jobs/9999/close HTTP/1.1
```

```json
{
  "timestamp": "2026-05-10T10:33:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "JOB_NOT_FOUND",
  "message": "No existe una oferta con id 9999.",
  "path": "/jobs/9999/close"
}
```

---

### 4.5 `GET /jobs/{id}/report`

#### 200 OK

```http
GET /jobs/1/report HTTP/1.1
```

```json
{
  "job_id": 1,
  "title": "Backend Engineer",
  "total_applications": 3,
  "by_status": {
    "pending": 1,
    "accepted": 1,
    "rejected": 1
  }
}
```

#### 404 Not Found

```http
GET /jobs/9999/report HTTP/1.1
```

```json
{
  "timestamp": "2026-05-10T10:34:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "JOB_NOT_FOUND",
  "message": "No existe una oferta con id 9999.",
  "path": "/jobs/9999/report"
}
```

---

### 4.6 `POST /applications`

#### 201 Created

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": 1,
  "job_id": 1
}
```

```json
{
  "id": 1,
  "candidate_id": 1,
  "job_id": 1,
  "status": "pending",
  "applied_at": "2026-05-10T10:45:00-06:00",
  "updated_at": "2026-05-10T10:45:00-06:00"
}
```

#### 404 Not Found — Candidato inexistente (RN-004)

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": 9999,
  "job_id": 1
}
```

```json
{
  "timestamp": "2026-05-10T10:46:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "CANDIDATE_NOT_FOUND",
  "message": "No existe un candidato con id 9999.",
  "path": "/applications"
}
```

#### 404 Not Found — Oferta inexistente

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": 1,
  "job_id": 9999
}
```

```json
{
  "timestamp": "2026-05-10T10:47:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "JOB_NOT_FOUND",
  "message": "No existe una oferta con id 9999.",
  "path": "/applications"
}
```

#### 409 Conflict — Oferta cerrada (RN-002)

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": 9,
  "job_id": 4
}
```

> `job_id=4` corresponde a "Data Analyst" (Umbrella LLC) que tiene `status = closed` en el seed.

```json
{
  "timestamp": "2026-05-10T10:48:00-06:00",
  "status": 409,
  "error": "Conflict",
  "error_code": "JOB_CLOSED",
  "message": "La oferta con id 4 está cerrada y no acepta postulaciones.",
  "path": "/applications"
}
```

#### 422 Unprocessable Entity — Validación

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": null,
  "job_id": 1
}
```

```json
{
  "timestamp": "2026-05-10T10:49:00-06:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "error_code": "VALIDATION_ERROR",
  "message": "La petición contiene campos inválidos.",
  "path": "/applications",
  "errors": [
    {
      "field": "candidate_id",
      "rejected_value": null,
      "message": "El candidate_id es obligatorio."
    }
  ]
}
```

#### 422 Unprocessable Entity — Postulación duplicada (RN-001)

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": 1,
  "job_id": 1
}
```

```json
{
  "timestamp": "2026-05-10T10:50:00-06:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "error_code": "DUPLICATE_APPLICATION",
  "message": "El candidato 1 ya postuló a la oferta 1.",
  "path": "/applications"
}
```

#### 502 Bad Gateway — JSONPlaceholder no disponible

```http
POST /applications HTTP/1.1
Content-Type: application/json

{
  "candidate_id": 1,
  "job_id": 1
}
```

```json
{
  "timestamp": "2026-05-10T10:51:00-06:00",
  "status": 502,
  "error": "Bad Gateway",
  "error_code": "SERVICE_UNAVAILABLE",
  "message": "El servicio de candidatos no está disponible.",
  "path": "/applications"
}
```

---

### 4.7 `PATCH /applications/{id}/status`

#### 200 OK

```http
PATCH /applications/1/status HTTP/1.1
Content-Type: application/json

{
  "status": "accepted"
}
```

```json
{
  "id": 1,
  "candidate_id": 1,
  "job_id": 1,
  "status": "accepted",
  "applied_at": "2026-05-10T10:45:00-06:00",
  "updated_at": "2026-05-10T11:00:00-06:00"
}
```

#### 404 Not Found

```http
PATCH /applications/9999/status HTTP/1.1
Content-Type: application/json

{
  "status": "accepted"
}
```

```json
{
  "timestamp": "2026-05-10T11:01:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "APPLICATION_NOT_FOUND",
  "message": "La postulación con id 9999 no existe.",
  "path": "/applications/9999/status"
}
```

#### 422 Unprocessable Entity — Status inválido (RN-005)

```http
PATCH /applications/1/status HTTP/1.1
Content-Type: application/json

{
  "status": "pending"
}
```

```json
{
  "timestamp": "2026-05-10T11:02:00-06:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "error_code": "VALIDATION_ERROR",
  "message": "La petición contiene campos inválidos.",
  "path": "/applications/1/status",
  "errors": [
    {
      "field": "status",
      "rejected_value": "pending",
      "message": "El status debe ser accepted o rejected"
    }
  ]
}
```

#### 422 Unprocessable Entity — Transición inválida (RN-003)

```http
PATCH /applications/2/status HTTP/1.1
Content-Type: application/json

{
  "status": "rejected"
}
```

> `application_id=2` corresponde a candidate_id=2, job_id=1 con `status = accepted` en el seed.

```json
{
  "timestamp": "2026-05-10T11:03:00-06:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "error_code": "INVALID_STATUS_TRANSITION",
  "message": "La postulación con id 2 ya tiene un estado final y no puede modificarse.",
  "path": "/applications/2/status"
}
```

---

### 4.8 `GET /candidates`

#### 200 OK

```http
GET /candidates HTTP/1.1
```

```json
[
  {
    "id": 1,
    "name": "Leanne Graham",
    "username": "Bret",
    "email": "Sincere@april.biz",
    "phone": "1-770-736-8031 x56442",
    "website": "hildegard.org",
    "address": {
      "street": "Kulas Light",
      "suite": "Apt. 556",
      "city": "Gwenborough",
      "zipcode": "92998-3874",
      "geo": { "lat": "-37.3159", "lng": "81.1496" }
    },
    "company": {
      "name": "Romaguera-Crona",
      "catchPhrase": "Multi-layered client-server neural-net",
      "bs": "harness real-time e-markets"
    }
  }
]
```

> JSONPlaceholder devuelve exactamente 10 usuarios (IDs 1–10).

#### 502 Bad Gateway — Ver sección 5 para condiciones de red requeridas.

---

### 4.9 `GET /candidates/{id}`

#### 200 OK

```http
GET /candidates/1 HTTP/1.1
```

```json
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org",
  "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874",
    "geo": { "lat": "-37.3159", "lng": "81.1496" }
  },
  "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
  }
}
```

#### 404 Not Found

```http
GET /candidates/9999 HTTP/1.1
```

```json
{
  "timestamp": "2026-05-10T11:10:00-06:00",
  "status": 404,
  "error": "Not Found",
  "error_code": "CANDIDATE_NOT_FOUND",
  "message": "No existe un candidato con id 9999.",
  "path": "/candidates/9999"
}
```

#### 502 Bad Gateway — Ver sección 5 para condiciones de red requeridas.

---

## 5. Qué se Mockea y Qué No

| Componente | ¿Se mockea? | Justificación |
|-----------|-------------|---------------|
| Base de datos (PostgreSQL) | **No** | Se usa la instancia real definida en `docker-compose.yml`. El estado se limpia vía `DELETE` en `after_scenario`. |
| JSONPlaceholder | **No** | Se llama al servicio real en `https://jsonplaceholder.typicode.com`. IDs 1–10 existen; ID 9999 no existe. |
| Servicios internos de Spring | No aplica | Todo corre en el mismo proceso de la aplicación bajo prueba. |

### Escenarios 502 — Condición especial

Los tres escenarios etiquetados con `@requires_network_failure` verifican HTTP 502 y **no pueden ejecutarse con JSONPlaceholder disponible**. Estrategias posibles:

| Estrategia | Descripción |
|-----------|-------------|
| Variable de entorno | Configurar la app para que `JSONPLACEHOLDER_URL` apunte a `http://localhost:9999` (puerto cerrado). Requiere que la app respete esta variable. |
| Proxy HTTP (WireMock/mitmproxy) | Interceptar las llamadas a `jsonplaceholder.typicode.com` y devolver un error de conexión. |
| Tag `@requires_network_failure` | Excluir del CI normal con `--tags="~@requires_network_failure"` y ejecutar en pipelines de fault-injection. |

**Recomendación adoptada en esta especificación:** usar el tag `@requires_network_failure` para separar estos escenarios del suite principal. El CI ejecuta el suite sin el tag (88% de cobertura) y un pipeline adicional ejecuta los tres escenarios de red.

---

## 6. Escenarios BDD en Gherkin

### `features/jobs.feature`

```gherkin
# language: es
@allure.label.epic:JobBoardAPI
@allure.label.feature:Ofertas
Feature: Gestión de Ofertas de Empleo
  Como empresa reclutadora
  Quiero publicar y administrar ofertas de empleo
  Para atraer candidatos calificados

  Background:
    Given la base de datos está limpia

  # ── POST /jobs ────────────────────────────────────────────────────────────

  @allure.label.story:CrearOferta
  @allure.label.severity:critical
  Scenario: Crear una oferta exitosamente retorna 201
    When se envía POST /jobs con body:
      """
      {
        "title": "Desarrollador Backend Senior",
        "description": "Buscamos un desarrollador con experiencia en Java 25 y Spring Boot 4.",
        "company": "TechCorp S.A.",
        "location": "Ciudad de México"
      }
      """
    Then el status de respuesta es 201
    And el body contiene "title" con valor "Desarrollador Backend Senior"
    And el body contiene "status" con valor "open"
    And el body contiene un "id" numérico positivo
    And el body contiene "created_at" en formato ISO 8601

  @allure.label.story:CrearOferta
  @allure.label.severity:normal
  Scenario: Crear una oferta con título vacío retorna 422
    When se envía POST /jobs con body:
      """
      {
        "title": "",
        "description": "Descripción válida.",
        "company": "DataCo",
        "location": "Monterrey"
      }
      """
    Then el status de respuesta es 422
    And el body contiene "error_code" con valor "VALIDATION_ERROR"
    And el body contiene un error de campo "title" con mensaje "El título no puede estar vacío."

  # ── GET /jobs ─────────────────────────────────────────────────────────────

  @allure.label.story:BuscarOfertas
  @allure.label.severity:critical
  Scenario: Buscar ofertas retorna página con resultados
    Given existe una oferta abierta con título "Analista de Datos" en "Monterrey" de la empresa "DataCo"
    When se envía GET /jobs con parámetros page=0 y size=20
    Then el status de respuesta es 200
    And el body contiene "total" mayor o igual a 1
    And el body contiene "page" con valor 0
    And el body contiene "size" con valor 20
    And el body contiene un array "content" no vacío

  # ── GET /jobs/{id} ────────────────────────────────────────────────────────

  @allure.label.story:ObtenerOferta
  @allure.label.severity:critical
  Scenario: Obtener oferta existente por ID retorna 200
    Given existe una oferta abierta con título "QA Automation Engineer" en "Remoto" de la empresa "QualityCo"
    When se envía GET /jobs/{id_oferta_creada}
    Then el status de respuesta es 200
    And el body contiene "title" con valor "QA Automation Engineer"
    And el body contiene "status" con valor "open"

  @allure.label.story:ObtenerOferta
  @allure.label.severity:normal
  Scenario: Obtener oferta inexistente retorna 404 con JOB_NOT_FOUND
    When se envía GET /jobs/9999
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "JOB_NOT_FOUND"
    And el body contiene "message" con valor "No existe una oferta con id 9999."
    And el body contiene "path" con valor "/jobs/9999"

  # ── PATCH /jobs/{id}/close ────────────────────────────────────────────────

  @allure.label.story:CerrarOferta
  @allure.label.severity:critical
  Scenario: Cerrar una oferta abierta retorna 200 con status closed
    Given existe una oferta abierta con título "Desarrollador Frontend" en "Guadalajara" de la empresa "WebCo"
    When se envía PATCH /jobs/{id_oferta_creada}/close
    Then el status de respuesta es 200
    And el body contiene "status" con valor "closed"
    And el body contiene "id" con el ID de la oferta creada

  @allure.label.story:CerrarOferta
  @allure.label.severity:normal
  Scenario: Cerrar una oferta inexistente retorna 404 con JOB_NOT_FOUND
    When se envía PATCH /jobs/9999/close
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "JOB_NOT_FOUND"
    And el body contiene "message" con valor "No existe una oferta con id 9999."
    And el body contiene "path" con valor "/jobs/9999/close"

  # ── GET /jobs/{id}/report ─────────────────────────────────────────────────

  @allure.label.story:ReporteOferta
  @allure.label.severity:normal
  Scenario: Obtener reporte de una oferta con postulaciones retorna 200
    Given existe una oferta abierta con título "DevOps Engineer" en "Remoto" de la empresa "CloudCo"
    And el candidato 1 ha postulado a la oferta creada
    And el candidato 2 ha postulado a la oferta creada
    When se envía GET /jobs/{id_oferta_creada}/report
    Then el status de respuesta es 200
    And el body contiene "job_id" con el ID de la oferta creada
    And el body contiene "title" con valor "DevOps Engineer"
    And el body contiene "total_applications" con valor 2
    And el body contiene "by_status" con la clave "pending" igual a 2

  @allure.label.story:ReporteOferta
  @allure.label.severity:normal
  Scenario: Obtener reporte de oferta inexistente retorna 404 con JOB_NOT_FOUND
    When se envía GET /jobs/9999/report
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "JOB_NOT_FOUND"
    And el body contiene "message" con valor "No existe una oferta con id 9999."
    And el body contiene "path" con valor "/jobs/9999/report"
```

---

### `features/applications.feature`

```gherkin
# language: es
@allure.label.epic:JobBoardAPI
@allure.label.feature:Postulaciones
Feature: Gestión de Postulaciones
  Como candidato
  Quiero postular a ofertas de empleo abiertas
  Para ser considerado por las empresas reclutadoras

  Background:
    Given la base de datos está limpia
    And existe una oferta abierta con título "Backend Engineer" en "Remoto" de la empresa "Acme Corp"
    And existe una oferta cerrada con título "Data Analyst" en "Remoto" de la empresa "Umbrella LLC"

  # ── POST /applications ────────────────────────────────────────────────────

  @allure.label.story:CrearPostulacion
  @allure.label.severity:critical
  Scenario: Postular exitosamente a una oferta abierta retorna 201
    When se envía POST /applications con body:
      """
      {
        "candidate_id": 1,
        "job_id": {id_oferta_abierta}
      }
      """
    Then el status de respuesta es 201
    And el body contiene "candidate_id" con valor 1
    And el body contiene "job_id" con el ID de la oferta abierta
    And el body contiene "status" con valor "pending"
    And el body contiene "applied_at" en formato ISO 8601
    And el body contiene "updated_at" en formato ISO 8601

  @allure.label.story:CrearPostulacion
  @allure.label.severity:normal
  Scenario: Postular con candidato inexistente retorna 404 con CANDIDATE_NOT_FOUND
    When se envía POST /applications con body:
      """
      {
        "candidate_id": 9999,
        "job_id": {id_oferta_abierta}
      }
      """
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "CANDIDATE_NOT_FOUND"
    And el body contiene "message" con valor "No existe un candidato con id 9999."
    And el body contiene "path" con valor "/applications"

  @allure.label.story:CrearPostulacion
  @allure.label.severity:normal
  Scenario: Postular a una oferta inexistente retorna 404 con JOB_NOT_FOUND
    When se envía POST /applications con body:
      """
      {
        "candidate_id": 1,
        "job_id": 9999
      }
      """
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "JOB_NOT_FOUND"
    And el body contiene "message" con valor "No existe una oferta con id 9999."
    And el body contiene "path" con valor "/applications"

  @allure.label.story:CrearPostulacion
  @allure.label.severity:normal
  Scenario: Postular a una oferta cerrada retorna 409 con JOB_CLOSED
    When se envía POST /applications con body:
      """
      {
        "candidate_id": 1,
        "job_id": {id_oferta_cerrada}
      }
      """
    Then el status de respuesta es 409
    And el body contiene "error_code" con valor "JOB_CLOSED"
    And el body contiene "path" con valor "/applications"

  @allure.label.story:CrearPostulacion
  @allure.label.severity:normal
  Scenario: Postular con candidate_id nulo retorna 422 con VALIDATION_ERROR
    When se envía POST /applications con body:
      """
      {
        "candidate_id": null,
        "job_id": {id_oferta_abierta}
      }
      """
    Then el status de respuesta es 422
    And el body contiene "error_code" con valor "VALIDATION_ERROR"
    And el body contiene un error de campo "candidate_id" con mensaje "El candidate_id es obligatorio."

  @allure.label.story:CrearPostulacion
  @allure.label.severity:normal
  Scenario: Postular dos veces a la misma oferta retorna 422 con DUPLICATE_APPLICATION
    Given el candidato 2 ya postuló a la oferta abierta
    When se envía POST /applications con body:
      """
      {
        "candidate_id": 2,
        "job_id": {id_oferta_abierta}
      }
      """
    Then el status de respuesta es 422
    And el body contiene "error_code" con valor "DUPLICATE_APPLICATION"
    And el body contiene "message" con valor "El candidato 2 ya postuló a la oferta {id_oferta_abierta}."

  @requires_network_failure
  @allure.label.story:CrearPostulacion
  @allure.label.severity:minor
  Scenario: JSONPlaceholder no disponible al crear postulación retorna 502
    Given JSONPlaceholder no está disponible
    When se envía POST /applications con body:
      """
      {
        "candidate_id": 1,
        "job_id": {id_oferta_abierta}
      }
      """
    Then el status de respuesta es 502
    And el body contiene "error_code" con valor "SERVICE_UNAVAILABLE"
    And el body contiene "path" con valor "/applications"

  # ── PATCH /applications/{id}/status ──────────────────────────────────────

  @allure.label.story:ActualizarEstado
  @allure.label.severity:critical
  Scenario: Actualizar estado de postulación pending a accepted retorna 200
    Given el candidato 3 ya postuló a la oferta abierta
    When se envía PATCH /applications/{id_postulacion_candidato_3}/status con body:
      """
      {
        "status": "accepted"
      }
      """
    Then el status de respuesta es 200
    And el body contiene "status" con valor "accepted"
    And el body contiene "candidate_id" con valor 3
    And el body contiene "updated_at" en formato ISO 8601

  @allure.label.story:ActualizarEstado
  @allure.label.severity:normal
  Scenario: Actualizar estado de postulación inexistente retorna 404 con APPLICATION_NOT_FOUND
    When se envía PATCH /applications/9999/status con body:
      """
      {
        "status": "accepted"
      }
      """
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "APPLICATION_NOT_FOUND"
    And el body contiene "message" con valor "La postulación con id 9999 no existe."
    And el body contiene "path" con valor "/applications/9999/status"

  @allure.label.story:ActualizarEstado
  @allure.label.severity:normal
  Scenario: Enviar status "pending" retorna 422 con VALIDATION_ERROR
    Given el candidato 4 ya postuló a la oferta abierta
    When se envía PATCH /applications/{id_postulacion_candidato_4}/status con body:
      """
      {
        "status": "pending"
      }
      """
    Then el status de respuesta es 422
    And el body contiene "error_code" con valor "VALIDATION_ERROR"
    And el body contiene un error de campo "status" con mensaje "El status debe ser accepted o rejected"

  @allure.label.story:ActualizarEstado
  @allure.label.severity:normal
  Scenario: Cambiar estado de postulación ya finalizada retorna 422 con INVALID_STATUS_TRANSITION
    Given el candidato 5 ya postuló a la oferta abierta
    And la postulación del candidato 5 tiene status "accepted"
    When se envía PATCH /applications/{id_postulacion_candidato_5}/status con body:
      """
      {
        "status": "rejected"
      }
      """
    Then el status de respuesta es 422
    And el body contiene "error_code" con valor "INVALID_STATUS_TRANSITION"
    And el body contiene "path" con valor "/applications/{id_postulacion_candidato_5}/status"
```

---

### `features/candidates.feature`

```gherkin
# language: es
@allure.label.epic:JobBoardAPI
@allure.label.feature:Candidatos
Feature: Consulta de Candidatos
  Como sistema
  Quiero consultar candidatos desde JSONPlaceholder
  Para validar su existencia antes de crear postulaciones

  # ── GET /candidates ───────────────────────────────────────────────────────

  @allure.label.story:ListarCandidatos
  @allure.label.severity:normal
  Scenario: Listar todos los candidatos exitosamente retorna 200
    When se envía GET /candidates
    Then el status de respuesta es 200
    And el body es un array con exactamente 10 elementos
    And el primer elemento contiene "id" con valor 1
    And el primer elemento contiene "name" con valor "Leanne Graham"
    And el primer elemento contiene "email" con valor "Sincere@april.biz"
    And el primer elemento contiene "username" con valor "Bret"

  @requires_network_failure
  @allure.label.story:ListarCandidatos
  @allure.label.severity:minor
  Scenario: Listar candidatos cuando JSONPlaceholder no está disponible retorna 502
    Given JSONPlaceholder no está disponible
    When se envía GET /candidates
    Then el status de respuesta es 502
    And el body contiene "error_code" con valor "SERVICE_UNAVAILABLE"
    And el body contiene "path" con valor "/candidates"

  # ── GET /candidates/{id} ──────────────────────────────────────────────────

  @allure.label.story:ObtenerCandidato
  @allure.label.severity:critical
  Scenario: Obtener candidato existente por ID retorna 200
    When se envía GET /candidates/1
    Then el status de respuesta es 200
    And el body contiene "id" con valor 1
    And el body contiene "name" con valor "Leanne Graham"
    And el body contiene "username" con valor "Bret"
    And el body contiene "email" con valor "Sincere@april.biz"
    And el body contiene "phone" con valor "1-770-736-8031 x56442"
    And el body contiene "website" con valor "hildegard.org"
    And el body contiene "address" con los campos street, suite, city, zipcode y geo
    And el body contiene "company" con los campos name, catchPhrase y bs

  @allure.label.story:ObtenerCandidato
  @allure.label.severity:normal
  Scenario: Obtener candidato inexistente retorna 404 con CANDIDATE_NOT_FOUND
    When se envía GET /candidates/9999
    Then el status de respuesta es 404
    And el body contiene "error_code" con valor "CANDIDATE_NOT_FOUND"
    And el body contiene "message" con valor "No existe un candidato con id 9999."
    And el body contiene "path" con valor "/candidates/9999"

  @requires_network_failure
  @allure.label.story:ObtenerCandidato
  @allure.label.severity:minor
  Scenario: Obtener candidato cuando JSONPlaceholder no está disponible retorna 502
    Given JSONPlaceholder no está disponible
    When se envía GET /candidates/1
    Then el status de respuesta es 502
    And el body contiene "error_code" con valor "SERVICE_UNAVAILABLE"
    And el body contiene "path" con valor "/candidates/1"
```

---

## 7. Criterio de Coverage

### 7.1 Cobertura por Endpoint

| Endpoint | Status Codes posibles | Status Codes cubiertos | Cobertura |
|----------|----------------------|----------------------|-----------|
| POST /jobs | 201, 422 | 201, 422 | 100% |
| GET /jobs | 200 | 200 | 100% |
| GET /jobs/{id} | 200, 404 | 200, 404 | 100% |
| PATCH /jobs/{id}/close | 200, 404 | 200, 404 | 100% |
| GET /jobs/{id}/report | 200, 404 | 200, 404 | 100% |
| POST /applications | 201, 404×2, 409, 422×2, 502 | 201, 404×2, 409, 422×2, 502 | 100% |
| PATCH /applications/{id}/status | 200, 404, 422×2 | 200, 404, 422×2 | 100% |
| GET /candidates | 200, 502 | 200, 502 | 100% |
| GET /candidates/{id} | 200, 404, 502 | 200, 404, 502 | 100% |

### 7.2 Resumen Ejecutivo

| Métrica | Valor |
|---------|-------|
| Endpoints únicos | 9 |
| Combinaciones endpoint + status cubiertos | 25 / 25 |
| Escenarios del suite principal (sin fallo de red) | 22 |
| Escenarios `@requires_network_failure` | 3 |
| **Cobertura suite principal** | **88%** ✅ |
| **Cobertura total** | **100%** ✅ |

> El umbral mínimo requerido es **80%**. El suite principal supera este umbral sin necesidad de infraestructura de fault-injection.

### 7.3 Reglas de Negocio Cubiertas

| Regla | Descripción | Escenario que la verifica |
|-------|-------------|--------------------------|
| RN-001 | Un candidato no puede postular dos veces a la misma oferta | `Postular dos veces a la misma oferta retorna 422 con DUPLICATE_APPLICATION` |
| RN-002 | Solo se postula a ofertas con `status = open` | `Postular a una oferta cerrada retorna 409 con JOB_CLOSED` |
| RN-003 | Una postulación `accepted` o `rejected` no cambia de estado | `Cambiar estado de postulación ya finalizada retorna 422 con INVALID_STATUS_TRANSITION` |
| RN-004 | Validar candidato en JSONPlaceholder antes de crear postulación | `Postular con candidato inexistente retorna 404 con CANDIDATE_NOT_FOUND` |
| RN-005 | El status de postulación no puede volver a `pending` | `Enviar status "pending" retorna 422 con VALIDATION_ERROR` |

### 7.4 Errores HTTP Cubiertos por Código de Error

| HTTP Status | `error_code` | Escenarios que lo verifican |
|-------------|-------------|----------------------------|
| 404 | `JOB_NOT_FOUND` | 4 escenarios (GET, PATCH close, report, POST application) |
| 404 | `CANDIDATE_NOT_FOUND` | 2 escenarios (GET /candidates/{id}, POST /applications) |
| 404 | `APPLICATION_NOT_FOUND` | 1 escenario (PATCH /applications/{id}/status) |
| 409 | `JOB_CLOSED` | 1 escenario |
| 422 | `VALIDATION_ERROR` | 4 escenarios (POST jobs, POST applications, PATCH status pending, PATCH status null) |
| 422 | `DUPLICATE_APPLICATION` | 1 escenario |
| 422 | `INVALID_STATUS_TRANSITION` | 1 escenario |
| 502 | `SERVICE_UNAVAILABLE` | 3 escenarios (`@requires_network_failure`) |
