package com.debuggeandoideas.JobBoardAPI.specification;

import com.debuggeandoideas.JobBoardAPI.entity.JobEntity;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {

    public static Specification<JobEntity> hasTitle(String title) {
        return (root, query, cb) ->
                title == null ? null :
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<JobEntity> hasLocation(String location) {
        return (root, query, cb) ->
                location == null ? null :
                cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<JobEntity> hasStatus(JobStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                cb.equal(root.get("status"), status);
    }
}
