package com.app.pingpong.domain.notification.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationTeamRequest;
import com.app.pingpong.domain.notification.dto.response.NotificationResponse;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.domain.notification.repository.NotificationRepository;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final PlanRepository planRepository;

    @Transactional
    public StatusCode notifyTodo(NotificationRequest request, Long loginMemberId) {
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member me = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Plan plan = planRepository.findByIdAndStatus(request.getPlanId(), ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));

        String message = me.getNickname() + "님이 할 일 " + plan.getTitle() + "을 회원님께 넘겼어요";
        Notification notification = Notification.builder()
                .memberId(request.getMemberId())
                .opponentId(loginMemberId)
                .type(TODO)
                .message(message)
                .build();
        notificationRepository.save(notification);

        return SUCCESS_SEND_NOTIFICATION;
    }

    @Transactional
    public StatusCode notifyFriend(NotificationFriendRequest request, Long loginMemberId) {
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member me = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String message = me.getNickname() + "님이 친구 신청을 보냈어요";
        Notification notification = Notification.builder()
                .memberId(loginMemberId)
                .opponentId(request.getMemberId())
                .type(FRIEND)
                .message(message)
                .build();

        notificationRepository.save(notification);

        return SUCCESS_SEND_NOTIFICATION;
    }

    @Transactional
    public StatusCode notifyTeam(NotificationTeamRequest request, Long loginMemberId) {
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Team team = teamRepository.findByIdAndStatus(request.getTeamId(), ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        String message = team.getName() + "의 초대를 받았어요.";
        Notification notification = Notification.builder()
                .memberId(loginMemberId)
                .opponentId(request.getMemberId())
                .type(TEAM)
                .message(message)
                .build();

        notificationRepository.save(notification);

        return SUCCESS_SEND_NOTIFICATION;
    }

    @Transactional
    public List<NotificationResponse> findAll(Long loginMemberId) {
        List<Notification> notifications = notificationRepository.findAllByOpponentIdOrderByCreatedAtAsc(loginMemberId);

        List<NotificationResponse> list = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getOpponentId() != null) {
                Member member = memberRepository.findById(notification.getMemberId()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
                notification.setClicked();
                notificationRepository.save(notification);
                list.add(NotificationResponse.of(notification, member));
            }
        }
        return list;
    }

    public StatusCode existUnReadNotification(Long id) {
        boolean exists = notificationRepository.existsAllByOpponentIdAndIsClicked(id, false);

        StatusCode statusCode;
        if (exists) {
            statusCode = SUCCESS_EXISTS_UNREAD_NOTIFY;
        } else {
            statusCode = SUCCESS_EXISTS_NOTIFY;
        }
        return statusCode;
    }
}
