package com.debuggeandoideas.JobBoardAPI.mapper;

import com.debuggeandoideas.JobBoardAPI.dto.request.JobRequest;
import com.debuggeandoideas.JobBoardAPI.entity.JobEntity;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JobMapperTest {

    private final JobMapper jobMapper = new JobMapper();

    // ─── toEntity ────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapeaCorrectamenteTodosLosCamposDelRequest() {
        // Given
        var request = new JobRequest(
                "Backend Engineer",
                "Diseñar e implementar APIs REST.",
                "Acme Corp",
                "Remoto"
        );

        // When
        var entity = jobMapper.toEntity(request);

        // Then
        assertThat(entity.getTitle()).isEqualTo("Backend Engineer");
        assertThat(entity.getDescription()).isEqualTo("Diseñar e implementar APIs REST.");
        assertThat(entity.getCompany()).isEqualTo("Acme Corp");
        assertThat(entity.getLocation()).isEqualTo("Remoto");
    }

    @Test
    void toEntity_asignaStatusOpenPorDefectoAlCrearLaEntidad() {
        // Given
        var request = new JobRequest("QA Engineer", "Pruebas automatizadas.", "Globex", "Remoto");

        // When
        var entity = jobMapper.toEntity(request);

        // Then
        assertThat(entity.getStatus()).isEqualTo(JobStatus.open);
    }

    @Test
    void toEntity_asignaCreatedAtNoNuloPorDefecto() {
        // Given
        var request = new JobRequest("DevOps Engineer", "Gestionar infra en AWS.", "Initech", "CDMX");

        // When
        var entity = jobMapper.toEntity(request);

        // Then
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Test
    void toResponse_mapeaCorrectamenteTodosLosCamposDeLaEntidad() {
        // Given
        var now = OffsetDateTime.parse("2025-07-15T14:30:00Z");
        var entity = JobEntity.builder()
                .id(1L)
                .title("Backend Engineer")
                .description("Diseñar e implementar APIs REST.")
                .company("Acme Corp")
                .location("Remoto")
                .status(JobStatus.open)
                .createdAt(now)
                .build();

        // When
        var response = jobMapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Backend Engineer");
        assertThat(response.getDescription()).isEqualTo("Diseñar e implementar APIs REST.");
        assertThat(response.getCompany()).isEqualTo("Acme Corp");
        assertThat(response.getLocation()).isEqualTo("Remoto");
        assertThat(response.getStatus()).isEqualTo(JobStatus.open);
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_mapeaCorrectamenteUnOfertaConStatusClosed() {
        // Given
        var entity = JobEntity.builder()
                .id(2L)
                .title("Data Analyst")
                .description("Análisis de datos con Python.")
                .company("Umbrella LLC")
                .location("Remoto")
                .status(JobStatus.closed)
                .createdAt(OffsetDateTime.parse("2025-06-01T09:00:00Z"))
                .build();

        // When
        var response = jobMapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(JobStatus.closed);
    }
}
