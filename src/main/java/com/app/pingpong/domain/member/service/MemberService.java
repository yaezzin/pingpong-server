package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.Status;
import com.app.pingpong.domain.member.repository.MemberRepository;

import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.app.pingpong.domain.member.entity.Status.DELETE;
import static com.app.pingpong.global.exception.StatusCode.*;
import static com.app.pingpong.global.util.RegexUtil.isRegexNickname;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        Member member = request.toEntity(passwordEncoder);
        return MemberResponse.of(memberRepository.save(member));
    }

    @Transactional
    public BaseResponse<String> validateNickname(String nickname){
        if (!isRegexNickname(nickname)) {
            throw new BaseException(INVALID_NICKNAME);
        }
        if (memberRepository.existsUserByNicknameAndStatus(nickname)) {
            throw new BaseException(USER_NICKNAME_ALREADY_EXISTS);
        }
        return new BaseResponse(SUCCESS_VALIDATE_NICKNAME);
    }

    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        return MemberResponse.of(member);
    }

    @Transactional
    public MemberResponse update(Long id, UpdateRequest request) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        s3Uploader.deleteFile(member.getProfileImage());

        validateNickname(request.getNickname());
        member.setNickname(request.getNickname());
        member.setProfileImage(request.getProfileImage());

        return MemberResponse.of(member);
    }

    @Transactional
    public BaseResponse<String> delete(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        member.setStatus(DELETE);
        return new BaseResponse<>(SUCCESS_USER_DELETE);
    }
}
