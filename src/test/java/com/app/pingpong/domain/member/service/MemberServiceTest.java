package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    MemberService memberService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    public void signup() {
        // given
        Member member = createMember();
        SignUpRequest request = new SignUpRequest("socialId", "email", "nickname", "profileImage");
        given(memberRepository.save(any())).willReturn(member);

        // when
        MemberResponse response = memberService.signup(request);

        // then
        verify(memberRepository, times(1)).save(any());
        assertThat(response.getMemberId()).isEqualTo(member.getId());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
    }

    @Test
    public void validateNickname() {
        // given
        Member member = createMember();
        given(memberRepository.existsMemberByNicknameAndStatus(any())).willReturn(false);

        // when
        StatusCode code = memberService.validateNickname(member.getNickname());

        // then
        assertThat(code).isEqualTo(SUCCESS_VALIDATE_NICKNAME);
    }

    @Test
    public void validateNicknameExceptionByAlreadyExists() {
        // given
        Member member = createMember();
        given(memberRepository.existsMemberByNicknameAndStatus(any())).willReturn(true);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.validateNickname(member.getNickname()));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NICKNAME_ALREADY_EXISTS);
    }

    @Test
    public void validateException() {
        // given
        Member member = createMember("nicknameIsInvalid");

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.validateNickname(member.getNickname()));
        assertThat(exception.getStatus()).isEqualTo(INVALID_NICKNAME);
    }


}