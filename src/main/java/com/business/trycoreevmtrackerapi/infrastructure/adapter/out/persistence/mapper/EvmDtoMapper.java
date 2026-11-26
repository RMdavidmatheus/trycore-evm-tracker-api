package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.EvmMetrics;
import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request.ActivityRequest;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request.ProjectRequest;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.ActivityResponse;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.EvmMetricsResponse;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvmDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "totalPlannedBudget", ignore = true)
    @Mapping(target = "totalActualCost", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "consolidatedMetrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toDomain(ProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Activity toDomain(ActivityRequest request);

    EvmMetricsResponse toResponse(EvmMetrics metrics);

    ActivityResponse toResponse(Activity activity);

    ProjectResponse toResponse(Project project);
}
