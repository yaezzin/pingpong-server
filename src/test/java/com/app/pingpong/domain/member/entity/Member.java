package com.app.pingpong.domain.member.entity;

import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.Status;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Member {

    @Id
    private Long id;

    private String socialId;

    private String email;

    private String nickname;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @OneToMany(mappedBy = "member")
    private List<MemberTeam> memberTeams = new ArrayList<>();

    @OneToMany(mappedBy = "manager")
    private List<Plan> plans = new ArrayList<>();

    public Member(Long id, String socialId, String email, String nickname, String profileImage, Status status, Authority authority) {
        this.id = id;
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.status = status;
        this.authority = authority;
    }
}

