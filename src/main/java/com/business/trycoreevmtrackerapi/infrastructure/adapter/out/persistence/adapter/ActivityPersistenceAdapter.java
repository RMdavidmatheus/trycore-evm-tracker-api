package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.adapter;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.port.out.ActivityRepositoryPort;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ActivityEntity;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ProjectEntity;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.repository.ActivityJpaRepository;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.repository.ProjectJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActivityPersistenceAdapter implements ActivityRepositoryPort {

    private final ActivityJpaRepository activityJpaRepository;
    private final ProjectJpaRepository projectJpaRepository;
    private final PersistenceMapper persistenceMapper;

    @Override
    public Activity save(UUID projectId, Activity activity) {
        ProjectEntity projectEntity = projectJpaRepository.findByIdAndIsActiveTrue(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        ActivityEntity entity;
        if (activity.id() != null) {
            entity = activityJpaRepository.findByIdAndProjectIdAndIsActiveTrue(activity.id(), projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found: " + activity.id()));
            entity.setName(activity.name());
            entity.setPlannedBudget(activity.plannedBudget());
            entity.setPlannedPercentage(activity.plannedPercentage());
            entity.setCompletedPercentage(activity.completedPercentage());
            entity.setActualCost(activity.actualCost());
            entity.setUpdatedAt(LocalDateTime.now());
        } else {
            entity = persistenceMapper.toEntity(activity);
            entity.setProject(projectEntity);
        }

        ActivityEntity saved = activityJpaRepository.save(entity);
        return persistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Activity> findActiveByIdAndProjectId(UUID activityId, UUID projectId) {
        return activityJpaRepository.findByIdAndProjectIdAndIsActiveTrue(activityId, projectId)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public List<Activity> findAllActiveByProjectId(UUID projectId) {
        return activityJpaRepository.findAllByProjectIdAndIsActiveTrueOrderByCreatedAtAsc(projectId)
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void softDeleteById(UUID activityId) {
        activityJpaRepository.findById(activityId).ifPresent(entity -> {
            LocalDateTime now = LocalDateTime.now();
            entity.setActive(false);
            entity.setDeletedAt(now);
            activityJpaRepository.save(entity);
        });
    }
}
