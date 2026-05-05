# PRD — Job Board API

---

## 🎯 Visión del producto

Una API REST de reclutamiento donde las empresas publican ofertas de trabajo y los candidatos aplican a ellas.
Los candidatos viven en un servicio externo — nuestra plataforma los consulta en tiempo real sin almacenarlos.

---

## 👥 Actores del sistema

| Actor | Descripción |
|---|---|
| Empresa | Publica ofertas y gestiona el estado de las postulaciones |
| Candidato | Aplica a ofertas de trabajo |
| Servicio de Candidatos | JSONPlaceholder — fuente externa, solo lectura |

---

## 🗂 Entidades del dominio

### Job — Oferta de trabajo

Vive en nuestra base de datos.

| Campo | Descripción |
|---|---|
| id | Identificador único |
| title | Título del puesto |
| description | Descripción de la oferta |
| company | Nombre de la empresa |
| location | Ubicación o modalidad |
| status | `open` \| `closed` |
| created_at | Fecha de creación |

### Application — Postulación

Vive en nuestra base de datos.

| Campo | Descripción |
|---|---|
| id | Identificador único |
| candidate_id | ID externo — referencia a JSONPlaceholder |
| job_id | Referencia a la oferta |
| status | `pending` \| `accepted` \| `rejected` |
| applied_at | Fecha de postulación |
| updated_at | Fecha de último cambio de estado |

### Candidate — Candidato (servicio externo)

No vive en nuestra base de datos. Se consulta en tiempo real.

```
GET https://jsonplaceholder.typicode.com/users/{id}
GET https://jsonplaceholder.typicode.com/users
```

La respuesta del servicio incluye los siguientes campos: `id`, `name`, `username`, `email`, `phone`, `website`, `address`, `company`

---

## 📋 Features a construir

| Feature | Descripción |
|---|---|
| FEAT-001 | Cliente HTTP del servicio de candidatos |
| FEAT-002 | CRUD de ofertas de trabajo |
| FEAT-003 | Aplicar a una oferta |
| FEAT-004 | Cambiar estado de postulación |
| FEAT-005 | Búsqueda y filtros de ofertas con paginación |
| FEAT-006 | Reporte de postulaciones por oferta |
| FEAT-007 | Manejo centralizado de errores |

---

## 🚦 Reglas de negocio

| Regla | Descripción |
|---|---|
| RN-001 | Un candidato solo puede postular una vez a la misma oferta |
| RN-002 | Solo se puede postular a ofertas con status `open` |
| RN-003 | Una postulación `accepted` o `rejected` no puede cambiar de estado |
| RN-004 | El candidato siempre se valida en JSONPlaceholder antes de crear una postulación |
| RN-005 | El status de postulación no puede volver a `pending` |

---

## 🚦 Estados de una postulación

```
POST /applications
        │
        ▼
    pending
    /       \
   ▼         ▼
accepted   rejected

PATCH status=accepted    PATCH status=rejected
```