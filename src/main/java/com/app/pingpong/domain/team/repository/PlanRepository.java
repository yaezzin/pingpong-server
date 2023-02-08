package com.app.pingpong.domain.team.repository;

import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findAllByTeamIdAndStatusOrderByWastedTimeDesc(Long teamId, Status status);
    Optional<Plan> findByIdAndStatus(Long planId, Status status);
    Optional<Plan> findByIdAndTeamIdAndStatus(Long planId, Long teamId, Status status);

}
