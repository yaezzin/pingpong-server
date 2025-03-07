package com.app.pingpong.domain.friend.service;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendQueryRepository;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.domain.notification.repository.NotificationRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.util.FcmUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;

@RequiredArgsConstructor
@Service
public class FriendService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final FriendQueryRepository friendQueryRepository;
    private final NotificationRepository notificationRepository;
    private final FcmUtil fcmUtil;

    public FriendResponse apply(FriendRequest request) throws IOException {
        Member applicant = memberRepository.findByIdAndStatus(request.getApplicantId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member respondent = memberRepository.findByIdAndStatus(request.getRespondentId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        checkFriendRequest(applicant, respondent);
        sendFriendFcm(respondent);
        return FriendResponse.of(friendRepository.save(request.toEntity(applicant.getId(), respondent.getId())));
    }

    @Transactional
    public StatusCode accept(Long opponentId, Long loginMemberId) {
        Friend request = getWaitingFriendRequest(opponentId, loginMemberId);
        setStatusActive(request);
        setNotificationAccepted(opponentId, loginMemberId);
        return SUCCESS_ACCEPT_FRIEND;
    }

    @Transactional
    public StatusCode refuse(Long opponentId, Long loginMemberId) {
        Friend friend = getWaitingFriendRequest(opponentId, loginMemberId);
        setStatusDelete(friend);
        setNotificationAccepted(opponentId, loginMemberId);
        return SUCCESS_REFUSE_FRIEND;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMyFriends(Long id) {
        List<Member> friends1 = friendRepository.findAllFriendsByApplicant(id);
        List<Member> friends2 = friendRepository.findAllFriendsByRespondent(id);

        List<MemberResponse> friendList = new ArrayList<>();
        for (Member m : friends1) {
            if (!m.getStatus().equals(DELETE)) {
                friendList.add(MemberResponse.of(m));
            }
        }

        for (Member m : friends2) {
            if (!m.getStatus().equals(DELETE)) {
                friendList.add(MemberResponse.of(m));
            }
        }

        return friendList;
    }

    @Transactional
    public StatusCode unfollow(Long memberId, Long loginMemberId) {
        Friend friend = friendQueryRepository.findFriendById(memberId, loginMemberId).orElseThrow(() -> new BaseException(FRIEND_NOT_FOUND));
        setStatusDelete(friend);
        return SUCCESS_UNFOLLOW_FRIEND;
    }

    private void checkFriendRequest(Member applicant, Member respondent) {
        /* 내가 상대방에게 보낸 친구 신청이 있는지 확인 -> WAIT */
        if (friendQueryRepository.existsRequestToRespondent(applicant.getId(), respondent.getId(), WAIT)) {
            throw new BaseException(MEMBER_ALREADY_FRIEND_REQUEST);
        }
        /* 상대방이 나한테 보낸 친구신청이 있는지 확인 */
        if (friendQueryRepository.existsRequestToRespondent(respondent.getId(), applicant.getId(), WAIT)) {
            throw new BaseException(MEMBER_ALREADY_GET_FRIEND_REQUEST);
        }
        /* 이미 친구를 수락하였는지 확인 -> */
        if (friendQueryRepository.existsRequestToRespondent(applicant.getId(), respondent.getId(), ACTIVE)) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
    }

    private Friend getWaitingFriendRequest(Long opponentId, Long loginMemberId) {
        boolean friendship = friendQueryRepository.isFriend(opponentId, loginMemberId);
        if (friendship) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
        Friend friend = friendQueryRepository.findWaitRequestBy(opponentId, loginMemberId).orElseThrow(() -> new BaseException(FRIEND_NOT_FOUND));
        return friend;
    }

    private void setStatusActive(Friend friend) {
        friend.setStatus(ACTIVE);
    }

    private void setStatusDelete(Friend friend) {
        friend.setStatus(DELETE);
    }

    private void setNotificationAccepted(Long opponentId, Long loginMemberId) {
        List<Notification> notifications = notificationRepository.findAllByMemberIdAndOpponentIdAndIsAccepted(opponentId, loginMemberId, false).orElseThrow(() -> new BaseException(NOTIFICATION_NOT_FOUND));
        for (Notification notification : notifications) {
            notification.setAccepted();
            notificationRepository.save(notification);
        }
    }

    private void sendFriendFcm(Member respondent) throws IOException {
        fcmUtil.sendMessageTo(respondent.getFcmToken(), 3);
    }
}

