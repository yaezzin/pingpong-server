package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT DISTINCT m FROM Member m " +
            "JOIN Friend f ON (f.applicant = m.id OR f.respondent = m.id) " +
            "WHERE (f.applicant = :id OR f.respondent = :id) AND f.status = 'ACTIVE' AND m.id =:id")
    List<Member> findAllFriendsByMemberId(Long id);


}

