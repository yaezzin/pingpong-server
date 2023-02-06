package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberDetailResponse;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;

import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.UserFacade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static com.app.pingpong.global.common.Status.ACTIVE;
import static com.app.pingpong.global.common.Status.DELETE;
import static com.app.pingpong.global.exception.StatusCode.*;
import static com.app.pingpong.global.util.RegexUtil.isRegexNickname;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        Member member = request.toEntity(passwordEncoder);
        return MemberResponse.of(memberRepository.save(member));
    }

    @Transactional
    public BaseResponse<String> validateNickname(String nickname) {
        if (!isRegexNickname(nickname)) {
            throw new BaseException(INVALID_NICKNAME);
        }
        if (memberRepository.existsUserByNicknameAndStatus(nickname)) {
            throw new BaseException(USER_NICKNAME_ALREADY_EXISTS);
        }
        return new BaseResponse(SUCCESS_VALIDATE_NICKNAME);
    }

    public MemberResponse findById(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        return MemberResponse.of(member);
    }

    @Transactional
    public MemberResponse update(Long id, UpdateRequest request) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        s3Uploader.deleteFile(member.getProfileImage());

        validateNickname(request.getNickname());
        member.setNickname(request.getNickname());
        member.setProfileImage(request.getProfileImage());

        return MemberResponse.of(member);
    }

    @Transactional
    public BaseResponse<String> delete(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        member.setStatus(DELETE);
        return new BaseResponse<>(SUCCESS_DELETE_USER);
    }

    public MemberDetailResponse getMyPage(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        int friendCount = friendRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    public MemberDetailResponse getOppPage(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        int friendCount = friendRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    public List<Friend> getMyFriends() {
        Member loginUser = userFacade.getCurrentUser();
        List<Friend> friends = friendRepository.findAllFriendsByUserId(loginUser.getId());

        List list = new ArrayList();
        for (Friend f : friends) {
            if (f.getApplicant().getId() == loginUser.getId()) {
                Member respondent = f.getRespondent();
                list.add(new MemberResponse(respondent.getId(), respondent.getNickname(), respondent.getProfileImage()));
            }
            if (f.getRespondent().getId() == loginUser.getId()) {
                Member applicant = f.getApplicant();
                list.add(new MemberResponse(applicant.getId(), applicant.getNickname(), applicant.getProfileImage()));
            }
        }
        return list;
    }

    // 친구 검색 최근 검색어 저장
    @Transactional
    public StatusCode saveSearchLog(SearchLogRequest request) {
        Member member = memberRepository.findByIdAndStatus(request.getId(), ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        String loginUserId = "id" + userFacade.getCurrentUser().getId(); // 내 식별자
        String memberId = "id" + member.getId();
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        listOps.leftPush(loginUserId, memberId);
        return SUCCESS;
    }

    public List<MemberResponse> getSearchLog() {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        String loginUserId = "id" + userFacade.getCurrentUser().getId(); // 내 식별자
        List<String> list1 = new ArrayList<>();
        for (Object o : listOps.range(loginUserId, 0, -1)) {
            String userId = o.toString().substring(2,3); //숫자값만 추출
            if (!list1.contains(userId) && list1.size() <= 10) {
                list1.add(userId);
            }
        }

        List<MemberResponse> list2 = new ArrayList<>();
        for (String l :list1) {
            Long userId = Long.parseLong(l);
            System.out.println("=====" + userId);
            Member member = memberRepository.findById(userId).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
            list2.add(MemberResponse.of(member));
        }
        return list2;
    }

    public List<MemberSearchResponse> findByNickname(String nickname) {
        List<Member> findMembers = memberRepository.findByStatusAndNicknameContains(ACTIVE, nickname).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        List<MemberSearchResponse> list = new ArrayList<>();
        for (Member findMember : findMembers) {
            boolean isFriend = friendRepository.isFriend(userFacade.getCurrentUser().getId(), findMember.getId());
            list.add(MemberSearchResponse.builder()
                    .userId(findMember.getId())
                    .nickname(findMember.getNickname())
                    .profileImage(findMember.getProfileImage())
                    .isFriend(isFriend)
                    .build());
        }
        return list;
    }
}
