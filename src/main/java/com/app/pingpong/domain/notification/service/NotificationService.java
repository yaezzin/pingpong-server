package com.app.pingpong.domain.notification.service;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.notification.dto.request.NotificationFriendRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationRequest;
import com.app.pingpong.domain.notification.dto.request.NotificationTeamRequest;
import com.app.pingpong.domain.notification.dto.response.NotificationExistResponse;
import com.app.pingpong.domain.notification.dto.response.NotificationResponse;
import com.app.pingpong.domain.notification.dto.response.SSENotificationResponse;
import com.app.pingpong.domain.notification.dto.response.SSENotificationsResponse;
import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.domain.notification.entity.SSENotification;
import com.app.pingpong.domain.notification.repository.EmitterRepository;
import com.app.pingpong.domain.notification.repository.NotificationRepository;
import com.app.pingpong.domain.notification.repository.SSENotificationRepository;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final PlanRepository planRepository;
    private final EmitterRepository emitterRepository;
    private final SSENotificationRepository sseNotificationRepository;

    @Transactional
    public StatusCode notifyTodo(NotificationRequest request, Long loginMemberId) {
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member me = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Plan plan = planRepository.findByIdAndStatus(request.getPlanId(), ACTIVE).orElseThrow(() -> new BaseException(PLAN_NOT_FOUND));

        String message = me.getNickname() + "님이 할 일 " + plan.getTitle() + "을 회원님께 넘겼어요.";
        Notification notification = Notification.builder()
                .memberId(request.getMemberId())
                .opponentId(loginMemberId)
                .type(TODO)
                .teamId(plan.getTeam().getId())
                .message(message)
                .build();
        notificationRepository.save(notification);

        return SUCCESS_SEND_NOTIFICATION;
    }

    @Transactional
    public StatusCode notifyFriend(NotificationFriendRequest request, Long loginMemberId) {
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member me = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String message = me.getNickname() + "님이 친구 신청을 보냈어요.";
        Notification notification = Notification.builder()
                .type(FRIEND)
                .memberId(loginMemberId)
                .opponentId(request.getMemberId())
                .teamId(null)
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
                .type(TEAM)
                .memberId(loginMemberId)
                .opponentId(request.getMemberId())
                .teamId(team.getId())
                .message(message)
                .build();

        notificationRepository.save(notification);
        send(request.getMemberId(), message);

        return SUCCESS_SEND_NOTIFICATION;
    }

    @Transactional
    public StatusCode notifyHost(NotificationTeamRequest request, Long loginMemberId) {
        Member loginMember = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        String message = loginMember.getNickname() + "님이 방장을 넘겼습니다.";
        Notification notification = Notification.builder()
                .type(HOST)
                .memberId(loginMemberId)
                .opponentId(opponent.getId())
                .teamId(request.getTeamId())
                .message(message)
                .build();

        notificationRepository.save(notification);

        return SUCCESS_SEND_NOTIFICATION;
    }

    public StatusCode notifyEmit(NotificationTeamRequest request, Long loginMemberId) {
        Member loginMember = memberRepository.findByIdAndStatus(loginMemberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member opponent = memberRepository.findByIdAndStatus(request.getMemberId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Team team = teamRepository.findByIdAndStatus(request.getTeamId(), ACTIVE).orElseThrow(() -> new BaseException(TEAM_NOT_FOUND));

        String message = "방장 " + loginMember.getNickname() + "님이 " + team.getName() + "에서 내보냈습니다.";
        Notification notification = Notification.builder()
                .type(EMIT)
                .memberId(loginMemberId)
                .opponentId(opponent.getId())
                .teamId(request.getTeamId())
                .message(message)
                .build();

        notificationRepository.save(notification);

        return SUCCESS_SEND_NOTIFICATION;
    }

    @Transactional
    public List<NotificationResponse> findAll(Long loginMemberId) {
        List<Notification> notifications = notificationRepository.findAllByOpponentIdOrderByCreatedAtDesc(loginMemberId);

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

    public NotificationExistResponse existUnReadNotification(Long id) {
        boolean exists = notificationRepository.existsAllByOpponentIdAndIsClicked(id, false);
        return new NotificationExistResponse(exists);
    }

    /*
     * SSE 연결
     * */
    public SseEmitter subscribe(String lastEventId, Long userId) {
        String id = userId + "_" + System.currentTimeMillis();

        // sse 연결 요청에 응답하기 위해 sseEmitter객체 만들기
        SseEmitter emitter = emitterRepository.save(id, new SseEmitter(DEFAULT_TIMEOUT));

        // 만약 정상동작하지 않으면 객체 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(id));
        emitter.onTimeout(() -> emitterRepository.deleteById(id));

        // 503 방지를 위해 더미 이벤트 전송
        sendToClient(emitter, id, "EventStream Created. [userId=" + userId + "]");

        // 미수신 이벤트가 있을 경우 전송하여 이벤트 유실 예방
        if (!lastEventId.isEmpty()) { //
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
        }
        return emitter;
    }

    /*
     * 데이터 전송
     * */
    public StatusCode send(Long receiverId, String content) {
        SSENotification notification = createNotification(receiverId, content);

        String id = String.valueOf(receiverId);
        sseNotificationRepository.save(notification);

        // 로그인 한 유저의 SseEmitter 모두 가져오기
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(id);
        if (sseEmitters.isEmpty()) {
            throw new BaseException(SSE_CONNECT_FAILED);
        }
        sseEmitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendToClient(emitter, key, SSENotificationResponse.from(notification));
                }
        );

        return SUCCESS_SEND_SSE_NOTIFICATION;
    }

    private void sendToClient(SseEmitter emitter, String id, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name("sse")
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(id);
            throw new RuntimeException("연결 오류!");
        }
    }

    private SSENotification createNotification(Long receiverId, String content) {
        return SSENotification.builder()
                .receiverId(receiverId)
                .content(content)
                .isClicked(false)
                .build();
    }

    @Transactional
    public SSENotificationsResponse findAllById(Long loginMemberId) {
        List<SSENotificationResponse> responses = sseNotificationRepository.findAllByReceiverId(loginMemberId).stream()
                .map(SSENotificationResponse::from)
                .collect(Collectors.toList());

        long unreadCount = responses.stream()
                .filter(notification -> !notification.isClicked())
                .count();

        return SSENotificationsResponse.of(responses, unreadCount);
    }

    @Transactional
    public StatusCode readNotification(String id) {
        SSENotification notification = sseNotificationRepository.findById(id)
                .orElseThrow(() -> new BaseException(NOTIFICATION_NOT_FOUND));
        notification.click();
        sseNotificationRepository.save(notification);
        return SUCCESS_READ_SSE_NOTIFICATION;
    }

}
