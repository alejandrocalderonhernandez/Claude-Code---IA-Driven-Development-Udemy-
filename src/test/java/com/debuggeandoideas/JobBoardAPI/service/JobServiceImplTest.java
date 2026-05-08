package com.debuggeandoideas.JobBoardAPI.service;

import com.debuggeandoideas.JobBoardAPI.dto.request.JobRequest;
import com.debuggeandoideas.JobBoardAPI.dto.request.JobSearchCriteria;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobResponse;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import com.debuggeandoideas.JobBoardAPI.entity.JobEntity;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.mapper.JobMapper;
import com.debuggeandoideas.JobBoardAPI.repository.ApplicationRepository;
import com.debuggeandoideas.JobBoardAPI.repository.JobRepository;
import com.debuggeandoideas.JobBoardAPI.service.impl.JobServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock private JobRepository jobRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private JobRequest buildRequest() {
        return new JobRequest("Backend Engineer", "Diseñar e implementar APIs REST.", "Acme Corp", "Remoto");
    }

    private JobEntity buildEntity(Long id, JobStatus status) {
        return JobEntity.builder()
                .id(id)
                .title("Backend Engineer")
                .description("Diseñar e implementar APIs REST.")
                .company("Acme Corp")
                .location("Remoto")
                .status(status)
                .createdAt(OffsetDateTime.parse("2025-07-15T14:30:00Z"))
                .build();
    }

    private JobResponse buildResponse(Long id, JobStatus status) {
        return JobResponse.builder()
                .id(id)
                .title("Backend Engineer")
                .description("Diseñar e implementar APIs REST.")
                .company("Acme Corp")
                .location("Remoto")
                .status(status)
                .createdAt(OffsetDateTime.parse("2025-07-15T14:30:00Z"))
                .build();
    }

    // ─── createJob ───────────────────────────────────────────────────────────

    @Test
    void createJob_guardaLaOfertaEnElRepositorioYRetornaElResponse() {
        // Given
        var request = buildRequest();
        var entity = buildEntity(null, JobStatus.open);
        var savedEntity = buildEntity(1L, JobStatus.open);
        var response = buildResponse(1L, JobStatus.open);

        when(jobMapper.toEntity(request)).thenReturn(entity);
        when(jobRepository.save(any(JobEntity.class))).thenReturn(savedEntity);
        when(jobMapper.toResponse(savedEntity)).thenReturn(response);

        // When
        var result = jobService.createJob(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(JobStatus.open);
        verify(jobRepository).save(any(JobEntity.class));
    }

    // ─── getAllJobs ───────────────────────────────────────────────────────────

    @Test
    void getAllJobs_retornaTodosLosJobsMapeadosAResponse() {
        // Given
        var entity1 = buildEntity(1L, JobStatus.open);
        var entity2 = buildEntity(2L, JobStatus.closed);
        var response1 = buildResponse(1L, JobStatus.open);
        var response2 = buildResponse(2L, JobStatus.closed);

        when(jobRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(jobMapper.toResponse(entity1)).thenReturn(response1);
        when(jobMapper.toResponse(entity2)).thenReturn(response2);

        // When
        var result = jobService.getAllJobs();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getStatus()).isEqualTo(JobStatus.closed);
        verify(jobRepository).findAll();
    }

    @Test
    void getAllJobs_retornaListaVaciaCuandoNoExistenOfertas() {
        // Given
        when(jobRepository.findAll()).thenReturn(List.of());

        // When
        var result = jobService.getAllJobs();

        // Then
        assertThat(result).isEmpty();
        verify(jobRepository).findAll();
    }

    // ─── getJobById ──────────────────────────────────────────────────────────

    @Test
    void getJobById_retornaElJobCuandoElIdExiste() {
        // Given
        var entity = buildEntity(1L, JobStatus.open);
        var response = buildResponse(1L, JobStatus.open);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jobMapper.toResponse(entity)).thenReturn(response);

        // When
        var result = jobService.getJobById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(jobRepository).findById(1L);
    }

    @Test
    void getJobById_lanzaJobNotFoundExceptionCuandoElIdNoExiste() {
        // Given
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> jobService.getJobById(99L))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── closeJob ────────────────────────────────────────────────────────────

    @Test
    void closeJob_cambiaElStatusAClosedYPersisteLaOferta() {
        // Given
        var entity = buildEntity(1L, JobStatus.open);
        var response = buildResponse(1L, JobStatus.closed);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jobRepository.save(entity)).thenReturn(entity);
        when(jobMapper.toResponse(entity)).thenReturn(response);

        // When
        var result = jobService.closeJob(1L);

        // Then
        assertThat(entity.getStatus()).isEqualTo(JobStatus.closed);
        assertThat(result.getStatus()).isEqualTo(JobStatus.closed);
        verify(jobRepository).save(entity);
    }

    @Test
    void closeJob_lanzaJobNotFoundExceptionCuandoElIdNoExiste() {
        // Given
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> jobService.closeJob(99L))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── searchJobs ──────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void searchJobs_retornaTodasLasOfertasPaginadasCuandoNoHayFiltros() {
        // Given
        var entity = buildEntity(1L, JobStatus.open);
        var response = buildResponse(1L, JobStatus.open);
        var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1L);

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(jobMapper.toResponse(entity)).thenReturn(response);

        var criteria = new JobSearchCriteria(null, null, null, 0, 20);

        // When
        var result = jobService.searchJobs(criteria);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        verify(jobRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchJobs_retornaListaVaciaCuandoNoHayResultadosParaLosFiltros() {
        // Given
        var page = new PageImpl<JobEntity>(List.of(), PageRequest.of(0, 20), 0L);
        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var criteria = new JobSearchCriteria("inexistente", null, null, 0, 20);

        // When
        var result = jobService.searchJobs(criteria);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchJobs_retornaMetadataDePaginacionCorrecta() {
        // Given
        var e1 = buildEntity(1L, JobStatus.open);
        var e2 = buildEntity(2L, JobStatus.open);
        var r1 = buildResponse(1L, JobStatus.open);
        var r2 = buildResponse(2L, JobStatus.open);
        var page = new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 2), 5L);

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(jobMapper.toResponse(e1)).thenReturn(r1);
        when(jobMapper.toResponse(e2)).thenReturn(r2);

        var criteria = new JobSearchCriteria(null, null, null, 0, 2);

        // When
        var result = jobService.searchJobs(criteria);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotal()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    // ─── getJobReport ─────────────────────────────────────────────────────────

    @Test
    void getJobReport_retornaReporteConTotalesCorrectosCuandoHayPostulaciones() {
        // Given
        var job = buildEntity(1L, JobStatus.open);
        var rows = List.of(
                new Object[]{ApplicationStatus.pending, 2L},
                new Object[]{ApplicationStatus.accepted, 1L},
                new Object[]{ApplicationStatus.rejected, 2L}
        );

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.countByJobIdGroupByStatus(1L)).thenReturn(rows);

        // When
        var result = jobService.getJobReport(1L);

        // Then
        assertThat(result.getJobId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Backend Engineer");
        assertThat(result.getTotalApplications()).isEqualTo(5L);
        assertThat(result.getByStatus()).containsEntry("pending", 2L);
        assertThat(result.getByStatus()).containsEntry("accepted", 1L);
        assertThat(result.getByStatus()).containsEntry("rejected", 2L);
    }

    @Test
    void getJobReport_retornaReporteConTodosLosStatusEnCeroCuandoNoHayPostulaciones() {
        // Given
        var job = buildEntity(1L, JobStatus.open);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.countByJobIdGroupByStatus(1L)).thenReturn(List.of());

        // When
        var result = jobService.getJobReport(1L);

        // Then
        assertThat(result.getTotalApplications()).isEqualTo(0L);
        assertThat(result.getByStatus()).containsEntry("pending", 0L);
        assertThat(result.getByStatus()).containsEntry("accepted", 0L);
        assertThat(result.getByStatus()).containsEntry("rejected", 0L);
    }

    @Test
    void getJobReport_lanzaJobNotFoundExceptionCuandoLaOfertaNoExiste() {
        // Given
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> jobService.getJobReport(99L))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchJobs_filtraPorStatusCuandoSeIndicaEnLosCriterios() {
        // Given
        var entity = buildEntity(1L, JobStatus.open);
        var response = buildResponse(1L, JobStatus.open);
        var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1L);

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(jobMapper.toResponse(entity)).thenReturn(response);

        var criteria = new JobSearchCriteria(null, null, JobStatus.open, 0, 20);

        // When
        var result = jobService.searchJobs(criteria);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(JobStatus.open);
    }
}
