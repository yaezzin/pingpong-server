package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberDetailResponse;
import com.app.pingpong.domain.member.dto.response.MemberFriendResponse;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.Status;
import com.app.pingpong.domain.member.repository.MemberRepository;

import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.util.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.app.pingpong.domain.member.entity.Status.DELETE;
import static com.app.pingpong.global.exception.StatusCode.*;
import static com.app.pingpong.global.util.RegexUtil.isRegexNickname;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final UserFacade userFacade;
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
        return new BaseResponse<>(SUCCESS_DELETE_USER);
    }

    public MemberDetailResponse getMyPage(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        int friendCount = friendRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    public MemberDetailResponse getOppPage(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        int friendCount = friendRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    public List<Friend> getMyFriends() {
        Member loginUser = userFacade.getCurrentUser();
        List<Friend> friends = friendRepository.findAllFriendsByUserId(loginUser.getId());

        List list = new ArrayList();
        for (Friend f : friends) {
            // 신청자가 나인 경우
            if (f.getApplicant().getId() == loginUser.getId()) {
                Member respondent = f.getRespondent();
                list.add(new MemberResponse(respondent.getId(), respondent.getNickname(), respondent.getProfileImage()));
            }
            // 피신청인자가 나인 경우
            if (f.getRespondent().getId() == loginUser.getId()) {
                Member applicant = f.getApplicant();
                list.add(new MemberResponse(applicant.getId(), applicant.getNickname(), applicant.getProfileImage()));
            }
        }
        return list;
    }

}
