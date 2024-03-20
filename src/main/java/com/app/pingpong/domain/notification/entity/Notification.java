package com.app.pingpong.domain.notification.entity;

import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Document("notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @Field("type")
    private Status type;

    @Field("member_id")
    private Long memberId;

    @Field("opponent_id")
    private Long opponentId;

    @Field("team_id")
    private Long teamId;

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
    public Notification(Status type, Long memberId, Long opponentId, Long teamId, String message) {
        this.type = type;
        this.memberId = memberId;
        this.opponentId = opponentId;
        this.teamId = teamId;
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
