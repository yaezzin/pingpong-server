package com.app.pingpong.domain.notification.repository;

import com.app.pingpong.domain.notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, Long> {
    Notification findByMemberIdAndOpponentId(Long memberId, Long opponentId);

    List<Notification> findAllByMemberIdOrderByCreatedAtAsc(Long memberId);

    List<Notification> findAllByOpponentIdOrderByCreatedAtAsc(Long opponentId);
}
