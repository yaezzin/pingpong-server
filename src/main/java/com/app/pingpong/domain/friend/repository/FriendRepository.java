package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("select distinct m from Member m inner join Friend f on (f.applicant =:id or f.respondent =:id) where f.status = 'ACTIVE'")
    List<Member> findAllFriendsByMemberId(Long id);
}

