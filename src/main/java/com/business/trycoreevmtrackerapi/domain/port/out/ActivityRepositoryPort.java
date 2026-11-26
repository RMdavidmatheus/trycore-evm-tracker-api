package com.business.trycoreevmtrackerapi.domain.port.out;

import com.business.trycoreevmtrackerapi.domain.model.Activity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepositoryPort {
    Activity save(UUID projectId, Activity activity);
    Optional<Activity> findActiveByIdAndProjectId(UUID activityId, UUID projectId);
    List<Activity> findAllActiveByProjectId(UUID projectId);
    void softDeleteById(UUID activityId);
}
