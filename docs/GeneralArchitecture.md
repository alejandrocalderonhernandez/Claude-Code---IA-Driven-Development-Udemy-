# Job Board API — Diagrama de Arquitectura

## Diagrama

```mermaid
flowchart TD
    Client(["Cliente HTTP"])

    Client -->|HTTP REST| JC & AC & CC

    subgraph API["Capa de API — Controllers"]
        JC["JobController\n/jobs"]
        AC["ApplicationController\n/applications"]
        CC["CandidateController\n/candidates"]
    end

    subgraph SVC["Capa de Servicio — Lógica de negocio"]
        JS["JobService\nFEAT-002, 005"]
        AS["ApplicationService\nFEAT-003, 004, 006"]
        CS["CandidateService\nFEAT-001"]
    end

    subgraph REPO["Capa de Datos — Repositories"]
        JR["JobRepository"]
        AR["ApplicationRepository"]
    end

    subgraph DB["PostgreSQL"]
        T1[("jobs\nopen | closed")]
        T2[("applications\npending | accepted | rejected")]
    end

    EXT["JSONPlaceholder\n/users/{id}"]

    GEH["GlobalExceptionHandler — FEAT-007\nManejo centralizado de errores"]

    JC --> JS
    AC --> AS
    CC --> CS

    JS --> JR
    AS --> AR
    AS --> CS
    CS -->|HTTP GET| EXT

    JR --> T1
    AR --> T2

    API -.->|excepciones| GEH
    SVC -.->|excepciones| GEH
    REPO -.->|excepciones| GEH

    style Client fill:#D3D1C7,stroke:#5F5E5A,color:#2C2C2A
    style EXT fill:#F5C4B3,stroke:#993C1D,color:#4A1B0C
    style GEH fill:#FAC775,stroke:#854F0B,color:#412402
    style API fill:#EEEDFE,stroke:#534AB7,color:#26215C
    style SVC fill:#E1F5EE,stroke:#0F6E56,color:#04342C
    style REPO fill:#E6F1FB,stroke:#185FA5,color:#042C53
    style DB fill:#E6F1FB,stroke:#185FA5,color:#042C53
    style JC fill:#EEEDFE,stroke:#534AB7,color:#26215C
    style AC fill:#EEEDFE,stroke:#534AB7,color:#26215C
    style CC fill:#EEEDFE,stroke:#534AB7,color:#26215C
    style JS fill:#E1F5EE,stroke:#0F6E56,color:#04342C
    style AS fill:#E1F5EE,stroke:#0F6E56,color:#04342C
    style CS fill:#E1F5EE,stroke:#0F6E56,color:#04342C
    style JR fill:#E6F1FB,stroke:#185FA5,color:#042C53
    style AR fill:#E6F1FB,stroke:#185FA5,color:#042C53
    style T1 fill:#B5D4F4,stroke:#185FA5,color:#042C53
    style T2 fill:#B5D4F4,stroke:#185FA5,color:#042C53
```

---

## Resumen de componentes

### Cliente HTTP

Cualquier consumidor externo de la API: aplicación frontend, herramienta como Postman o un servicio de terceros. Se comunica exclusivamente mediante peticiones HTTP/REST contra los endpoints expuestos.

---

### Capa de API — Controllers

Punto de entrada de todas las peticiones. Cada controlador mapea rutas HTTP a métodos, valida la forma de la petición (tipos, campos requeridos) y delega la ejecución a la capa de servicio. No contiene lógica de negocio.

| Componente | Ruta base | Responsabilidad |
|---|---|---|
| `JobController` | `/jobs` | Expone CRUD de ofertas, búsqueda y filtros (FEAT-002, FEAT-005) |
| `ApplicationController` | `/applications` | Gestiona creación de postulaciones, cambio de estado y reporte (FEAT-003, FEAT-004, FEAT-006) |
| `CandidateController` | `/candidates` | Proxy hacia el servicio externo; permite consultar candidatos desde la API propia |

---

### Capa de Servicio — Lógica de negocio

Núcleo de la aplicación. Aquí se implementan y verifican todas las reglas de negocio antes de persistir o leer datos. Orquesta llamadas a repositorios y al cliente HTTP externo.

| Componente | Features | Reglas de negocio aplicadas |
|---|---|---|
| `JobService` | FEAT-002, FEAT-005 | Gestiona ciclo de vida de ofertas; aplica paginación y filtros por `status` |
| `ApplicationService` | FEAT-003, FEAT-004, FEAT-006 | RN-002 (solo ofertas `open`), RN-003 (estados terminales inmutables), RN-005 (no regresa a `pending`); invoca `CandidateService` para RN-004 |
| `CandidateService` | FEAT-001 | RN-004: valida existencia del candidato en JSONPlaceholder antes de crear una postulación |

---

### Capa de Datos — Repositories

Abstracción sobre la base de datos. Cada repositorio expone métodos tipados para consultar y persistir una entidad. Aísla la capa de servicio de los detalles de SQL y del ORM utilizado.

| Componente | Entidad | Operaciones principales |
|---|---|---|
| `JobRepository` | `jobs` | Buscar por `id`, listar con filtro de `status`, paginación por cursor sobre `created_at` |
| `ApplicationRepository` | `applications` | Insertar postulación, actualizar `status`, consultar por `job_id` o `candidate_id`, reporte agregado |

---

### PostgreSQL

Base de datos relacional que persiste las dos entidades del dominio propio del sistema.

| Tabla | Descripción |
|---|---|
| `jobs` | Almacena ofertas de trabajo con sus campos y el ENUM `job_status` (`open`, `closed`). Índices sobre `status` y `created_at DESC` para FEAT-005. |
| `applications` | Almacena postulaciones con referencia lógica a `candidate_id` (externo). ENUM `application_status` (`pending`, `accepted`, `rejected`). Constraint `UNIQUE (candidate_id, job_id)` para RN-001. Trigger `set_updated_at` mantiene `updated_at` sincronizado automáticamente. |

---

### JSONPlaceholder (servicio externo)

Fuente de verdad de los candidatos. La plataforma no almacena candidatos localmente; los consulta en tiempo real mediante peticiones HTTP GET a `https://jsonplaceholder.typicode.com/users/{id}`. Esta llamada ocurre en `CandidateService` como requisito previo a crear cualquier postulación (RN-004).

---

### GlobalExceptionHandler — FEAT-007

Componente transversal que intercepta todas las excepciones no controladas lanzadas en cualquier capa (API, Servicio, Repositorio). Centraliza la transformación de errores en respuestas HTTP con estructura uniforme, evitando que los detalles internos se filtren al cliente y garantizando códigos de estado consistentes (`400`, `404`, `409`, `422`, `500`).