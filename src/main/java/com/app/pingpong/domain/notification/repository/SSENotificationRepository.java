package com.app.pingpong.domain.notification.repository;

import com.app.pingpong.domain.notification.entity.SSENotification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SSENotificationRepository extends MongoRepository<SSENotification, String> {
    List<SSENotification> findAllByReceiverId(Long id);
}
