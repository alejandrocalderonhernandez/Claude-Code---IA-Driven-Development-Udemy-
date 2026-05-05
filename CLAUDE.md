# CLAUDE.md — Job Board API

## Proyecto
API REST de reclutamiento: las empresas publican ofertas y los candidatos aplican.
Los candidatos viven en JSONPlaceholder — nunca se almacenan localmente.

---

## Documentos del proyecto
**Lee solo el documento relevante a la tarea actual**, la cual se te indicará en cada prompt.
Cada feature se incluirá en docs/tickets/{feature}, por ejemplo:
docs/tickets/FEAT-001.md
docs/tickets/FEAT-002.md
Existen archivos específicos de la arquitectura del proyecto en docs/, los cuales
te indicaré cuándo leer en caso de ser relevantes.

---

## Stack
- **Base de datos:** PostgreSQL 16
- **Schema:** solo en `db/init/` — el ORM nunca modifica DDL
- **Servicio externo:** `https://jsonplaceholder.typicode.com` (solo lectura)
- **Lenguaje/framework:** 
  - Java 25, 
  - Spring boot 4, 
  - Spring starter data JPA, 
  - Spring starter web
  - Project lombok

---

## Reglas de negocio — siempre vigentes

| Regla | Descripción |
|-------|-------------|
| RN-001 | Un candidato no puede postular dos veces a la misma oferta |
| RN-002 | Solo se postula a ofertas con `status = open` |
| RN-003 | Una postulación `accepted` o `rejected` no cambia de estado |
| RN-004 | Validar candidato en JSONPlaceholder antes de crear una postulación |
| RN-005 | El status de postulación no puede volver a `pending` |

Toda regla de negocio vive en la capa de servicio. Nunca en controladores ni repositorios.

---

## Prohibiciones
- No agregar campos que no estén en el archivo docs/PRDJobBoardAPI.md
- No crear endpoints fuera de los definidos en cada FEAT
- No usar `ddl-auto: create` ni `update` — el schema es inmutable desde código
- No ignorar RN-004 al crear postulaciones