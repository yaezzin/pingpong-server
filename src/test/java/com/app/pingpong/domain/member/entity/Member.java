package com.app.pingpong.domain.member.entity;

import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.global.common.Authority;
import com.app.pingpong.global.common.Status;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.global.common.Status.ACTIVE;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Member(String socialId, String email, String nickname, String profileImage, Status status, Authority authority) {
        this.socialId = socialId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.status = status;
        this.authority = authority;
    }

    public Member toEntity(PasswordEncoder passwordEncoder) {
        return new Member(passwordEncoder.encode(socialId), email, nickname, profileImage, ACTIVE, Authority.ROLE_USER);
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public Long getId() {
        return id;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage){
        this.profileImage = profileImage;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

