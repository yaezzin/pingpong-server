package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.MemberTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {
    List<MemberTeam> findAllByTeamId(Long teamId);
    MemberTeam findByTeamIdAndMemberId(Long teamId, Long memberId);
}
