package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.adapter;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.domain.port.out.ProjectRepositoryPort;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ActivityEntity;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ProjectEntity;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.repository.ProjectJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectPersistenceAdapter implements ProjectRepositoryPort {

    private final ProjectJpaRepository projectJpaRepository;
    private final PersistenceMapper persistenceMapper;

    @Override
    public Project save(Project project) {
        ProjectEntity entity;
        if (project.id() != null) {
            entity = projectJpaRepository.findByIdAndIsActiveTrue(project.id())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found: " + project.id()));
            entity.setName(project.name());
            entity.setDescription(project.description());
            entity.setUpdatedAt(LocalDateTime.now());
        } else {
            entity = persistenceMapper.toEntity(project);
        }
        ProjectEntity saved = projectJpaRepository.save(entity);
        return mapProjectWithActiveActivities(saved);
    }

    @Override
    public Optional<Project> findActiveById(UUID id) {
        return projectJpaRepository.findByIdAndIsActiveTrue(id)
                .map(this::mapProjectWithActiveActivities);
    }

    @Override
    public List<Project> findAllActive() {
        return projectJpaRepository.findAllByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapProjectWithActiveActivities)
                .toList();
    }

    @Override
    public void softDeleteById(UUID id) {
        projectJpaRepository.findByIdAndIsActiveTrue(id).ifPresent(entity -> {
            LocalDateTime now = LocalDateTime.now();
            entity.setActive(false);
            entity.setDeletedAt(now);
            entity.setUpdatedAt(now);
            if (entity.getActivities() != null) {
                entity.getActivities().stream()
                        .filter(ActivityEntity::isActive)
                        .forEach(activity -> {
                            activity.setActive(false);
                            activity.setDeletedAt(now);
                            activity.setUpdatedAt(now);
                        });
            }
            projectJpaRepository.save(entity);
        });
    }

    @Override
    public boolean existsActiveById(UUID id) {
        return projectJpaRepository.existsByIdAndIsActiveTrue(id);
    }

    private Project mapProjectWithActiveActivities(ProjectEntity entity) {
        Project base = persistenceMapper.toDomain(entity);
        List<Activity> activeActivities = entity.getActivities() == null
                ? List.of()
                : entity.getActivities().stream()
                .filter(ActivityEntity::isActive)
                .map(persistenceMapper::toDomain)
                .toList();

        return new Project(
                base.id(),
                base.name(),
                base.description(),
                base.isActive(),
                base.totalPlannedBudget(),
                base.totalActualCost(),
                activeActivities,
                base.consolidatedMetrics(),
                base.createdAt(),
                base.updatedAt()
        );
    }
}
