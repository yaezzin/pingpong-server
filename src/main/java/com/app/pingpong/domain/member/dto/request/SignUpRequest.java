package com.app.pingpong.domain.member.dto.request;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.app.pingpong.global.common.status.Status.ACTIVE;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String socialId;
    private String email;
    private String nickname;
    private String profileImage;

    @Builder
    public Member toEntity(PasswordEncoder passwordEncoder) {
        Member member = Member.builder()
                .socialId(passwordEncoder.encode(socialId))
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .status(ACTIVE)
                .authority(Authority.ROLE_USER)
                .build();
        return member;
    }
}
