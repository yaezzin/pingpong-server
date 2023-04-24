package com.app.pingpong.domain.friend.entity;

import com.app.pingpong.domain.member.entity.Member;
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

    @OneToOne
    private Member applicant;

    @OneToOne
    private Member respondent;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Date createdAt;

    @Builder
    public Friend(Member applicant, Member respondent, Status status, Date createdAt) {
        this.applicant = applicant;
        this.respondent = respondent;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
