package com.business.trycoreevmtrackerapi.application.service;

import com.business.trycoreevmtrackerapi.domain.exception.ResourceNotFoundException;
import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.port.out.ActivityRepositoryPort;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActivityServiceTest {

    private final ActivityRepositoryPort activityRepositoryPort = mock(ActivityRepositoryPort.class);
    private final ProjectRepositoryPort projectRepositoryPort = mock(ProjectRepositoryPort.class);
    private final EvmCalculatorService evmCalculatorService = mock(EvmCalculatorService.class);
    private final ActivityService service = new ActivityService(
            activityRepositoryPort,
            projectRepositoryPort,
            evmCalculatorService
    );

    @Test
    void createActivitySavesAndEnrichesTheActivity() {
        UUID projectId = UUID.randomUUID();
        Activity activity = activity(UUID.randomUUID(), projectId, "Build", true);
        Activity saved = activity(UUID.randomUUID(), projectId, "Build", true);
        EvmMetrics metrics = metrics();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.save(projectId, activity)).thenReturn(saved);
        when(evmCalculatorService.calculateForActivity(saved)).thenReturn(metrics);

        Activity result = service.createActivity(projectId, activity);

        assertEquals(saved.id(), result.id());
        assertSame(metrics, result.metrics());
        verify(activityRepositoryPort).save(projectId, activity);
    }

    @Test
    void createActivityFailsWhenProjectDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.createActivity(projectId, activity(UUID.randomUUID(), projectId, "Build", true))
        );

        assertEquals("Proyecto no encontrado con ID: " + projectId, exception.getMessage());
        verifyNoInteractions(activityRepositoryPort);
    }

    @Test
    void updateActivityPreservesExistingIdentityAndTimestamps() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 2, 3, 4);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 2, 3, 4, 5);
        Activity existing = new Activity(activityId, projectId, "Old", bd("10"), bd("20"), bd("10"),
                bd("2"), null, false, createdAt, updatedAt);
        Activity requested = activity(UUID.randomUUID(), projectId, "New", true);
        EvmMetrics metrics = metrics();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.findActiveByIdAndProjectId(activityId, projectId)).thenReturn(Optional.of(existing));
        when(activityRepositoryPort.save(eq(projectId), any(Activity.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(evmCalculatorService.calculateForActivity(any(Activity.class))).thenReturn(metrics);

        Activity result = service.updateActivity(projectId, activityId, requested);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepositoryPort).save(eq(projectId), captor.capture());
        Activity toUpdate = captor.getValue();
        assertEquals(activityId, toUpdate.id());
        assertEquals("New", toUpdate.name());
        assertFalse(toUpdate.isActive());
        assertEquals(createdAt, toUpdate.createdAt());
        assertEquals(updatedAt, toUpdate.updatedAt());
        assertNull(toUpdate.metrics());
        assertSame(metrics, result.metrics());
    }

    @Test
    void updateActivityFailsWhenProjectDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateActivity(projectId, activityId, activity(activityId, projectId, "Build", true)));

        verifyNoInteractions(activityRepositoryPort);
    }

    @Test
    void updateActivityFailsWhenActivityDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.findActiveByIdAndProjectId(activityId, projectId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateActivity(projectId, activityId, activity(activityId, projectId, "Build", true))
        );

        assertEquals("Actividad no encontrada con ID: " + activityId, exception.getMessage());
        verify(activityRepositoryPort, never()).save(any(), any());
    }

    @Test
    void deleteActivitySoftDeletesAnExistingActivity() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.findActiveByIdAndProjectId(activityId, projectId))
                .thenReturn(Optional.of(activity(activityId, projectId, "Build", true)));

        assertDoesNotThrow(() -> service.deleteActivity(projectId, activityId));

        verify(activityRepositoryPort).softDeleteById(activityId);
    }

    @Test
    void deleteActivityFailsWhenProjectDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteActivity(projectId, activityId));

        verifyNoInteractions(activityRepositoryPort);
    }

    @Test
    void deleteActivityFailsWhenActivityDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.findActiveByIdAndProjectId(activityId, projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteActivity(projectId, activityId));

        verify(activityRepositoryPort, never()).softDeleteById(any());
    }

    @Test
    void getActivitiesEnrichesAllResults() {
        UUID projectId = UUID.randomUUID();
        Activity activity = activity(UUID.randomUUID(), projectId, "Build", true);
        EvmMetrics metrics = metrics();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.findAllActiveByProjectId(projectId)).thenReturn(List.of(activity));
        when(evmCalculatorService.calculateForActivity(activity)).thenReturn(metrics);

        List<Activity> result = service.getActivitiesByProjectId(projectId);

        assertEquals(1, result.size());
        assertSame(metrics, result.getFirst().metrics());
    }

    @Test
    void getActivitiesReturnsEmptyListWhenProjectHasNoActivities() {
        UUID projectId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(true);
        when(activityRepositoryPort.findAllActiveByProjectId(projectId)).thenReturn(List.of());

        assertTrue(service.getActivitiesByProjectId(projectId).isEmpty());
        verifyNoInteractions(evmCalculatorService);
    }

    @Test
    void getActivitiesFailsWhenProjectDoesNotExist() {
        UUID projectId = UUID.randomUUID();
        when(projectRepositoryPort.existsActiveById(projectId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getActivitiesByProjectId(projectId));

        verifyNoInteractions(activityRepositoryPort);
    }

    private Activity activity(UUID id, UUID projectId, String name, boolean active) {
        return new Activity(id, projectId, name, bd("100"), bd("50"), bd("40"), bd("30"), null,
                active, LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 2, 0, 0));
    }

    private EvmMetrics metrics() {
        return new EvmMetrics(bd("50"), bd("40"), bd("10"), bd("-10"), bd("1.3333"), bd("0.8000"),
                bd("75"), bd("25"), null, null);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}