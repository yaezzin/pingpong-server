package com.app.pingpong.domain.team.entity;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    private Status status;

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
