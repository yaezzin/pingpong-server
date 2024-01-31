package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.MemberBadge;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {
    List<MemberBadge> findByMemberId(Long memberId);

    List<MemberBadge> findTop8ByMemberIdAndStatusOrderByBadgeIdAsc(Long memberId, Status status);
}
