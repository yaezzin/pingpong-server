package com.app.pingpong.domain.friend.service;

import com.app.pingpong.domain.friend.dto.request.FriendRefuseRequest;
import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.repository.NotificationRepository;
import com.app.pingpong.domain.notification.service.NotificationService;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.DateUtil.now;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class FriendServiceTest {

    @Autowired
    FriendService friendService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FriendRepository friendRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @BeforeEach
    public void setUp() {
        notificationRepository.deleteAll();
        friendRepository.deleteAll();
    }

    @Test
    @DisplayName("친구 신청 성공 테스트")
    public void apply() {
        // given
        Member applicant = memberRepository.save(createMember("applicant"));
        Member respondent = memberRepository.save(createMember("respondent"));
        FriendRequest request = new FriendRequest(applicant.getId(), respondent.getId());

        // when
        FriendResponse response = friendService.apply(request);

        // then
        assertThat(response.getApplicantId()).isEqualTo(applicant.getId());
        assertThat(response.getRespondentId()).isEqualTo(respondent.getId());
    }

    @Test
    @DisplayName("친구 신청 실패 - 유저가 존재하지 않음")
    public void apply_fail_member_not_found() {
        // given
        Member applicant = createMember("applicant");
        Member respondent = createMember("respondent");
        FriendRequest request = new FriendRequest(applicant.getId(), respondent.getId());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> {
            friendService.apply(request);
        });
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("친구 신청 실패 - 이미 친구 신청이 존재하는 경우 (중복 신청)")
    public void apply_fail_already_friend_request() {
        Member applicant = memberRepository.save(createMember("applicant"));
        Member respondent = memberRepository.save(createMember("respondent"));
        FriendRequest request = new FriendRequest(applicant.getId(), respondent.getId());

        // when, then
        FriendResponse response = friendService.apply(request);
        assertThrows(BaseException.class, () -> {
            friendService.apply(request);
        });
    }

    @Test
    @DisplayName("친구 신청 실패 - 상대방이 나한테 보낸 친구 신청이 존재")
    public void apply_fail_already_get_friend_request() {
        // given
        Member applicant = memberRepository.save(createMember("applicant"));
        Member respondent = memberRepository.save(createMember("respondent"));
        FriendRequest request = new FriendRequest(applicant.getId(), respondent.getId());
        FriendRequest opponentRequest = new FriendRequest(respondent.getId(), applicant.getId());

        // when, then
        friendService.apply(request);
        assertThrows(BaseException.class, () -> {
            friendService.apply(opponentRequest);
        });
    }

    @Test
    @DisplayName("친구 신청 실패 - 이미 친구")
    public void apply_fail_already_friendship() {
        // given
        Member applicant = memberRepository.save(createMember("applicant"));
        Member respondent = memberRepository.save(createMember("respondent"));
        friendRepository.save(new Friend(applicant.getId(), respondent.getId(), ACTIVE, now()));
        FriendRequest request = new FriendRequest(applicant.getId(), respondent.getId());

        // when, then
        assertThrows(BaseException.class, () -> {
            friendService.apply(request);
        });
    }

    @Test
    @DisplayName("친구 신청 수락 + 알림 전송")
    public void accept() {
        // given
        Member loginMember = memberRepository.save(createMember("applicant"));
        Member opponent = memberRepository.save(createMember("respondent"));
        Friend friendship = friendRepository.save(new Friend(opponent.getId(), loginMember.getId(), WAIT, now()));
        NotificationFriendRequest request = new NotificationFriendRequest(loginMember.getId());

        // when
        StatusCode code = notificationService.notifyFriend(request, opponent.getId());
        StatusCode response = friendService.accept(opponent.getId(), loginMember.getId());

        // then
        assertThat(response).isEqualTo(SUCCESS_ACCEPT_FRIEND);
        assertThat(friendship.getStatus()).isEqualTo(ACTIVE);
        assertThat(code).isEqualTo(SUCCESS_SEND_NOTIFICATION);
    }

    @Test
    @DisplayName("친구 신청 수락 실패 - 이미 친구 상태")
    public void accept_fail_already_friendship() {
        // given
        Member loginMember = memberRepository.save(createMember());
        Member opponent = memberRepository.save(createMember());
        Friend friendship = friendRepository.save(new Friend(opponent.getId(), loginMember.getId(), ACTIVE, now()));
        NotificationFriendRequest request = new NotificationFriendRequest(loginMember.getId());
        StatusCode code = notificationService.notifyFriend(request, opponent.getId());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> {
            friendService.accept(opponent.getId(), loginMember.getId());
        });
        assertThat(exception.getStatus()).isEqualTo(ALREADY_ON_FRIEND);
    }

    @Test
    @DisplayName("친구 신청 수락 실패 - 친구 신청이 존재하지 않음")
    public void accept_fail() {
        // given
        Member loginMember = memberRepository.save(createMember("applicant"));
        Member opponent = memberRepository.save(createMember("respondent"));

        // when, then
        assertThrows(BaseException.class, () -> {
            friendService.accept(opponent.getId(), loginMember.getId());
        });
    }

    @Test
    @DisplayName("친구 신청 거절")
    public void refuse() {
        Member loginMember = memberRepository.save(createMember("applicant"));
        Member opponent = memberRepository.save(createMember("respondent"));
        FriendRefuseRequest request = new FriendRefuseRequest(opponent.getId());

        Friend friendship = friendRepository.save(new Friend(opponent.getId(), loginMember.getId(), WAIT, now()));
        NotificationFriendRequest notificationRequest = new NotificationFriendRequest(loginMember.getId());

        // when
        StatusCode code = notificationService.notifyFriend(notificationRequest, opponent.getId());
        StatusCode response = friendService.refuse(request, loginMember.getId());

        // then
        assertThat(response).isEqualTo(SUCCESS_REFUSE_FRIEND);
        assertThat(friendship.getStatus()).isEqualTo(DELETE);
        assertThat(code).isEqualTo(SUCCESS_SEND_NOTIFICATION);
    }

    @Test
    @DisplayName("친구 신청 거절 실패 - 친구 신청이 존재하지 않음")
    public void refuse_fail() {
        // given
        Member loginMember = memberRepository.save(createMember("applicant"));
        Member opponent = memberRepository.save(createMember("respondent"));
        FriendRefuseRequest request = new FriendRefuseRequest(opponent.getId());

        // when, then
        assertThrows(BaseException.class, () -> {
            friendService.refuse(request, loginMember.getId());
        });
    }

    @Test
    @DisplayName("친구 신청 거절 실패 - 이미 친구 상태")
    public void refuse_fail_already_friendship() {
        // given
        Member loginMember = memberRepository.save(createMember("applicant"));
        Member opponent = memberRepository.save(createMember("respondent"));
        FriendRefuseRequest request = new FriendRefuseRequest(opponent.getId());
        Friend friendship = friendRepository.save(new Friend(opponent.getId(), loginMember.getId(), ACTIVE, now()));
        NotificationFriendRequest notificationRequest = new NotificationFriendRequest(loginMember.getId());
        StatusCode code = notificationService.notifyFriend(notificationRequest, opponent.getId());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> {
            friendService.refuse(request, loginMember.getId());
        });
        assertThat(exception.getStatus()).isEqualTo(ALREADY_ON_FRIEND);
    }

    @Test
    @DisplayName("나의 친구 전체 조회")
    public void getMyFriends() {
        Member member1 = memberRepository.save(createMember());
        Member member2 = memberRepository.save(createMember());
        Member member3 = memberRepository.save(createMember());

        Friend friendship1 = friendRepository.save(new Friend(member1.getId(), member2.getId(), ACTIVE, now()));
        Friend friendship2 = friendRepository.save(new Friend(member1.getId(), member3.getId(), ACTIVE, now()));

        // when
        List<MemberResponse> response = friendService.getMyFriends(member1.getId());

        // then
        assertThat(response.size()).isEqualTo(3);
        assertThat(response.get(0).getMemberId()).isEqualTo(member1.getId());
        assertThat(response.get(1).getMemberId()).isEqualTo(member2.getId());
        assertThat(response.get(2).getMemberId()).isEqualTo(member3.getId());
    }


}
