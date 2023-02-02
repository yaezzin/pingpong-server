package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<List<Member>> findByNicknameContains(String nickname);

    boolean existsByEmail(String email);
    boolean existsUserByNickname(String nickname);

    @Query("select count(m.id) > 0 from Member m where m.nickname =:nickname and m.status = 'ACTIVE'")
    boolean existsUserByNicknameAndStatus(@Param("nickname") String nickname);

}
