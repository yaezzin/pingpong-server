package com.app.pingpong.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Document("snotification") // 할 일 넘김 받았을 때, 친구 신청 받았을 때, 팀 초대 받았을때, 방출됐을떄, 방장 넘김 받았을때
public class SSENotification {

    @Id
    @Indexed(unique = true)
    private String id;

    @Field("receiverId")
    private Long receiverId;

    @Field("content")
    private String content;

    @Field("isClicked")
    private boolean isClicked;

    @CreatedDate
    @Indexed(expireAfterSeconds = 2592000)
    @Field("created_at")
    private LocalDateTime createdAt;

    @Builder
    public SSENotification(Long receiverId, String content, boolean isClicked) {
        this.receiverId = receiverId;
        this.content = content;
        this.isClicked = isClicked;
    }

    public void click() {
        this.isClicked = true;
    }
}
