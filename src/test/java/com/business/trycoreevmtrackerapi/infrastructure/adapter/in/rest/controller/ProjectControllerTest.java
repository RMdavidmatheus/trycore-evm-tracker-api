package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.controller;

import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.domain.port.in.ProjectUseCase;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request.ProjectRequest;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.ProjectResponse;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper.EvmDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProjectControllerTest {

    private final ProjectUseCase projectUseCase = mock(ProjectUseCase.class);
    private final EvmDtoMapper evmDtoMapper = mock(EvmDtoMapper.class);
    private final ProjectController controller = new ProjectController(projectUseCase, evmDtoMapper);

    @Test
    void createProjectReturnsCreatedResponse() {
        ProjectRequest request = new ProjectRequest("Project", "Description");
        Project domain = project();
        Project created = project();
        ProjectResponse response = response();
        when(evmDtoMapper.toDomain(request)).thenReturn(domain);
        when(projectUseCase.createProject(domain)).thenReturn(created);
        when(evmDtoMapper.toResponse(created)).thenReturn(response);

        ResponseEntity<ProjectResponse> result = controller.createProject(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getAllProjectsReturnsMappedResponses() {
        Project project = project();
        ProjectResponse response = response();
        when(projectUseCase.getAllProjects()).thenReturn(List.of(project));
        when(evmDtoMapper.toResponse(project)).thenReturn(response);

        ResponseEntity<List<ProjectResponse>> result = controller.getAllProjects();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(response), result.getBody());
    }

    @Test
    void getProjectByIdReturnsMappedResponse() {
        UUID id = UUID.randomUUID();
        Project project = project();
        ProjectResponse response = response();
        when(projectUseCase.getProjectById(id)).thenReturn(project);
        when(evmDtoMapper.toResponse(project)).thenReturn(response);

        ResponseEntity<ProjectResponse> result = controller.getProjectById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateProjectReturnsUpdatedResponse() {
        UUID id = UUID.randomUUID();
        ProjectRequest request = new ProjectRequest("Updated", "New description");
        Project domain = project();
        Project updated = project();
        ProjectResponse response = response();
        when(evmDtoMapper.toDomain(request)).thenReturn(domain);
        when(projectUseCase.updateProject(id, domain)).thenReturn(updated);
        when(evmDtoMapper.toResponse(updated)).thenReturn(response);

        ResponseEntity<ProjectResponse> result = controller.updateProject(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteProjectReturnsNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteProject(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(projectUseCase).deleteProject(id);
    }

    private Project project() {
        return new Project(UUID.randomUUID(), "Project", "Description", true, null, null, List.of(), null,
                null, null);
    }

    private ProjectResponse response() {
        return new ProjectResponse(UUID.randomUUID(), "Project", "Description", null, null, List.of(), null,
                true, null, null);
    }
}