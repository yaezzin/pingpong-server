package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Authority;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static com.app.pingpong.global.exception.StatusCode.SUCCESS_VALIDATE_NICKNAME;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Spy @InjectMocks MemberService memberService;
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    public void 회원가입() {
        SignUpRequest request = new SignUpRequest("socialId", "email", "nickname", "profileImage");
        MemberResponse response = MemberResponse.of(request.toEntity(passwordEncoder));
        lenient().doReturn(response).when(memberService).signup(any(SignUpRequest.class));
    }

    @Test
    public void 닉네임_정규식() {
        assertEquals(memberService.validateNickname(createMember().getNickname()).getMessage(), SUCCESS_VALIDATE_NICKNAME.getMessage());
    }

    @Test
    public void 닉네임_정규식_실패() {
        assertThrows(BaseException.class, () -> memberService.validateNickname(createFailedMember().getNickname()));
    }

    @Test
    public void 닉네임_중복_테스트() {
        // given
        given(memberRepository.existsUserByNickname(anyString())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> memberService.validateNickname(createMember().getNickname())).isInstanceOf(BaseException.class);
    }

    private Member createMember() {
        return Member.builder()
                .socialId("123")
                .email("email")
                .nickname("nickname")
                .profileImage("dfd")
                .authority(Authority.ROLE_USER)
                .build();
    }

    private Member createFailedMember() {
        return new Member("123", "email", "nadㅁ@z@!!1231", "profileImage", Authority.ROLE_USER);
    }
}

