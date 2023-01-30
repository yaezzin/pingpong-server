package com.app.pingpong.domain.member.dto.request;

import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String socialId;
    private String email;
    private String nickname;
    private String profileImage;

    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .socialId(passwordEncoder.encode(socialId))
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .authority(Authority.ROLE_USER)
                .build();
    }
}
