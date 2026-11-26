package com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "planned_budget", nullable = false, precision = 19, scale = 4)
    private BigDecimal plannedBudget;

    @Column(name = "planned_percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal plannedPercentage;

    @Column(name = "completed_percentage", nullable = false, precision = 7, scale = 4)
    private BigDecimal completedPercentage;

    @Column(name = "actual_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal actualCost;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
