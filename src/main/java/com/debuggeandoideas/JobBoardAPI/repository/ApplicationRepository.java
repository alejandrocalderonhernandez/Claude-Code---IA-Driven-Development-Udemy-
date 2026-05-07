package com.debuggeandoideas.JobBoardAPI.repository;

import com.debuggeandoideas.JobBoardAPI.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
}
