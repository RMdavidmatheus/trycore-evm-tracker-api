package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.repository;

import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, UUID> {
    Optional<ActivityEntity> findByIdAndProjectIdAndIsActiveTrue(UUID id, UUID projectId);
    List<ActivityEntity> findAllByProjectIdAndIsActiveTrueOrderByCreatedAtAsc(UUID projectId);
}
