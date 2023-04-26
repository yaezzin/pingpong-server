package com.app.pingpong.domain.notification.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationRequest;
import com.app.pingpong.domain.notification.dto.response.NotificationFriendResponse;
import com.app.pingpong.domain.notification.dto.response.NotificationTodoResponse;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.domain.notification.repository.NotificationRepository;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.MEMBER_NOT_FOUND;
import static com.app.pingpong.global.common.exception.StatusCode.PLAN_NOT_FOUND;
import static com.app.pingpong.global.common.status.Status.*;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;

    public NotificationTodoResponse notifyTodo(NotificationRequest request, Long loginMemberId) {
        // 할 일 넘긴 것 저장
        Notification notification = new Notification(loginMemberId, TODO, false, false, LocalDateTime.now());
        notificationRepository.save(notification);

        // 리턴할 정보들
        Member member = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Plan plan = planRepository.findByIdAndStatus(request.getPlanId(), ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));

        return NotificationTodoResponse.of(member.getNickname(), plan.getTitle());
    }

    public NotificationFriendResponse notifyFriend(NotificationFriendRequest request, Long loginMemberId) {
        Notification notification = new Notification(loginMemberId, FRIEND, false, false, LocalDateTime.now());
        notificationRepository.save(notification);

        Member member = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        return NotificationFriendResponse.of(member.getNickname());
    }


    public List<Notification> findAll() {
        List<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc();


        return notifications;
    }
}
