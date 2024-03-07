package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT m FROM Member m " +
            "JOIN Friend f ON f.applicant = m.id " +
            "WHERE f.respondent = :respondentId AND f.status = 'ACTIVE'")
    List<Member> findAllFriendsByRespondent(Long respondentId);

    @Query("SELECT m FROM Member m " +
            "JOIN Friend f ON f.respondent = m.id " +
            "WHERE f.applicant = :applicantId AND f.status = 'ACTIVE'")
    List<Member> findAllFriendsByApplicant(Long applicantId);
}

