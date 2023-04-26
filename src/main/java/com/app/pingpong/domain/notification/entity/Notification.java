package com.app.pingpong.domain.notification.entity;

import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
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

    @Field("member_id")
    private Long id;

    @Field("type")
    private Status type;

    @Field("is_clicked")
    private Boolean isClicked;

    @Field("is_accepted")
    private Boolean isAccepted;

    @CreatedDate
    @Indexed(expireAfterSeconds = 2592000)
    @Field("created_at")
    private LocalDateTime createdAt;

}
