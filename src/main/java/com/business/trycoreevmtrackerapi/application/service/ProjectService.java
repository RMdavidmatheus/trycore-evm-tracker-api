package com.business.trycoreevmtrackerapi.application.service;

import com.business.trycoreevmtrackerapi.domain.exception.ResourceNotFoundException;
import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.domain.port.in.ProjectUseCase;
import com.business.trycoreevmtrackerapi.domain.port.out.ProjectRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProjectService implements ProjectUseCase {

    private static final int RESULT_SCALE = 4;
    private static final String PROJECT_NOT_FOUND_MSG = "Proyecto no encontrado con ID: ";

    private final ProjectRepositoryPort projectRepositoryPort;
    private final EvmCalculatorService evmCalculatorService;

    @Override
    @Transactional
    public Project createProject(Project project) {
        Project saved = projectRepositoryPort.save(project);
        return enrichProjectWithEvm(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(UUID id) {
        Project project = projectRepositoryPort.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MSG + id));
        return enrichProjectWithEvm(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepositoryPort.findAllActive()
                .stream()
                .map(this::enrichProjectWithEvm)
                .toList();
    }

    @Override
    @Transactional
    public Project updateProject(UUID id, Project project) {
        Project existing = projectRepositoryPort.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MSG + id));

        Project toUpdate = new Project(
                existing.id(),
                project.name(),
                project.description(),
                existing.isActive(),
                existing.totalPlannedBudget(),
                existing.totalActualCost(),
                existing.activities(),
                existing.consolidatedMetrics(),
                existing.createdAt(),
                existing.updatedAt()
        );

        Project updated = projectRepositoryPort.save(toUpdate);
        return enrichProjectWithEvm(updated);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        if (!projectRepositoryPort.existsActiveById(id)) {
            throw new ResourceNotFoundException(PROJECT_NOT_FOUND_MSG + id);
        }
        projectRepositoryPort.softDeleteById(id);
    }

    private Project enrichProjectWithEvm(Project project) {
        List<Activity> enrichedActivities = project.activities() == null
                ? List.of()
                : project.activities().stream()
                .map(this::enrichActivity)
                .toList();

        BigDecimal totalBac = enrichedActivities.stream()
                .map(Activity::plannedBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        BigDecimal totalAc = enrichedActivities.stream()
                .map(Activity::actualCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        EvmMetrics consolidated = evmCalculatorService.calculateConsolidated(enrichedActivities);

        return new Project(
                project.id(),
                project.name(),
                project.description(),
                project.isActive(),
                totalBac,
                totalAc,
                enrichedActivities,
                consolidated,
                project.createdAt(),
                project.updatedAt()
        );
    }

    private Activity enrichActivity(Activity activity) {
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
