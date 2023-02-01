package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("select count(f.id) > 0 from Friend f where f.applicant.id =:applicantId " +
             "and f.respondent.id =:respondentId and f.status = 'WAIT'")
    boolean existsWaitByApplicantIdAndRespondentId(@Param("applicantId") Long applicantId, @Param("respondentId") Long respondentId);

    @Query("select count(f.id) > 0 from Friend f where f.applicant.id =:applicantId " +
            "and f.respondent.id =:respondentId and f.status = 'ACTIVE'")
    boolean existsByApplicantIdAndRespondentId(@Param("applicantId") Long applicantId, @Param("respondentId") Long respondentId);

    @Query("select f from Friend f where f.applicant.id =:applicantId " +
            "and f.respondent.id =:respondentId and f.status = 'WAIT' or f.status = 'ACTIVE'")
    Optional<Friend> findByApplicantIdAndRespondentId(Long applicantId, Long respondentId);

    @Query("select count(f.id) from Friend f where (f.applicant.id =:id or f.respondent.id =:id) and f.status = 'ACTIVE'")
    int findFriendCount(Long id);

    @Query("select f from Friend f where f.applicant.id =:id or f.respondent.id =:id and f.status = 'ACTIVE'")
    List<Friend> findAllFriendsByUserId(Long id);
}
