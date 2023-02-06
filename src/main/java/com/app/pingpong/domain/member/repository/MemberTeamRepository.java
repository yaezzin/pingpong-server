package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.global.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {
    List<MemberTeam> findAllByTeamId(Long teamId);
    MemberTeam findByTeamIdAndMemberId(Long teamId, Long memberId);
    Optional<MemberTeam> findByTeamIdAndMemberIdAndStatus(Long teamId, Long memberId, Status status);
}
