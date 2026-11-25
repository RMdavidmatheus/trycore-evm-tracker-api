package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ActivityEntity;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersistenceMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "metrics", ignore = true)
    Activity toDomain(ActivityEntity entity);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ActivityEntity toEntity(Activity domain);

    @Mapping(target = "totalPlannedBudget", ignore = true)
    @Mapping(target = "totalActualCost", ignore = true)
    @Mapping(target = "consolidatedMetrics", ignore = true)
    Project toDomain(ProjectEntity entity);

    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ProjectEntity toEntity(Project domain);
}
