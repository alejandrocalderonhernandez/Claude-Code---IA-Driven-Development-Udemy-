package com.debuggeandoideas.JobBoardAPI.specification;

import com.debuggeandoideas.JobBoardAPI.entity.JobEntity;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSpecificationTest {

    @Mock private Root<JobEntity> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    // ─── hasTitle ────────────────────────────────────────────────────────────

    @Test
    void hasTitle_retornaPredicadoNuloCuandoElTituloEsNulo() {
        // When
        var result = JobSpecification.hasTitle(null).toPredicate(root, query, cb);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasTitle_construyePredicadoLikeCaseInsensitiveCuandoElTituloEstaPresente() {
        // Given
        Path<Object> titlePath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("title")).thenReturn(titlePath);
        when(cb.lower(any(Expression.class))).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);

        // When
        var result = JobSpecification.hasTitle("Backend").toPredicate(root, query, cb);

        // Then
        assertThat(result).isNotNull();
        verify(cb).like(lowerExpr, "%backend%");
    }

    // ─── hasLocation ─────────────────────────────────────────────────────────

    @Test
    void hasLocation_retornaPredicadoNuloCuandoLaLocationEsNula() {
        // When
        var result = JobSpecification.hasLocation(null).toPredicate(root, query, cb);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasLocation_construyePredicadoLikeCaseInsensitiveCuandoLaLocationEstaPresente() {
        // Given
        Path<Object> locationPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("location")).thenReturn(locationPath);
        when(cb.lower(any(Expression.class))).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);

        // When
        var result = JobSpecification.hasLocation("Remoto").toPredicate(root, query, cb);

        // Then
        assertThat(result).isNotNull();
        verify(cb).like(lowerExpr, "%remoto%");
    }

    // ─── hasStatus ───────────────────────────────────────────────────────────

    @Test
    void hasStatus_retornaPredicadoNuloCuandoElStatusEsNulo() {
        // When
        var result = JobSpecification.hasStatus(null).toPredicate(root, query, cb);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasStatus_construyePredicadoEqualCuandoElStatusEstaPresente() {
        // Given
        Path<Object> statusPath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("status")).thenReturn(statusPath);
        when(cb.equal(any(Expression.class), eq(JobStatus.open))).thenReturn(predicate);

        // When
        var result = JobSpecification.hasStatus(JobStatus.open).toPredicate(root, query, cb);

        // Then
        assertThat(result).isNotNull();
        verify(cb).equal(statusPath, JobStatus.open);
    }
}
