package com.app.pingpong.domain.friend.service;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.domain.notification.repository.NotificationRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.app.pingpong.factory.FriendFactory.createFriend;
import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {

    @InjectMocks
    FriendService friendService;

    @Mock
    FriendFactory friendFactory;

    @Mock
    FriendRepository friendRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    NotificationRepository notificationRepository;

    @Test
    public void apply() {
        // given
        Member applicant = createMember();
        Member respondent = createMember();
        FriendRequest request = new FriendRequest(1L, 2L);
        Friend friend = createFriend(request.getApplicantId(), request.getRespondentId());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(applicant)).willReturn(Optional.of(respondent));
        given(friendFactory.existsRequestToRespondent(any(), any(), any())).willReturn(false).willReturn(false).willReturn(false);
        given(friendRepository.save(any())).willReturn(friend);

        // when
        FriendResponse result = friendService.apply(request);

        // then
        verify(friendRepository, times(1)).save(any());
        assertThat(result.getApplicantId()).isEqualTo(request.getApplicantId());
        assertThat(result.getRespondentId()).isEqualTo(request.getRespondentId());
    }

    @Test
    public void applyExceptionByApplicantNotFound() {
        // given
        FriendRequest request = new FriendRequest(1L, 2L);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.apply(request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void applyExceptionByRespondentNotFound() {
        // given
        Member applicant = createMember();
        FriendRequest request = new FriendRequest(1L, 2L);
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(applicant)).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.apply(request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void applyExceptionByAlreadyFriendRequestByApplicant() {
        // given
        Member applicant = createMember();
        Member respondent = createMember();
        FriendRequest request = new FriendRequest(1L, 2L);

        given(memberRepository.findByIdAndStatus(any(), any()))
                .willReturn(Optional.of(applicant))
                .willReturn(Optional.of(respondent));
        given(friendFactory.existsRequestToRespondent(any(), any(), eq(WAIT))).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.apply(request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_ALREADY_FRIEND_REQUEST);
    }

    @Test
    public void applyExceptionByAlreadyFriendRequestByRespondent() {
        // given
        Member applicant = createMember();
        Member respondent = createMember();
        FriendRequest request = new FriendRequest(1L, 2L);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(applicant)).willReturn(Optional.of(respondent));
        given(friendFactory.existsRequestToRespondent(any(), any(), eq(WAIT))).willReturn(false).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.apply(request));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_ALREADY_GET_FRIEND_REQUEST);
    }

    @Test
    public void applyExceptionByAlreadyFriendShip() {
        // given
        Member applicant = createMember();
        Member respondent = createMember();
        FriendRequest request = new FriendRequest(1L, 2L);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(applicant)).willReturn(Optional.of(respondent));
        given(friendFactory.existsRequestToRespondent(any(), any(), any())).willReturn(false).willReturn(false).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.apply(request));
        assertThat(exception.getStatus()).isEqualTo(ALREADY_ON_FRIEND);
    }

    @Test
    public void accept() {
        // given
        Friend friend = createFriend(1L, 2L);
        Notification notification = Notification.builder()
                .memberId(friend.getApplicant())
                .opponentId(friend.getRespondent())
                .type(FRIEND)
                .message("message")
                .build();

        given(friendFactory.isFriend(any(), any())).willReturn(false);
        given(friendFactory.findWaitRequestBy(any(), any())).willReturn(Optional.of(friend));
        given(notificationRepository.findByMemberIdAndOpponentId(any(), any())).willReturn(Optional.of(notification));

        // when
        StatusCode code = friendService.accept(friend.getRespondent(), friend.getApplicant());

        // then
        assertThat(code).isEqualTo(SUCCESS_ACCEPT_FRIEND);
        assertThat(friend.getStatus()).isEqualTo(ACTIVE);
        assertThat(notification.getIsAccepted()).isEqualTo(true);
    }

    @Test
    public void acceptExceptionByAlreadyFriend() {
        // given
        given(friendFactory.isFriend(any(), any())).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.accept(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(ALREADY_ON_FRIEND);
    }

    @Test
    public void acceptExceptionByFriendNotFound() {
        // given
        given(friendFactory.isFriend(any(), any())).willReturn(false);
        given(friendFactory.findWaitRequestBy(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.accept(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(FRIEND_NOT_FOUND);
    }

    @Test
    public void acceptExceptionByNotificationNotFound() {
        // given
        Friend friend = createFriend(1L, 2L);

        given(friendFactory.isFriend(any(), any())).willReturn(false);
        given(friendFactory.findWaitRequestBy(any(), any())).willReturn(Optional.of(friend));
        given(notificationRepository.findByMemberIdAndOpponentId(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.accept(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(NOTIFICATION_NOT_FOUND);
    }

    @Test
    public void refuse() {
        // given
        Friend friend = createFriend(1L, 2L);
        Notification notification = Notification.builder()
                .memberId(friend.getApplicant())
                .opponentId(friend.getRespondent())
                .type(FRIEND)
                .message("message")
                .build();

        given(friendFactory.isFriend(any(), any())).willReturn(false);
        given(friendFactory.findWaitRequestBy(any(), any())).willReturn(Optional.of(friend));
        given(notificationRepository.findByMemberIdAndOpponentId(any(), any())).willReturn(Optional.of(notification));

        // when
        StatusCode code = friendService.refuse(friend.getRespondent(), friend.getApplicant());

        // then
        assertThat(code).isEqualTo(SUCCESS_REFUSE_FRIEND);
        assertThat(friend.getStatus()).isEqualTo(DELETE);
        assertThat(notification.getIsAccepted()).isEqualTo(true);
    }

    @Test
    public void refuseExceptionByAlreadyFriend() {
        // given
        given(friendFactory.isFriend(any(), any())).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.refuse(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(ALREADY_ON_FRIEND);
    }

    @Test
    public void refuseExceptionByFriendNotFound() {
        // given
        given(friendFactory.isFriend(any(), any())).willReturn(false);
        given(friendFactory.findWaitRequestBy(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> friendService.refuse(1L, 2L));
        assertThat(exception.getStatus()).isEqualTo(FRIEND_NOT_FOUND);
    }

    @Test
    public void getMyFriends() {

    }

    @Test
    public void getMyFriendsExceptionBy() {
        // given
        List<Friend> friends = new ArrayList<>();


        given(friendRepository.findAllFriendsByMemberId(any())).willReturn();
    }
}
