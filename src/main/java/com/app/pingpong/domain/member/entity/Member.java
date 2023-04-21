package com.app.pingpong.domain.member.entity;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.Status;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String socialId;

    private String email;

    @Column(length = 10)
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

    @Builder
    public Member(String socialId, String email, String nickname, String profileImage, Status status, Authority authority) {
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.status = status;
        this.authority = authority;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

