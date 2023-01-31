package com.app.pingpong.domain.member.dto.request;

import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.app.pingpong.domain.member.entity.Status.ACTIVE;

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
        return Member.builder()
                .socialId(passwordEncoder.encode(socialId))
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .status(ACTIVE)
                .authority(Authority.ROLE_USER)
                .build();
    }
}
