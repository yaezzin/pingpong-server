package com.app.pingpong.domain.team.repository;

import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByIdAndStatus(Long planId, Status status);

    Optional<Plan> findByIdAndTeamIdAndStatusAndAchievement(Long planId, Long teamId, Status status, Status achievement);

    Optional<List<Plan>> findAllByTeamIdAndStatusOrderByWastedTimeDesc(Long teamId, Status status);

    List<Plan> findAllByManagerIdAndTeamIdAndStatusOrderByWastedTimeDesc(Long memberId, Long teamId, Status status);

    List<Plan> findAllByTeamIdAndStatusAndDateBetween(Long teamId, Status status, LocalDate startDate, LocalDate endDate);

    List<Plan> findAllByManagerIdAndStatusAndDateBetweenOrderByDateAsc(Long id, Status status, LocalDate startDate, LocalDate endDate);

    List<Plan> findAllByManagerIdAndStatusAndDateOrderByDateAsc(Long id, Status status, LocalDate date);

    List<Plan> findAllByTeamIdAndDateAndStatus(Long teamId, LocalDate date, Status status);

    List<Plan> findAllByTeamIdAndManagerIdAndStatusAndDate(Long id, Long currentMemberId, Status active, LocalDate date);
}
