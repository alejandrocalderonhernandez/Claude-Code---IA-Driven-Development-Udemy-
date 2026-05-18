package com.debuggeandoideas.JobBoardAPI.repository;

import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationHistoryDto;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    @Query("SELECT a.status, COUNT(a) FROM ApplicationEntity a WHERE a.jobId = :jobId GROUP BY a.status")
    List<Object[]> countByJobIdGroupByStatus(@Param("jobId") Long jobId);

    // TODO UNIT TEST
    @Query("""
            SELECT new com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationHistoryDto(
                a.id,
                a.status,
                a.appliedAt,
                a.updatedAt,
                j.id,
                j.title,
                j.company,
                j.location,
                j.status
            )
            FROM ApplicationEntity a, JobEntity j
            WHERE a.jobId = j.id
              AND a.candidateId = :candidateId
            ORDER BY a.appliedAt DESC
            """)
    List<ApplicationHistoryDto> findApplicationHistoryByCandidateId(
            @Param("candidateId") Long candidateId
    );
}
