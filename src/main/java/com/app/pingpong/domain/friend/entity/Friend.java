package com.app.pingpong.domain.friend.entity;

import com.app.pingpong.global.common.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicant;

    @Column(nullable = false)
    private Long respondent;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private Date createdAt;

    @Builder
    public Friend(Long applicant, Long respondent, Status status, Date createdAt) {
        this.applicant = applicant;
        this.respondent = respondent;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
