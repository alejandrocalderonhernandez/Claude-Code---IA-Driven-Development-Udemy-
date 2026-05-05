# Job Board API — Entity Mapping

> Mapeo de entidades JPA para **Spring Boot 4** · **Spring Data JPA 2025.1** · **Hibernate 7** · **Java 25**
> Basado en `DatabaseSpec.md` — PRD v1.

---

## Índice

1. [Stack de versiones y dependencias](#1-stack-de-versiones-y-dependencias)
2. [Decisiones de diseño clave](#2-decisiones-de-diseño-clave)
3. [Enums de dominio](#3-enums-de-dominio)
4. [Entidad `Job`](#4-entidad-job)
5. [Entidad `Application`](#5-entidad-application)
6. [Repositories — Spring Data JPA](#6-repositories--spring-data-jpa)
7. [Configuración de persistencia](#7-configuración-de-persistencia-applicationyml)
8. [Dependencias Maven](#8-dependencias-maven-pomxml)
9. [Tabla de mapeo columna → campo Java](#9-tabla-de-mapeo-columna--campo-java)
10. [Decisiones destacadas y justificaciones](#10-decisiones-destacadas-y-justificaciones)

---

## 1. Stack de versiones y dependencias

| Componente | Versión | Notas |
|---|---|---|
| Java | **25** | LTS. Soporte de primera clase en Spring Boot 4. Se usan records, sealed classes y pattern matching. |
| Spring Boot | **4.0.x** | GA desde noviembre 2025. Basado en Spring Framework 7. |
| Spring Data JPA | **2025.1** | Alineado con Spring Boot 4. Derived queries usan JPQL strings (~3.5× throughput). |
| Hibernate | **7.x** | Jakarta Persistence 3.2. `jakarta.persistence.*` — ya no `javax.persistence.*`. |
| Jakarta Persistence | **3.2** | Namespace `jakarta.*` obligatorio desde Spring Boot 3; se mantiene en Boot 4. |
| PostgreSQL Driver | **42.7.x** | JDBC 4.3. Compatible con `PostgreSQLEnumJdbcType`. |
| JSpecify | **1.0** | Anotaciones de null-safety adoptadas portfolio-wide en Spring Framework 7. |

> **Importante:** desde Spring Boot 3.0 todos los imports son `jakarta.persistence.*`, `jakarta.validation.*`. El namespace `javax.*` ya no existe en este stack.

---

## 2. Decisiones de diseño clave

| Decisión | Elección | Justificación |
|---|---|---|
| **Tipo de clase de entidad** | `class` mutable con Lombok | JPA/Hibernate requiere constructor sin args y setters para proxies y lazy loading. Los `record` de Java son inmutables y no son adecuados como entidades JPA. |
| **Estrategia de ID** | `GenerationType.IDENTITY` | Mapea directamente a `BIGSERIAL` de PostgreSQL. Simple y sin overhead de secuencias separadas en dev. |
| **Enum PostgreSQL** | `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` | Hibernate 6/7 nativo. Mapea al tipo ENUM declarado con `CREATE TYPE` en PostgreSQL, evitando VARCHAR. |
| **Timestamps** | `OffsetDateTime` | Mapea a `TIMESTAMPTZ` (UTC) de PostgreSQL. Más preciso que `LocalDateTime` para contextos multi-zona. |
| **`updated_at`** | Manejado por trigger en BD | El trigger `trg_applications_updated_at` del schema actualiza `updated_at` automáticamente. JPA lo lee con `@Column(insertable=false, updatable=false)`. |
| **Relación `Application → Job`** | `@ManyToOne(fetch = LAZY)` | Evita N+1 queries. Se carga explícitamente con JOIN FETCH cuando se necesita. |
| **DDL** | `ddl-auto: validate` | El schema vive en `db/init/01_schema.sql`. Hibernate solo valida, nunca modifica. |
| **Null-safety** | Anotaciones JSpecify | `@NonNull` / `@Nullable` de `org.jspecify.annotations` en todos los campos. |
| **Boilerplate** | Lombok | `@Getter`, `@Setter`, `@NoArgsConstructor`, `@ToString`, `@EqualsAndHashCode`. |

---

## 3. Enums de dominio

Los enums Java mapean 1:1 con los tipos `ENUM` declarados en PostgreSQL (`job_status`, `application_status`).

### `JobStatus.java`

```java
package com.jobboard.domain.enums;

/**
 * Mapea el tipo PostgreSQL: CREATE TYPE job_status AS ENUM ('open', 'closed')
 * Los valores deben coincidir exactamente (case-sensitive) con los del ENUM de PostgreSQL.
 */
public enum JobStatus {
    open,
    closed
}
```

### `ApplicationStatus.java`

```java
package com.jobboard.domain.enums;

/**
 * Mapea el tipo PostgreSQL: CREATE TYPE application_status AS ENUM ('pending', 'accepted', 'rejected')
 * Los valores deben coincidir exactamente (case-sensitive) con los del ENUM de PostgreSQL.
 */
public enum ApplicationStatus {
    pending,
    accepted,
    rejected
}
```

> **Nota sobre naming:** Los valores del ENUM en PostgreSQL son lowercase (`open`, `closed`, `pending`…).
> Los enums Java respetan ese casing para que `SqlTypes.NAMED_ENUM` haga el matching sin conversión adicional.
> Si se prefiere la convención Java uppercase (`OPEN`, `CLOSED`), se necesita un `AttributeConverter` que haga la transformación explícita — ver sección de decisiones.

---

## 4. Entidad `Job`

Mapea la tabla `jobs` del schema.

```java
package com.jobboard.domain.entity;

import com.jobboard.domain.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Entidad JPA que mapea la tabla {@code jobs}.
 *
 * <p>Notas de mapeo:
 * <ul>
 *   <li>{@code status} usa {@code SqlTypes.NAMED_ENUM} para mapear al tipo ENUM nativo de PostgreSQL.</li>
 *   <li>{@code created_at} es insertable pero no updatable: se establece al crear y nunca cambia.</li>
 *   <li>DDL gestionado por {@code db/init/01_schema.sql}. Hibernate solo valida ({@code ddl-auto: validate}).</li>
 * </ul>
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "applications")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Job {

    // ------------------------------------------------------------------
    // Identidad
    // ------------------------------------------------------------------

    /**
     * Clave primaria generada por PostgreSQL via BIGSERIAL.
     * {@code GenerationType.IDENTITY} delega la generación a la BD.
     * Es {@code null} antes de que el registro sea persistido (estado TRANSIENT).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private @Nullable Long id;

    // ------------------------------------------------------------------
    // Campos de negocio
    // ------------------------------------------------------------------

    /**
     * Título del puesto. VARCHAR(255) NOT NULL en BD.
     */
    @Column(name = "title", nullable = false, length = 255)
    private @NonNull String title;

    /**
     * Descripción detallada. TEXT NOT NULL en BD — sin límite de longitud en JPA.
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private @NonNull String description;

    /**
     * Nombre de la empresa. VARCHAR(255) NOT NULL.
     */
    @Column(name = "company", nullable = false, length = 255)
    private @NonNull String company;

    /**
     * Ubicación o modalidad. VARCHAR(255) NOT NULL.
     */
    @Column(name = "location", nullable = false, length = 255)
    private @NonNull String location;

    /**
     * Estado de la oferta. Mapea al tipo ENUM nativo de PostgreSQL {@code job_status}.
     *
     * <p>{@code @JdbcTypeCode(SqlTypes.NAMED_ENUM)} le indica a Hibernate 7 que use
     * el tipo ENUM declarado en PostgreSQL en lugar de VARCHAR.
     * Requiere que los valores del enum Java coincidan con los de {@code CREATE TYPE job_status}.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "job_status")
    private @NonNull JobStatus status = JobStatus.open;

    // ------------------------------------------------------------------
    // Auditoría
    // ------------------------------------------------------------------

    /**
     * Fecha de creación. TIMESTAMPTZ NOT NULL DEFAULT NOW() en BD.
     * {@code updatable = false}: nunca se modifica una vez insertado.
     * {@code insertable = true}: Hibernate puede enviarlo; si no se setea, la BD usa NOW().
     */
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private @NonNull OffsetDateTime createdAt = OffsetDateTime.now();

    // ------------------------------------------------------------------
    // Relaciones
    // ------------------------------------------------------------------

    /**
     * Postulaciones asociadas a esta oferta.
     * {@code FetchType.LAZY}: no se carga hasta acceder explícitamente.
     * {@code CascadeType.ALL} NO se usa: las postulaciones no deben borrarse
     * si se borra la oferta — la BD tiene ON DELETE RESTRICT.
     */
    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private java.util.List<Application> applications = new java.util.ArrayList<>();

}
```

---

## 5. Entidad `Application`

Mapea la tabla `applications` del schema.

```java
package com.jobboard.domain.entity;

import com.jobboard.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Entidad JPA que mapea la tabla {@code applications}.
 *
 * <p>Notas de mapeo:
 * <ul>
 *   <li>{@code candidate_id} es un ID externo (JSONPlaceholder). No hay FK local ni entidad Candidate.</li>
 *   <li>{@code job_id} → {@code @ManyToOne LAZY} hacia {@link Job}. FK con ON DELETE RESTRICT en BD.</li>
 *   <li>{@code updated_at} es manejado por el trigger {@code trg_applications_updated_at} en PostgreSQL.
 *       JPA lo declara con {@code insertable=false, updatable=false} para no sobrescribirlo.</li>
 *   <li>El UNIQUE constraint {@code uq_candidate_job (candidate_id, job_id)} vive en el schema SQL.
 *       JPA lo refleja con {@code @Table(uniqueConstraints=...)} solo para documentación/validación.</li>
 * </ul>
 */
@Entity
@Table(
    name = "applications",
    uniqueConstraints = {
        // Refleja CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id) — RN-001
        // La BD es la fuente de verdad; esto documenta la restricción a nivel de entidad.
        @UniqueConstraint(
            name = "uq_candidate_job",
            columnNames = {"candidate_id", "job_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "job")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Application {

    // ------------------------------------------------------------------
    // Identidad
    // ------------------------------------------------------------------

    /**
     * Clave primaria generada por BIGSERIAL.
     * {@code null} en estado TRANSIENT (antes de persistir).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private @Nullable Long id;

    // ------------------------------------------------------------------
    // Campos de negocio
    // ------------------------------------------------------------------

    /**
     * ID externo del candidato. Referencia lógica a JSONPlaceholder /users/{id}.
     * No existe FK en BD ni entidad local para Candidate — la validación
     * de existencia ocurre en la capa de servicio (RN-004).
     */
    @Column(name = "candidate_id", nullable = false, updatable = false)
    private @NonNull Long candidateId;

    /**
     * Relación ManyToOne hacia {@link Job}.
     *
     * <p>LAZY: la oferta no se carga hasta que se accede explícitamente.
     * Sin CascadeType: las operaciones sobre Application no se propagan a Job.
     * ON DELETE RESTRICT está en BD — no borrar ofertas con postulaciones.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "job_id",
        nullable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "fk_applications_job_id")
    )
    private @NonNull Job job;

    /**
     * Estado de la postulación. Mapea al ENUM nativo de PostgreSQL {@code application_status}.
     *
     * <p>Transiciones válidas (RN-003, RN-005):
     * {@code pending} → {@code accepted} | {@code rejected}.
     * Una vez en estado terminal, no cambia. Validación en capa de servicio.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "application_status")
    private @NonNull ApplicationStatus status = ApplicationStatus.pending;

    // ------------------------------------------------------------------
    // Auditoría
    // ------------------------------------------------------------------

    /**
     * Fecha de postulación. TIMESTAMPTZ NOT NULL DEFAULT NOW() en BD.
     * {@code updatable = false}: inmutable una vez creado.
     */
    @Column(name = "applied_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private @NonNull OffsetDateTime appliedAt = OffsetDateTime.now();

    /**
     * Fecha de última modificación. Actualizada automáticamente por el trigger
     * {@code trg_applications_updated_at} en PostgreSQL.
     *
     * <p>{@code insertable = false}: la BD asigna el valor inicial con NOW().
     * {@code updatable = false}: Hibernate nunca envía este campo en un UPDATE;
     * el trigger se encarga. JPA solo lo lee.
     */
    @Column(name = "updated_at", nullable = false,
            insertable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ")
    private @Nullable OffsetDateTime updatedAt;

}
```

---

## 6. Repositories — Spring Data JPA

### `JobRepository.java`

```java
package com.jobboard.domain.repository;

import com.jobboard.domain.entity.Job;
import com.jobboard.domain.enums.JobStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Repositorio para {@link Job}.
 *
 * <p>Spring Data JPA 2025.1 genera las derived queries como JPQL strings,
 * aprovechando el query cache de Hibernate (~3.5× throughput en dev).
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    // ------------------------------------------------------------------
    // FEAT-002: CRUD estándar
    // findById, save, deleteById → heredados de JpaRepository
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // FEAT-005: Búsqueda y filtros con paginación por cursor
    // Paginación por cursor sobre (created_at DESC, id DESC) para evitar
    // el problema de offset pagination con grandes volúmenes.
    // Usa el índice idx_jobs_created_at del schema.
    // ------------------------------------------------------------------

    /**
     * Lista todas las ofertas paginadas por cursor, ordenadas por created_at DESC.
     * Usa {@link Slice} para no disparar SELECT COUNT(*) innecesario — compatible
     * con la paginación por cursor del PRD.
     */
    @Query("""
        SELECT j FROM Job j
        WHERE (:status IS NULL OR j.status = :status)
        ORDER BY j.createdAt DESC, j.id DESC
        """)
    Slice<Job> findAllByStatusOrderByCreatedAtDesc(
            @Param("status") @org.jspecify.annotations.Nullable JobStatus status,
            @NonNull Pageable pageable
    );

    /**
     * Continuación del cursor: trae la siguiente página a partir del último
     * elemento visto (createdAt, id).
     * Aprovecha el índice compuesto {@code idx_jobs_created_at}.
     */
    @Query("""
        SELECT j FROM Job j
        WHERE (:status IS NULL OR j.status = :status)
          AND (j.createdAt < :cursorDate
               OR (j.createdAt = :cursorDate AND j.id < :cursorId))
        ORDER BY j.createdAt DESC, j.id DESC
        """)
    Slice<Job> findNextPageByStatusAndCursor(
            @Param("status") @org.jspecify.annotations.Nullable JobStatus status,
            @Param("cursorDate") @NonNull OffsetDateTime cursorDate,
            @Param("cursorId") @NonNull Long cursorId,
            @NonNull Pageable pageable
    );

}
```

---

### `ApplicationRepository.java`

```java
package com.jobboard.domain.repository;

import com.jobboard.domain.entity.Application;
import com.jobboard.domain.enums.ApplicationStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para {@link Application}.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // ------------------------------------------------------------------
    // FEAT-003: Aplicar a oferta
    // ------------------------------------------------------------------

    /**
     * Verifica RN-001: ¿ya existe una postulación de este candidato a esta oferta?
     * Usa el índice implícito del UNIQUE constraint {@code uq_candidate_job}.
     */
    boolean existsByCandidateIdAndJobId(
            @NonNull Long candidateId,
            @NonNull Long jobId
    );

    // ------------------------------------------------------------------
    // FEAT-006: Reporte de postulaciones por oferta
    // ------------------------------------------------------------------

    /**
     * Devuelve todas las postulaciones de una oferta con JOIN FETCH a Job
     * para evitar el problema N+1 al acceder a job.title / job.company.
     * Usa el índice {@code idx_applications_job_id}.
     */
    @Query("""
        SELECT a FROM Application a
        JOIN FETCH a.job j
        WHERE j.id = :jobId
        ORDER BY a.appliedAt DESC
        """)
    List<Application> findAllByJobIdWithJob(@Param("jobId") @NonNull Long jobId);

    /**
     * Cuenta postulaciones por oferta y estado para el resumen del reporte (FEAT-006).
     * Usa el índice compuesto {@code idx_applications_job_status}.
     */
    @Query("""
        SELECT a.status, COUNT(a)
        FROM Application a
        WHERE a.job.id = :jobId
        GROUP BY a.status
        """)
    List<Object[]> countByJobIdGroupByStatus(@Param("jobId") @NonNull Long jobId);

}
```

---

## 7. Configuración de persistencia (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jobboard
    username: app
    password: secret
    hikari:
      # Pool recomendado para virtual threads (Project Loom activo en Java 21+/25)
      # Spring Boot 4 auto-configura virtual threads en Java 21+
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000

  jpa:
    # NUNCA usar create/update en un schema gestionado por scripts SQL
    # El schema vive en db/init/01_schema.sql
    hibernate:
      ddl-auto: validate

    properties:
      hibernate:
        # Dialecto para PostgreSQL 16 — Hibernate 7 lo auto-detecta,
        # pero es recomendable declararlo explícitamente
        dialect: org.hibernate.dialect.PostgreSQLDialect

        # Mejora el debug: muestra SQL formateado en logs (desactivar en producción)
        format_sql: true

        # Estadísticas de Hibernate — útiles durante desarrollo
        generate_statistics: false

        # Desactivar Open Session in View: antipatrón en APIs REST
        # Spring Boot 4 lo desactiva por defecto; se documenta explícitamente
        open-in-view: false

        # Tamaño de batch para inserts/updates en lote
        jdbc:
          batch_size: 20

        # Ordenar inserts/updates por tipo de entidad para aprovechar batch
        order_inserts: true
        order_updates: true

    # Muestra SQL en consola (solo desarrollo)
    show-sql: false

  # Virtual Threads (Project Loom) — Spring Boot 4 los activa por defecto en Java 21+/25
  threads:
    virtual:
      enabled: true
```

---

## 8. Dependencias Maven (`pom.xml`)

```xml
<!-- Spring Boot 4 Parent — gestiona versiones de todo el stack -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version>
</parent>

<properties>
    <java.version>25</java.version>
</properties>

<dependencies>

    <!-- Spring Data JPA + Hibernate 7 + HikariCP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Driver PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Spring Web MVC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Validación (Jakarta Validation 3.x) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok — reducción de boilerplate en entidades -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- JSpecify — null-safety annotations (adoptadas en Spring Framework 7) -->
    <dependency>
        <groupId>org.jspecify</groupId>
        <artifactId>jspecify</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Tests -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

---

## 9. Tabla de mapeo columna → campo Java

### Tabla `jobs` → Entidad `Job`

| Columna BD | Tipo SQL | Campo Java | Tipo Java | Anotaciones clave |
|---|---|---|---|---|
| `id` | `BIGSERIAL PK` | `id` | `Long` | `@Id`, `@GeneratedValue(IDENTITY)`, `@Column(updatable=false)` |
| `title` | `VARCHAR(255) NOT NULL` | `title` | `String` | `@Column(nullable=false, length=255)` |
| `description` | `TEXT NOT NULL` | `description` | `String` | `@Column(nullable=false, columnDefinition="TEXT")` |
| `company` | `VARCHAR(255) NOT NULL` | `company` | `String` | `@Column(nullable=false, length=255)` |
| `location` | `VARCHAR(255) NOT NULL` | `location` | `String` | `@Column(nullable=false, length=255)` |
| `status` | `job_status ENUM` | `status` | `JobStatus` | `@Enumerated(STRING)`, `@JdbcTypeCode(NAMED_ENUM)`, `@Column(columnDefinition="job_status")` |
| `created_at` | `TIMESTAMPTZ NOT NULL` | `createdAt` | `OffsetDateTime` | `@Column(updatable=false)` |

---

### Tabla `applications` → Entidad `Application`

| Columna BD | Tipo SQL | Campo Java | Tipo Java | Anotaciones clave |
|---|---|---|---|---|
| `id` | `BIGSERIAL PK` | `id` | `Long` | `@Id`, `@GeneratedValue(IDENTITY)`, `@Column(updatable=false)` |
| `candidate_id` | `BIGINT NOT NULL` | `candidateId` | `Long` | `@Column(nullable=false, updatable=false)` — sin FK local |
| `job_id` | `BIGINT FK → jobs(id)` | `job` | `Job` | `@ManyToOne(LAZY)`, `@JoinColumn(name="job_id", updatable=false)` |
| `status` | `application_status ENUM` | `status` | `ApplicationStatus` | `@Enumerated(STRING)`, `@JdbcTypeCode(NAMED_ENUM)`, `@Column(columnDefinition="application_status")` |
| `applied_at` | `TIMESTAMPTZ NOT NULL` | `appliedAt` | `OffsetDateTime` | `@Column(updatable=false)` |
| `updated_at` | `TIMESTAMPTZ NOT NULL` | `updatedAt` | `OffsetDateTime` | `@Column(insertable=false, updatable=false)` — gestionado por trigger |

---

## 10. Decisiones destacadas y justificaciones

### 10.1 Por qué no usar `record` para las entidades

Java 25 popularizó los `record` para modelar datos inmutables. Sin embargo, **JPA requiere que las entidades sean clases mutables** con:
- Constructor sin argumentos (para instanciación por Hibernate al leer de BD).
- Setters o acceso por campo (para que Hibernate escriba los valores).
- Capacidad de ser extendidas por proxies CGLIB (para lazy loading).

Los `record` son `final` e inmutables — incompatibles con estos requisitos. Se usan `record` en los DTOs de la capa de API, no en las entidades.

---

### 10.2 Por qué `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` y no `@Enumerated(STRING)`

`@Enumerated(EnumType.STRING)` le dice a Hibernate que envíe el enum como `VARCHAR`. PostgreSQL acepta la comparación por casting implícito en algunos casos, pero en inserts estrictos lanza:

```
ERROR: column "status" is of type job_status but expression is of type character varying
```

`@JdbcTypeCode(SqlTypes.NAMED_ENUM)` (Hibernate 6+/7) usa `Types.OTHER` en JDBC, que es la forma correcta de enviar un valor al tipo ENUM nativo de PostgreSQL, coincidiendo exactamente con el tipo declarado en `CREATE TYPE`.

---

### 10.3 Por qué `OffsetDateTime` y no `LocalDateTime`

`TIMESTAMPTZ` en PostgreSQL almacena UTC internamente. `OffsetDateTime` preserva el offset de zona horaria en Java y es el tipo recomendado por Hibernate 6+ para este mapeo. `LocalDateTime` descarta la información de zona, lo que puede causar inconsistencias en entornos multi-zona o entre servidor y BD.

---

### 10.4 Por qué `updated_at` con `insertable=false, updatable=false`

El trigger `trg_applications_updated_at` en PostgreSQL es la fuente de verdad para `updated_at`. Si Hibernate intentara escribir este campo en un UPDATE, entraría en conflicto o sobrescribiría el valor del trigger. Al marcarlo `insertable=false, updatable=false`, JPA solo lee el campo — nunca lo envía en INSERT ni UPDATE. La BD siempre tiene el control.

---

### 10.5 Por qué `FetchType.LAZY` en `@ManyToOne`

El default de `@ManyToOne` en JPA es `EAGER`, lo que causaría que cada vez que se cargue una `Application`, se cargue también su `Job` con un JOIN adicional — incluso cuando no se necesita. `LAZY` evita este comportamiento. Cuando el `Job` es necesario (ej. en FEAT-006), se usa `JOIN FETCH` explícito en la query del repositorio.

---

### 10.6 `equals` y `hashCode` basados en `id`

`@EqualsAndHashCode(onlyExplicitlyIncluded = true)` con `@EqualsAndHashCode.Include` en `id` garantiza que:
- Dos entidades con el mismo `id` de BD son iguales.
- Entidades en estado TRANSIENT (id = null) no son iguales entre sí por defecto.
- No se accede a campos lazy en `equals/hashCode`, evitando LazyInitializationException.

---

*Documento generado para Job Board API — PRD v1 · Stack: Spring Boot 4 · Hibernate 7 · Java 25*