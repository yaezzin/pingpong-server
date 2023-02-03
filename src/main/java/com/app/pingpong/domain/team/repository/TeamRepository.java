package com.app.pingpong.domain.team.repository;

import com.app.pingpong.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByHostId(Long hostId);

    @Query("select t from Team t where t.id =:id and t.status = 'ACTIVE'")
    Optional<Team> findActiveTeamById(Long id);
}
