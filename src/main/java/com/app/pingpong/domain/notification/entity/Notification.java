package com.app.pingpong.domain.notification.entity;

import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Document("notification")
public class Notification {

    @Field("memberId")
    private Long memberId;

    @Field("opponentId")
    private Long opponentId;

    @Field("type")
    private Status type;

    @Field("message")
    private String message;

    @Field("is_clicked")
    private Boolean isClicked;

    @Field("is_accepted")
    private Boolean isAccepted;

    @CreatedDate
    @Indexed(expireAfterSeconds = 2592000)
    @Field("created_at")
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long memberId, Long opponentId, Status type, String message) {
        this.memberId = memberId;
        this.opponentId = opponentId;
        this.type = type;
        this.message = message;
        this.isClicked = false;
        this.isAccepted = false;
    }

    public void setClicked() {
        this.isClicked = true;
    }

    public void setAccepted() {
        this.isAccepted = true;
    }
}
