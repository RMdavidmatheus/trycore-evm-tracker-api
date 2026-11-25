package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.repository;

import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    Optional<ProjectEntity> findByIdAndIsActiveTrue(UUID id);
    List<ProjectEntity> findAllByIsActiveTrueOrderByCreatedAtDesc();
    boolean existsByIdAndIsActiveTrue(UUID id);
}
