package com.app.pingpong.domain.team.entity;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 18)
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @LastModifiedDate
    private LocalDateTime wastedTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 10, columnDefinition = "varchar(10) default 'INCOMPLETE'")
    @Enumerated(EnumType.STRING)
    private Status achievement;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member manager;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Plan(String title, LocalDate date) {
        this.title = title;
        this.date = date;
    }
}
