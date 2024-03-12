package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndStatus(String email, Status status);

    Optional<Member> findByIdAndStatus(Long id, Status status);

    Optional<List<Member>> findByStatusAndNicknameContains(Status status, String nickname);

    @Query("select count(m.id) > 0 from Member m where m.nickname =:nickname and m.status = 'ACTIVE'")
    boolean existsMemberByNicknameAndStatus(@Param("nickname") String nickname);

    boolean existsByEmail(String email);

    boolean existsMemberByNickname(String nickname);

    Optional<Member> findBySocialIdAndStatus(String id, Status status);

}
