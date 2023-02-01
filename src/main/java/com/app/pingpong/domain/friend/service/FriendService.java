package com.app.pingpong.domain.friend.service;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.Status;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.app.pingpong.domain.member.entity.Status.ACTIVE;
import static com.app.pingpong.domain.member.entity.Status.WAIT;
import static com.app.pingpong.global.exception.StatusCode.*;

@RequiredArgsConstructor
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final UserFacade userFacade;

    public FriendResponse addFriend(FriendRequest request) {
        Member applicant = memberRepository.findById(request.getApplicantId()).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        Member respondent = memberRepository.findById(request.getRespondentId()).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        checkFriend(applicant, respondent);
        return FriendResponse.of(friendRepository.save(request.toEntity(applicant, respondent)));
    }

    @Transactional
    public StatusCode acceptFriend(Long id) {
        Member loginUser = userFacade.getCurrentUser();
        Friend friend = friendRepository.findByApplicantIdAndRespondentId(id, loginUser.getId()).orElseThrow(() -> new BaseException(FRIEND_NOT_FOUND));
        checkAndSetFriendStatus(friend);
        return SUCCESS_ACCEPT_FRIEND;
    }

    private void checkFriend(Member applicant, Member respondent) {
        // "이미 친구신청을 하였으니까 오류 뽑고 프론트에서 팝업 띄워서 친구 삭제하겠습니까?하기
        if (friendRepository.existsWaitByApplicantIdAndRespondentId(applicant.getId(), respondent.getId())) {
            throw new BaseException(USER_ALREADY_FRIEND_REQUEST);
        }
        // "나에게 친구 신청을 한 유저입니다 -> 오류 뽑음 -> ㅊ"
        if (friendRepository.existsWaitByApplicantIdAndRespondentId(respondent.getId(), applicant.getId())) {
            throw new BaseException(USER_ALREADY_GET_FRIEND_REQUEST);
        }
        // "이미 친구입니다."
        if (friendRepository.existsByApplicantIdAndRespondentId(applicant.getId(), respondent.getId())) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
    }

    private void checkAndSetFriendStatus(Friend friend) {
        if (friend.getStatus().equals(ACTIVE)) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
        friend.setStatus(ACTIVE);
    }
}
