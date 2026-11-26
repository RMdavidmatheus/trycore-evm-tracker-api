package com.business.trycoreevmtrackerapi.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.business.trycoreevmtrackerapi.domain.exception.ResourceNotFoundException;
import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.port.in.ActivityUseCase;
import com.business.trycoreevmtrackerapi.domain.port.out.ActivityRepositoryPort;
import com.business.trycoreevmtrackerapi.domain.port.out.ProjectRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService implements ActivityUseCase {

    private static final String PROJECT_NOT_FOUND_MSG = "Proyecto no encontrado con ID: ";
    private static final String ACTIVITY_NOT_FOUND_MSG = "Actividad no encontrada con ID: ";

    private final ActivityRepositoryPort activityRepositoryPort;
    private final ProjectRepositoryPort projectRepositoryPort;
    private final EvmCalculatorService evmCalculatorService;

    @Override
    @Transactional
    public Activity createActivity(UUID projectId, Activity activity) {
        validateProjectExists(projectId);
        Activity saved = activityRepositoryPort.save(projectId, activity);
        return enrichWithMetrics(saved);
    }

    @Override
    @Transactional
    public Activity updateActivity(UUID projectId, UUID activityId, Activity activity) {
        validateProjectExists(projectId);
        Activity existing = activityRepositoryPort.findActiveByIdAndProjectId(activityId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ACTIVITY_NOT_FOUND_MSG + activityId));

        Activity toUpdate = new Activity(
                existing.id(),
                projectId,
                activity.name(),
                activity.plannedBudget(),
                activity.plannedPercentage(),
                activity.completedPercentage(),
                activity.actualCost(),
                null,
                existing.isActive(),
                existing.createdAt(),
                existing.updatedAt()
        );

        Activity updated = activityRepositoryPort.save(projectId, toUpdate);
        return enrichWithMetrics(updated);
    }

    @Override
    @Transactional
    public void deleteActivity(UUID projectId, UUID activityId) {
        validateProjectExists(projectId);
        if (activityRepositoryPort.findActiveByIdAndProjectId(activityId, projectId).isEmpty()) {
            throw new ResourceNotFoundException(ACTIVITY_NOT_FOUND_MSG + activityId);
        }
        activityRepositoryPort.softDeleteById(activityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Activity> getActivitiesByProjectId(UUID projectId) {
        validateProjectExists(projectId);
        return activityRepositoryPort.findAllActiveByProjectId(projectId)
                .stream()
                .map(this::enrichWithMetrics)
                .toList();
    }

    private void validateProjectExists(UUID projectId) {
        if (!projectRepositoryPort.existsActiveById(projectId)) {
            throw new ResourceNotFoundException(PROJECT_NOT_FOUND_MSG + projectId);
        }
    }

    private Activity enrichWithMetrics(Activity activity) {
        EvmMetrics metrics = evmCalculatorService.calculateForActivity(activity);
        return new Activity(
                activity.id(),
                activity.projectId(),
                activity.name(),
                activity.plannedBudget(),
                activity.plannedPercentage(),
                activity.completedPercentage(),
                activity.actualCost(),
                metrics,
                activity.isActive(),
                activity.createdAt(),
                activity.updatedAt()
        );
    }

}
