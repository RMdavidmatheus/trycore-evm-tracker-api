package com.business.trycoreevmtrackerapi.application.service;

import com.business.trycoreevmtrackerapi.domain.exception.ResourceNotFoundException;
import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.domain.port.out.ProjectRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    private final ProjectRepositoryPort projectRepositoryPort = mock(ProjectRepositoryPort.class);
    private final EvmCalculatorService evmCalculatorService = mock(EvmCalculatorService.class);
    private final ProjectService service = new ProjectService(projectRepositoryPort, evmCalculatorService);

    @Test
    void createProjectHandlesNullActivitiesAndCalculatesConsolidatedMetrics() {
        Project project = project(UUID.randomUUID(), "New", null, true, null);
        EvmMetrics metrics = metrics();
        when(projectRepositoryPort.save(project)).thenReturn(project);
        when(evmCalculatorService.calculateConsolidated(List.of())).thenReturn(metrics);

        Project result = service.createProject(project);

        assertEquals(BigDecimal.ZERO.setScale(4), result.totalPlannedBudget());
        assertEquals(BigDecimal.ZERO.setScale(4), result.totalActualCost());
        assertTrue(result.activities().isEmpty());
        assertSame(metrics, result.consolidatedMetrics());
        verify(evmCalculatorService).calculateConsolidated(List.of());
    }

    @Test
    void getProjectByIdReturnsEnrichedProject() {
        UUID id = UUID.randomUUID();
        Project existing = project(id, "Existing", "Description", true, List.of());
        EvmMetrics metrics = metrics();
        when(projectRepositoryPort.findActiveById(id)).thenReturn(Optional.of(existing));
        when(evmCalculatorService.calculateConsolidated(List.of())).thenReturn(metrics);

        Project result = service.getProjectById(id);

        assertSame(metrics, result.consolidatedMetrics());
        assertEquals(id, result.id());
    }

    @Test
    void getProjectByIdFailsWhenProjectDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(projectRepositoryPort.findActiveById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.getProjectById(id));

        assertEquals("Proyecto no encontrado con ID: " + id, exception.getMessage());
        verifyNoInteractions(evmCalculatorService);
    }

    @Test
    void getAllProjectsEnrichesProjectsAndSumsActivityValues() {
        UUID projectId = UUID.randomUUID();
        Activity activity = activity(projectId, "Design", "80", "25");
        Project project = project(projectId, "Plan", "Description", true, List.of(activity));
        EvmMetrics activityMetrics = metrics();
        EvmMetrics consolidatedMetrics = metrics();
        when(projectRepositoryPort.findAllActive()).thenReturn(List.of(project));
        when(evmCalculatorService.calculateForActivity(activity)).thenReturn(activityMetrics);
        when(evmCalculatorService.calculateConsolidated(anyList())).thenReturn(consolidatedMetrics);

        List<Project> results = service.getAllProjects();

        assertEquals(1, results.size());
        Project result = results.getFirst();
        assertEquals(new BigDecimal("80.0000"), result.totalPlannedBudget());
        assertEquals(new BigDecimal("25.0000"), result.totalActualCost());
        assertSame(activityMetrics, result.activities().getFirst().metrics());
        assertSame(consolidatedMetrics, result.consolidatedMetrics());
    }

    @Test
    void getAllProjectsReturnsEmptyListWhenRepositoryHasNoProjects() {
        when(projectRepositoryPort.findAllActive()).thenReturn(List.of());

        assertTrue(service.getAllProjects().isEmpty());
        verifyNoInteractions(evmCalculatorService);
    }

    @Test
    void updateProjectChangesEditableFieldsAndRetainsExistingState() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        Activity existingActivity = activity(id, "Design", "80", "25");
        Project existing = new Project(id, "Old", "Old description", false, bd("80"), bd("25"),
                List.of(existingActivity), null, createdAt, createdAt.plusDays(1));
        Project request = project(UUID.randomUUID(), "Updated", "New description", true, null);
        EvmMetrics activityMetrics = metrics();
        EvmMetrics consolidatedMetrics = metrics();
        when(projectRepositoryPort.findActiveById(id)).thenReturn(Optional.of(existing));
        when(projectRepositoryPort.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(evmCalculatorService.calculateForActivity(existingActivity)).thenReturn(activityMetrics);
        when(evmCalculatorService.calculateConsolidated(anyList())).thenReturn(consolidatedMetrics);

        Project result = service.updateProject(id, request);

        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepositoryPort).save(captor.capture());
        Project toUpdate = captor.getValue();
        assertEquals("Updated", toUpdate.name());
        assertEquals("New description", toUpdate.description());
        assertFalse(toUpdate.isActive());
        assertEquals(createdAt, toUpdate.createdAt());
        assertEquals(existing.activities(), toUpdate.activities());
        assertSame(consolidatedMetrics, result.consolidatedMetrics());
    }

    @Test
    void updateProjectFailsWhenProjectDoesNotExist() {
        UUID id = UUID.randomUUID();
        Project request = project(id, "Updated", "Description", true, null);
        when(projectRepositoryPort.findActiveById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProject(id, request));

        verify(projectRepositoryPort, never()).save(any(Project.class));
    }

    @Test
    void deleteProjectSoftDeletesAnExistingProject() {
        UUID id = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(id)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteProject(id));

        verify(projectRepositoryPort).softDeleteById(id);
    }

    @Test
    void deleteProjectFailsWhenProjectDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteProject(id));

        verify(projectRepositoryPort, never()).softDeleteById(any());
    }

    private Project project(UUID id, String name, String description, boolean active, List<Activity> activities) {
        return new Project(id, name, description, active, null, null, activities, null, null, null);
    }

    private Activity activity(UUID projectId, String name, String budget, String actualCost) {
        return new Activity(UUID.randomUUID(), projectId, name, bd(budget), bd("50"), bd("40"), bd(actualCost),
                null, true, null, null);
    }

    private EvmMetrics metrics() {
        return new EvmMetrics(bd("50"), bd("40"), bd("10"), bd("-10"), bd("1.3333"), bd("0.8000"),
                bd("75"), bd("25"), null, null);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}