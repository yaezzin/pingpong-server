package com.app.pingpong.domain.notification.repository;

import com.app.pingpong.domain.notification.entity.Notification;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, Long> {
    Optional<List<Notification>> findAllByMemberIdAndOpponentIdAndIsAccepted(Long memberId, Long opponentId, Boolean isAccepted);

    List<Notification> findAllByOpponentIdOrderByCreatedAtDesc(Long opponentId);

    boolean existsAllByOpponentIdAndIsClicked(Long opponentId, Boolean isClicked);

    Optional<Notification> findByIdAndMemberIdAndOpponentIdAndTypeAndIsAccepted(String notificationId, Long memberId, Long opponentId, Status status, Boolean isAccepted);

    Notification findById(String notificationId);

    Optional<Notification> findByIdAndIsAccepted(String notificationId, Boolean isAccepted);
}
