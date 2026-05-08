package com.debuggeandoideas.JobBoardAPI.repository;

import com.debuggeandoideas.JobBoardAPI.entity.ApplicationEntity;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
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
}
