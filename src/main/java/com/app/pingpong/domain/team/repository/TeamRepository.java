package com.app.pingpong.domain.team.repository;

import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByHostId(Long hostId);
    Optional<Team> findByIdAndStatus(Long id, Status status);
}
