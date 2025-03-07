package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static com.app.pingpong.factory.FriendFactory.createFriend;
import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.status.Status.WAIT;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class FriendRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendQueryRepository friendQueryRepository;

    @Autowired
    FriendRepository friendRepository;

    @Test
    public void existsRequestToRespondent() {
        // given
        Member applicant = memberRepository.save(createMember());
        Member respondent = memberRepository.save(createMember());
        Friend friend = friendRepository.save(createFriend(applicant.getId(), respondent.getId()));

        // when
        boolean result = friendQueryRepository.existsRequestToRespondent(applicant.getId(), respondent.getId(), WAIT);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isTrue();
    }

    @Test
    public void findWaitRequestBy() {
        // given
        Member applicant = memberRepository.save(createMember());
        Member respondent = memberRepository.save(createMember());
        Friend friend = friendRepository.save(createFriend(applicant.getId(), respondent.getId()));

        // when
        Optional<Friend> result = friendQueryRepository.findWaitRequestBy(applicant.getId(), respondent.getId());

        // then
        assertThat(result.get().getStatus()).isEqualTo(WAIT);
    }
}
