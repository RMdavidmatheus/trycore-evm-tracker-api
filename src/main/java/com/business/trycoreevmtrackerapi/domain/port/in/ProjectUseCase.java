package com.business.trycoreevmtrackerapi.domain.port.in;

import com.business.trycoreevmtrackerapi.domain.model.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectUseCase {
    Project createProject(Project project);
    Project getProjectById(UUID id);
    List<Project> getAllProjects();
    Project updateProject(UUID id, Project project);
    void deleteProject(UUID id);
}
