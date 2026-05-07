package com.debuggeandoideas.JobBoardAPI.mapper;

import com.debuggeandoideas.JobBoardAPI.dto.request.ApplicationRequest;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationEntity;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationMapperTest {

    private final ApplicationMapper applicationMapper = new ApplicationMapper();

    // ─── toEntity ────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapeaCorrectamenteCandidateIdYJobIdDesdeElRequest() {
        // Given
        var request = new ApplicationRequest(1L, 2L);

        // When
        var entity = applicationMapper.toEntity(request);

        // Then
        assertThat(entity.getCandidateId()).isEqualTo(1L);
        assertThat(entity.getJobId()).isEqualTo(2L);
    }

    @Test
    void toEntity_asignaStatusPendingPorDefectoAlCrearLaEntidad() {
        // Given
        var request = new ApplicationRequest(1L, 2L);

        // When
        var entity = applicationMapper.toEntity(request);

        // Then
        assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.pending);
    }

    @Test
    void toEntity_asignaAppliedAtNoNuloPorDefecto() {
        // Given
        var request = new ApplicationRequest(1L, 2L);

        // When
        var entity = applicationMapper.toEntity(request);

        // Then
        assertThat(entity.getAppliedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Test
    void toResponse_mapeaCorrectamenteTodosLosCamposDeLaEntidad() {
        // Given
        var appliedAt = OffsetDateTime.parse("2025-07-15T10:00:00Z");
        var updatedAt = OffsetDateTime.parse("2025-07-15T12:00:00Z");
        var entity = ApplicationEntity.builder()
                .id(10L)
                .candidateId(1L)
                .jobId(2L)
                .status(ApplicationStatus.pending)
                .appliedAt(appliedAt)
                .updatedAt(updatedAt)
                .build();

        // When
        var response = applicationMapper.toResponse(entity);

        // Then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCandidateId()).isEqualTo(1L);
        assertThat(response.getJobId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.pending);
        assertThat(response.getAppliedAt()).isEqualTo(appliedAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
