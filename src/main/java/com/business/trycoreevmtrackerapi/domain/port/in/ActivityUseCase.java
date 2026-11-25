package com.business.trycoreevmtrackerapi.domain.port.in;

import com.business.trycoreevmtrackerapi.domain.model.Activity;

import java.util.List;
import java.util.UUID;

public interface ActivityUseCase {
    Activity createActivity(UUID projectId, Activity activity);
    Activity updateActivity(UUID projectId, UUID activityId, Activity activity);
    void deleteActivity(UUID projectId, UUID activityId);
    List<Activity> getActivitiesByProjectId(UUID projectId);
}
