package org.example.planner_backend.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "family")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(name = "is_setup_completed", nullable = false)
    private boolean isSetupCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "shopping_day", nullable = false, length = 20)
    @Builder.Default
    private DayOfWeek shoppingDay = DayOfWeek.SUNDAY;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_weekday_servings", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private MealServings defaultWeekdayServings = new MealServings(3, null, 4);

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_weekend_servings", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private MealServings defaultWeekendServings = new MealServings(4, 4, 4);

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
