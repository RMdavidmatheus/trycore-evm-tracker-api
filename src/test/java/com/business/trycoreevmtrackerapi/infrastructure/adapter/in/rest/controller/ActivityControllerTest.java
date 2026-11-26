package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.controller;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.port.in.ActivityUseCase;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request.ActivityRequest;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.ActivityResponse;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper.EvmDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ActivityControllerTest {

    private final ActivityUseCase activityUseCase = mock(ActivityUseCase.class);
    private final EvmDtoMapper evmDtoMapper = mock(EvmDtoMapper.class);
    private final ActivityController controller = new ActivityController(activityUseCase, evmDtoMapper);

    @Test
    void createActivityReturnsCreatedResponse() {
        UUID projectId = UUID.randomUUID();
        ActivityRequest request = request();
        Activity domain = activity(projectId);
        Activity created = activity(projectId);
        ActivityResponse response = response(projectId);
        when(evmDtoMapper.toDomain(request)).thenReturn(domain);
        when(activityUseCase.createActivity(projectId, domain)).thenReturn(created);
        when(evmDtoMapper.toResponse(created)).thenReturn(response);

        ResponseEntity<ActivityResponse> result = controller.createActivity(projectId, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getActivitiesReturnsMappedResponses() {
        UUID projectId = UUID.randomUUID();
        Activity activity = activity(projectId);
        ActivityResponse response = response(projectId);
        when(activityUseCase.getActivitiesByProjectId(projectId)).thenReturn(List.of(activity));
        when(evmDtoMapper.toResponse(activity)).thenReturn(response);

        ResponseEntity<List<ActivityResponse>> result = controller.getActivitiesByProjectId(projectId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(response), result.getBody());
    }

    @Test
    void updateActivityReturnsUpdatedResponse() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        ActivityRequest request = request();
        Activity domain = activity(projectId);
        Activity updated = activity(projectId);
        ActivityResponse response = response(projectId);
        when(evmDtoMapper.toDomain(request)).thenReturn(domain);
        when(activityUseCase.updateActivity(projectId, activityId, domain)).thenReturn(updated);
        when(evmDtoMapper.toResponse(updated)).thenReturn(response);

        ResponseEntity<ActivityResponse> result = controller.updateActivity(projectId, activityId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteActivityReturnsNoContent() {
        UUID projectId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteActivity(projectId, activityId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(activityUseCase).deleteActivity(projectId, activityId);
    }

    private ActivityRequest request() {
        return new ActivityRequest("Build", new BigDecimal("100"), new BigDecimal("50"),
                new BigDecimal("40"), new BigDecimal("30"));
    }

    private Activity activity(UUID projectId) {
        return new Activity(UUID.randomUUID(), projectId, "Build", new BigDecimal("100"), new BigDecimal("50"),
                new BigDecimal("40"), new BigDecimal("30"), null, true, null, null);
    }

    private ActivityResponse response(UUID projectId) {
        return new ActivityResponse(UUID.randomUUID(), projectId, "Build", new BigDecimal("100"),
                new BigDecimal("50"), new BigDecimal("40"), new BigDecimal("30"), null, true, null, null);
    }
}