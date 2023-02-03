package com.app.pingpong.domain.member.entity;

import com.app.pingpong.domain.team.entity.Team;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class MemberTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

}
