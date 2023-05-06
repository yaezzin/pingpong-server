package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static com.app.pingpong.factory.FriendFactory.createFriend;
import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.WAIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.DateUtil.now;

@DataJpaTest
public class FriendRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendFactory friendFactory;

    @Autowired
    FriendRepository friendRepository;

    @Test
    public void findAllFriendsByMemberId() {
        // given
        Member loginMember = memberRepository.save(createMember());
        Member opponentMember = memberRepository.save(createMember());
        Friend friend = new Friend(loginMember.getId(), opponentMember.getId(), ACTIVE, now());
        friendRepository.save(friend);

        // when
        List<Member> list = friendRepository.findAllFriendsByMemberId(loginMember.getId());

        // then
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    public void existsRequestToRespondent() {
        // given
        Member applicant = memberRepository.save(createMember());
        Member respondent = memberRepository.save(createMember());
        Friend friend = friendRepository.save(createFriend(applicant.getId(), respondent.getId()));

        // when
        boolean result = friendFactory.existsRequestToRespondent(applicant.getId(), respondent.getId(), WAIT);

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
        Optional<Friend> result = friendFactory.findWaitRequestBy(applicant.getId(), respondent.getId());

        // then
        assertThat(result.get().getStatus()).isEqualTo(WAIT);
    }
}
