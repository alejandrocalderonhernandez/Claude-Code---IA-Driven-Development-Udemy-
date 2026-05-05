# Job Board API — DTOs & Routing Model

> Modelado completo de DTOs (Request / Response / Error) y tabla de routing
> basada en el **Modelo de Madurez de Richardson (RMM)**.

---

## Índice

1. [Modelo de Madurez de Richardson — Nivel alcanzado](#1-modelo-de-madurez-de-richardson--nivel-alcanzado)
2. [Tabla de Routing](#2-tabla-de-routing)
3. [DTOs — Jobs](#3-dtos--jobs) · `CreateJobRequest` · `PatchJobStatusRequest` · `JobResponse` · `JobPageResponse`
4. [DTOs — Applications](#4-dtos--applications) · `CreateApplicationRequest` · `PatchApplicationStatusRequest` · `ApplicationResponse` · `ApplicationReportResponse`
5. [DTOs — Candidates (JSONPlaceholder)](#5-dtos--candidates-jsonplaceholder)
6. [DTOs — Errores](#6-dtos--errores)
7. [Resumen de códigos HTTP por operación](#7-resumen-de-códigos-http-por-operación)

---

## 1. Modelo de Madurez de Richardson — Nivel alcanzado

El **Modelo de Madurez de Richardson (RMM)** describe cuatro niveles de adopción REST:

| Nivel | Nombre | Descripción |
|---|---|---|
| **0** | The Swamp of POX | Un único endpoint, todo por POST |
| **1** | Resources | Endpoints separados por recurso |
| **2** | HTTP Verbs | Uso correcto de GET / POST / PUT / PATCH / DELETE + códigos HTTP semánticos |
| **3** | Hypermedia (HATEOAS) | Las respuestas incluyen links a las acciones posibles |

**Esta API se diseña en Nivel 2**, que es el estándar de facto para APIs REST de producción:

- ✅ Recursos identificados con URIs únicos (`/jobs`, `/jobs/{id}`, `/applications`, etc.)
- ✅ Verbos HTTP con semántica correcta (GET para leer, POST para crear, PATCH para modificar parcialmente, DELETE para eliminar)
- ✅ Códigos de estado HTTP semánticos (`200`, `201`, `204`, `400`, `404`, `409`, `422`, `500`)
- ❌ HATEOAS no se implementa (fuera del alcance del PRD)

---

## 2. Tabla de Routing

### 2.1 `/jobs` — Ofertas de trabajo (FEAT-002, FEAT-005)

| Método | URI | Descripción | Request Body | Response Body | Códigos HTTP | Feature |
|---|---|---|---|---|---|---|
| `GET` | `/jobs` | Listar ofertas con filtros y paginación | — | `JobPageResponse` | `200` | FEAT-005 |
| `POST` | `/jobs` | Crear nueva oferta | `CreateJobRequest` | `JobResponse` | `201`, `400`, `422` | FEAT-002 |
| `GET` | `/jobs/{id}` | Obtener oferta por ID | — | `JobResponse` | `200`, `404` | FEAT-002 |
| `PATCH` | `/jobs/{id}/status` | Cambiar solo el status de la oferta | `PatchJobStatusRequest` | `JobResponse` | `200`, `400`, `404`, `422` | FEAT-002 |
| `DELETE` | `/jobs/{id}` | Eliminar oferta (sin postulaciones) | — | — | `204`, `404`, `409` | FEAT-002 |

> **Nota sobre DELETE:** La BD tiene `ON DELETE RESTRICT` en `applications.job_id`. Si la oferta tiene postulaciones, se retorna `409 Conflict`.

---

### 2.2 `/applications` — Postulaciones (FEAT-003, FEAT-004, FEAT-006)

| Método | URI | Descripción | Request Body | Response Body | Códigos HTTP | Feature |
|---|---|---|---|---|---|---|
| `POST` | `/applications` | Crear postulación | `CreateApplicationRequest` | `ApplicationResponse` | `201`, `400`, `404`, `409`, `422` | FEAT-003 |
| `PATCH` | `/applications/{id}/status` | Cambiar estado de postulación | `PatchApplicationStatusRequest` | `ApplicationResponse` | `200`, `400`, `404`, `409`, `422` | FEAT-004 |
| `GET` | `/jobs/{id}/applications` | Reporte de postulaciones por oferta | — | `ApplicationReportResponse` | `200`, `404` | FEAT-006 |

> **Nota:** `PATCH /applications/{id}/status` es subrecurso explícito para evitar que el cliente envíe campos arbitrarios. Hace evidente en la URI que solo el estado es mutable.

---

### 2.3 `/candidates` — Candidatos vía proxy (FEAT-001)

| Método | URI | Descripción | Request Body | Response Body | Códigos HTTP | Feature |
|---|---|---|---|---|---|---|
| `GET` | `/candidates` | Listar todos los candidatos | — | `CandidateResponse[]` | `200`, `502` | FEAT-001 |
| `GET` | `/candidates/{id}` | Obtener candidato por ID | — | `CandidateResponse` | `200`, `404`, `502` | FEAT-001 |

> **Nota:** `502 Bad Gateway` se usa cuando JSONPlaceholder no responde o retorna error. El error se origina en un servicio upstream, no en nuestra API.

---

## 3. DTOs — Jobs

### 3.1 `CreateJobRequest`
Usado en: `POST /jobs`

```json
{
  "title": "Backend Engineer",
  "description": "Diseñar e implementar APIs REST con Node.js y PostgreSQL.",
  "company": "Acme Corp",
  "location": "Remoto"
}
```

| Campo | Tipo | Requerido | Validaciones |
|---|---|---|---|
| `title` | `string` | ✅ | No vacío, máx. 255 caracteres |
| `description` | `string` | ✅ | No vacío |
| `company` | `string` | ✅ | No vacío, máx. 255 caracteres |
| `location` | `string` | ✅ | No vacío, máx. 255 caracteres |

> `status` no se incluye en el request: toda oferta nueva nace como `open` (regla implícita del dominio).

---

### 3.2 `PatchJobStatusRequest`
Usado en: `PATCH /jobs/{id}/status`

```json
{
  "status": "closed"
}
```

| Campo | Tipo | Requerido | Validaciones |
|---|---|---|---|
| `status` | `enum` | ✅ | Valores permitidos: `open`, `closed` |

---

### 3.3 `JobResponse`
Retornado en: `POST /jobs` · `GET /jobs/{id}` · `PATCH /jobs/{id}/status`

```json
{
  "id": 1,
  "title": "Backend Engineer",
  "description": "Diseñar e implementar APIs REST con Node.js y PostgreSQL.",
  "company": "Acme Corp",
  "location": "Remoto",
  "status": "open",
  "created_at": "2025-07-15T14:30:00Z"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `integer` | Identificador único en BD |
| `title` | `string` | Título del puesto |
| `description` | `string` | Descripción de la oferta |
| `company` | `string` | Nombre de la empresa |
| `location` | `string` | Ubicación o modalidad |
| `status` | `enum` | `open` \| `closed` |
| `created_at` | `string (ISO 8601)` | Fecha de creación en UTC |

---

### 3.4 `JobPageResponse`
Retornado en: `GET /jobs`

```json
{
  "data": [
    {
      "id": 3,
      "title": "DevOps Engineer",
      "description": "Gestionar infraestructura en AWS con Terraform y Kubernetes.",
      "company": "Initech",
      "location": "Guadalajara, MX",
      "status": "open",
      "created_at": "2025-07-14T09:00:00Z"
    }
  ],
  "pagination": {
    "next_cursor": "2025-07-14T09:00:00Z_3",
    "has_more": true,
    "limit": 20
  }
}
```

**Query params aceptados:**

| Parámetro | Tipo | Requerido | Descripción |
|---|---|---|---|
| `status` | `enum` | ❌ | Filtrar por `open` o `closed` |
| `cursor` | `string` | ❌ | Cursor opaco de la página anterior |
| `limit` | `integer` | ❌ | Resultados por página. Default: `20`, máx: `100` |

| Campo | Tipo | Descripción |
|---|---|---|
| `data` | `JobResponse[]` | Lista de ofertas de la página actual |
| `pagination.next_cursor` | `string \| null` | Cursor para la siguiente página; `null` si no hay más resultados |
| `pagination.has_more` | `boolean` | `true` si existe al menos una página más |
| `pagination.limit` | `integer` | Tamaño de página aplicado |

---

## 4. DTOs — Applications

### 4.1 `CreateApplicationRequest`
Usado en: `POST /applications`

```json
{
  "candidate_id": 1,
  "job_id": 2
}
```

| Campo | Tipo | Requerido | Validaciones |
|---|---|---|---|
| `candidate_id` | `integer` | ✅ | Entero positivo. Se valida en JSONPlaceholder (RN-004) |
| `job_id` | `integer` | ✅ | Entero positivo. La oferta debe existir y estar `open` (RN-002) |

> `status` no se incluye: toda postulación nueva nace en `pending`.

---

### 4.2 `PatchApplicationStatusRequest`
Usado en: `PATCH /applications/{id}/status`

```json
{
  "status": "accepted"
}
```

| Campo | Tipo | Requerido | Validaciones |
|---|---|---|---|
| `status` | `enum` | ✅ | Valores permitidos: `accepted`, `rejected`. No se acepta `pending` (RN-005). Una postulación ya `accepted` o `rejected` no puede cambiar (RN-003) |

---

### 4.3 `ApplicationResponse`
Retornado en: `POST /applications` · `PATCH /applications/{id}/status`

```json
{
  "id": 5,
  "candidate_id": 1,
  "job_id": 2,
  "status": "pending",
  "applied_at": "2025-07-15T10:00:00Z",
  "updated_at": "2025-07-15T10:00:00Z"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `integer` | Identificador único en BD |
| `candidate_id` | `integer` | ID externo del candidato (JSONPlaceholder) |
| `job_id` | `integer` | ID de la oferta a la que se postula |
| `status` | `enum` | `pending` \| `accepted` \| `rejected` |
| `applied_at` | `string (ISO 8601)` | Fecha de creación de la postulación en UTC |
| `updated_at` | `string (ISO 8601)` | Fecha del último cambio de estado en UTC |

---

### 4.4 `ApplicationReportResponse`
Retornado en: `GET /jobs/{id}/applications`

```json
{
  "job": {
    "id": 1,
    "title": "Backend Engineer",
    "company": "Acme Corp",
    "status": "open"
  },
  "summary": {
    "total": 3,
    "pending": 1,
    "accepted": 1,
    "rejected": 1
  },
  "applications": [
    {
      "id": 1,
      "candidate_id": 1,
      "status": "pending",
      "applied_at": "2025-07-10T08:00:00Z",
      "updated_at": "2025-07-10T08:00:00Z"
    },
    {
      "id": 2,
      "candidate_id": 2,
      "status": "accepted",
      "applied_at": "2025-07-11T09:00:00Z",
      "updated_at": "2025-07-12T14:00:00Z"
    },
    {
      "id": 3,
      "candidate_id": 3,
      "status": "rejected",
      "applied_at": "2025-07-11T10:00:00Z",
      "updated_at": "2025-07-13T11:00:00Z"
    }
  ]
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `job.id` | `integer` | ID de la oferta |
| `job.title` | `string` | Título del puesto |
| `job.company` | `string` | Empresa |
| `job.status` | `enum` | Estado actual de la oferta |
| `summary.total` | `integer` | Total de postulaciones |
| `summary.pending` | `integer` | Postulaciones en estado `pending` |
| `summary.accepted` | `integer` | Postulaciones en estado `accepted` |
| `summary.rejected` | `integer` | Postulaciones en estado `rejected` |
| `applications` | `ApplicationSummaryItem[]` | Lista detallada de postulaciones |

**`ApplicationSummaryItem`** (elemento dentro de `applications`):

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `integer` | ID de la postulación |
| `candidate_id` | `integer` | ID del candidato |
| `status` | `enum` | Estado de la postulación |
| `applied_at` | `string (ISO 8601)` | Fecha de postulación |
| `updated_at` | `string (ISO 8601)` | Fecha del último cambio |

---

## 5. DTOs — Candidates (JSONPlaceholder)

Estos DTOs modelan la respuesta del servicio externo `https://jsonplaceholder.typicode.com/users`. Son de **solo lectura** — la plataforma no escribe en este servicio.

### 5.1 `CandidateAddressDto` (objeto anidado)

```json
{
  "street": "Kulas Light",
  "suite": "Apt. 556",
  "city": "Gwenborough",
  "zipcode": "92998-3874",
  "geo": {
    "lat": "-37.3159",
    "lng": "81.1496"
  }
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `street` | `string` | Calle |
| `suite` | `string` | Número interior / suite |
| `city` | `string` | Ciudad |
| `zipcode` | `string` | Código postal |
| `geo.lat` | `string` | Latitud geográfica |
| `geo.lng` | `string` | Longitud geográfica |

---

### 5.2 `CandidateCompanyDto` (objeto anidado)

```json
{
  "name": "Romaguera-Crona",
  "catchPhrase": "Multi-layered client-server neural-net",
  "bs": "harness real-time e-markets"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `name` | `string` | Nombre de la empresa donde trabaja el candidato |
| `catchPhrase` | `string` | Frase de la empresa |
| `bs` | `string` | Descripción de negocio |

---

### 5.3 `CandidateResponse`
Retornado en: `GET /candidates` · `GET /candidates/{id}`
Mapeado desde: `GET https://jsonplaceholder.typicode.com/users/{id}`

```json
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "phone": "1-770-736-0988 x56442",
  "website": "hildegard.org",
  "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874",
    "geo": {
      "lat": "-37.3159",
      "lng": "81.1496"
    }
  },
  "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
  }
}
```

| Campo | Tipo | Utilidad en el sistema |
|---|---|---|
| `id` | `integer` | ⭐ **Crítico** — se almacena como `candidate_id` en `applications` y es la clave de validación RN-004 |
| `name` | `string` | ⭐ **Alta** — presentación al cliente en respuestas enriquecidas |
| `email` | `string` | ⭐ **Alta** — identificación del candidato para el usuario final |
| `username` | `string` | Media — útil como alias de presentación |
| `phone` | `string` | Baja — dato de contacto, no procesado por el sistema |
| `website` | `string` | Baja — dato de perfil, no procesado por el sistema |
| `address` | `CandidateAddressDto` | Baja — dato de perfil, no procesado por el sistema |
| `company` | `CandidateCompanyDto` | Baja — dato de perfil, no procesado por el sistema |

---

## 6. DTOs — Errores

Todas las respuestas de error siguen una estructura uniforme gestionada por el `GlobalExceptionHandler` (FEAT-007).

### 6.1 `ErrorResponse` — Estructura base

```json
{
  "timestamp": "2025-07-15T14:35:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "JOB_NOT_FOUND",
  "message": "No existe una oferta con id 99.",
  "path": "/jobs/99"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `timestamp` | `string (ISO 8601)` | Momento exacto en que ocurrió el error (UTC) |
| `status` | `integer` | Código HTTP de la respuesta |
| `error` | `string` | Descripción estándar del código HTTP |
| `code` | `string` | Código interno de error (ver catálogo abajo) |
| `message` | `string` | Mensaje legible para el desarrollador |
| `path` | `string` | URI de la petición que originó el error |

---

### 6.2 `ValidationErrorResponse` — Errores de validación de campos (`422`)

Extiende `ErrorResponse` con un campo adicional `errors` que detalla qué campo falló y por qué.

```json
{
  "timestamp": "2025-07-15T14:36:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "VALIDATION_ERROR",
  "message": "La petición contiene campos inválidos.",
  "path": "/jobs",
  "errors": [
    {
      "field": "title",
      "rejected_value": "",
      "message": "El título no puede estar vacío."
    },
    {
      "field": "company",
      "rejected_value": null,
      "message": "El nombre de la empresa es obligatorio."
    }
  ]
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `errors` | `FieldError[]` | Lista de errores por campo |
| `errors[].field` | `string` | Nombre del campo que falló la validación |
| `errors[].rejected_value` | `any \| null` | Valor recibido que fue rechazado |
| `errors[].message` | `string` | Descripción del error de validación |

---

### 6.3 Catálogo de códigos de error internos

| `code` | HTTP | Descripción | Endpoint(s) |
|---|---|---|---|
| `VALIDATION_ERROR` | `422` | Uno o más campos del request son inválidos | Todos los endpoints con body |
| `JOB_NOT_FOUND` | `404` | No existe una oferta con el `id` indicado | `GET /jobs/{id}`, `PATCH /jobs/{id}/status`, `DELETE /jobs/{id}` |
| `APPLICATION_NOT_FOUND` | `404` | No existe una postulación con el `id` indicado | `PATCH /applications/{id}/status` |
| `CANDIDATE_NOT_FOUND` | `404` | El `candidate_id` no existe en JSONPlaceholder (RN-004) | `POST /applications` |
| `JOB_CLOSED` | `409` | La oferta tiene status `closed`; no se aceptan postulaciones (RN-002) | `POST /applications` |
| `DUPLICATE_APPLICATION` | `409` | El candidato ya tiene una postulación activa para esa oferta (RN-001) | `POST /applications` |
| `APPLICATION_STATUS_IMMUTABLE` | `409` | La postulación ya está en estado terminal `accepted` o `rejected` (RN-003) | `PATCH /applications/{id}/status` |
| `INVALID_STATUS_TRANSITION` | `422` | Se intentó transicionar a `pending` o un valor de status no permitido (RN-005) | `PATCH /applications/{id}/status` |
| `JOB_HAS_APPLICATIONS` | `409` | No se puede eliminar la oferta porque tiene postulaciones asociadas | `DELETE /jobs/{id}` |
| `CANDIDATE_SERVICE_UNAVAILABLE` | `502` | JSONPlaceholder no respondió o retornó error | `POST /applications`, `GET /candidates`, `GET /candidates/{id}` |
| `INTERNAL_SERVER_ERROR` | `500` | Error inesperado no controlado | Cualquier endpoint |

---

## 7. Resumen de códigos HTTP por operación

| Código | Semántica | Cuándo se usa en esta API |
|---|---|---|
| `200 OK` | Éxito, respuesta con cuerpo | GET (recurso), PATCH |
| `201 Created` | Recurso creado exitosamente | POST /jobs, POST /applications |
| `204 No Content` | Éxito, sin cuerpo de respuesta | DELETE /jobs/{id} |
| `400 Bad Request` | Petición malformada | JSON inválido, tipo de dato incorrecto |
| `404 Not Found` | Recurso no encontrado | Job, Application o Candidate inexistente |
| `409 Conflict` | Conflicto con el estado actual del recurso | RN-001, RN-002, RN-003, DELETE con dependencias |
| `422 Unprocessable Entity` | Sintaxis válida, semántica inválida | Validaciones de campos, RN-005 |
| `500 Internal Server Error` | Error inesperado del servidor | Fallo no controlado |
| `502 Bad Gateway` | Error en servicio upstream | JSONPlaceholder no disponible |

---

*Documento generado para Job Board API — PRD v1 · Arquitectura Nivel 2 (RMM)*