package com.debuggeandoideas.JobBoardAPI.service;

import com.debuggeandoideas.JobBoardAPI.client.CandidateClient;
import com.debuggeandoideas.JobBoardAPI.dto.request.ApplicationRequest;
import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationResponse;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationEntity;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import com.debuggeandoideas.JobBoardAPI.entity.JobEntity;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.DuplicateApplicationException;
import com.debuggeandoideas.JobBoardAPI.exception.JobClosedException;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.mapper.ApplicationMapper;
import com.debuggeandoideas.JobBoardAPI.repository.ApplicationRepository;
import com.debuggeandoideas.JobBoardAPI.repository.JobRepository;
import com.debuggeandoideas.JobBoardAPI.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock private CandidateClient candidateClient;
    @Mock private JobRepository jobRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApplicationMapper applicationMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private ApplicationRequest buildRequest() {
        return new ApplicationRequest(1L, 2L);
    }

    private JobEntity buildJob(JobStatus status) {
        return JobEntity.builder()
                .id(2L)
                .title("Backend Engineer")
                .description("Desc.")
                .company("Acme Corp")
                .location("Remoto")
                .status(status)
                .createdAt(OffsetDateTime.parse("2025-07-01T09:00:00Z"))
                .build();
    }

    private ApplicationEntity buildEntity(Long id) {
        return ApplicationEntity.builder()
                .id(id)
                .candidateId(1L)
                .jobId(2L)
                .status(ApplicationStatus.pending)
                .appliedAt(OffsetDateTime.parse("2025-07-15T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2025-07-15T10:00:00Z"))
                .build();
    }

    private ApplicationResponse buildResponse() {
        return ApplicationResponse.builder()
                .id(10L)
                .candidateId(1L)
                .jobId(2L)
                .status(ApplicationStatus.pending)
                .appliedAt(OffsetDateTime.parse("2025-07-15T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2025-07-15T10:00:00Z"))
                .build();
    }

    // ─── applyToJob — happy path ─────────────────────────────────────────────

    @Test
    void applyToJob_creaLaPostulacionCuandoTodasLasValidacionesPasan() {
        // Given
        var request = buildRequest();
        var openJob = buildJob(JobStatus.open);
        var entity = buildEntity(null);
        var savedEntity = buildEntity(10L);
        var response = buildResponse();

        when(jobRepository.findById(2L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(false);
        when(applicationMapper.toEntity(request)).thenReturn(entity);
        when(applicationRepository.save(any(ApplicationEntity.class))).thenReturn(savedEntity);
        when(applicationMapper.toResponse(savedEntity)).thenReturn(response);

        // When
        var result = applicationService.applyToJob(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.pending);
        assertThat(result.getCandidateId()).isEqualTo(1L);
        verify(applicationRepository).save(any(ApplicationEntity.class));
    }

    // ─── applyToJob — RN-004: candidato no existe ────────────────────────────

    @Test
    void applyToJob_lanzaCandidateNotFoundExceptionCuandoElCandidatoNoExisteEnJSONPlaceholder() {
        // Given
        var request = buildRequest();
        when(candidateClient.getCandidateById(1L))
                .thenThrow(new CandidateNotFoundException(1L));

        // When / Then
        assertThatThrownBy(() -> applicationService.applyToJob(request))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void applyToJob_noConsultaElRepositorioDejobsSiElCandidatoNoExiste() {
        // Given
        var request = buildRequest();
        when(candidateClient.getCandidateById(1L))
                .thenThrow(new CandidateNotFoundException(1L));

        // When
        assertThatThrownBy(() -> applicationService.applyToJob(request))
                .isInstanceOf(CandidateNotFoundException.class);

        // Then — el flujo se corta antes de llegar al repositorio
        verify(jobRepository, never()).findById(any());
        verify(applicationRepository, never()).save(any());
    }

    // ─── applyToJob — oferta no existe ──────────────────────────────────────

    @Test
    void applyToJob_lanzaJobNotFoundExceptionCuandoLaOfertaNoExiste() {
        // Given
        var request = buildRequest();
        when(jobRepository.findById(2L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> applicationService.applyToJob(request))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("2");

        verify(applicationRepository, never()).save(any());
    }

    // ─── applyToJob — RN-002: oferta cerrada ────────────────────────────────

    @Test
    void applyToJob_lanzaJobClosedExceptionCuandoLaOfertaEstaCerrada() {
        // Given
        var request = buildRequest();
        var closedJob = buildJob(JobStatus.closed);
        when(jobRepository.findById(2L)).thenReturn(Optional.of(closedJob));

        // When / Then
        assertThatThrownBy(() -> applicationService.applyToJob(request))
                .isInstanceOf(JobClosedException.class)
                .hasMessageContaining("2");

        verify(applicationRepository, never()).save(any());
    }

    // ─── applyToJob — RN-001: postulación duplicada ─────────────────────────

    @Test
    void applyToJob_lanzaDuplicateApplicationExceptionCuandoElCandidatoYaPostulo() {
        // Given
        var request = buildRequest();
        var openJob = buildJob(JobStatus.open);
        when(jobRepository.findById(2L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> applicationService.applyToJob(request))
                .isInstanceOf(DuplicateApplicationException.class)
                .hasMessageContaining("1")
                .hasMessageContaining("2");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyToJob_noGuardaLaPostulacionCuandoYaExisteUnaDuplicada() {
        // Given
        var request = buildRequest();
        var openJob = buildJob(JobStatus.open);
        when(jobRepository.findById(2L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.existsByCandidateIdAndJobId(1L, 2L)).thenReturn(true);

        // When
        assertThatThrownBy(() -> applicationService.applyToJob(request))
                .isInstanceOf(DuplicateApplicationException.class);

        // Then — nunca se llama a save
        verify(applicationRepository, never()).save(any());
    }
}
