package com.app.pingpong.domain.friend.service;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.MemberFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.app.pingpong.global.common.Status.*;
import static com.app.pingpong.global.exception.StatusCode.*;

@RequiredArgsConstructor
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final FriendFactory friendFactory;
    private final MemberFacade memberFacade;

    @Transactional
    public FriendResponse apply(FriendRequest request) {
        Member applicant = memberRepository.findByIdAndStatus(request.getApplicantId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        Member respondent = memberRepository.findByIdAndStatus(request.getRespondentId(), ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        checkFriendRequest(applicant, respondent);
        return FriendResponse.of(friendRepository.save(request.toEntity(applicant.getId(), respondent.getId())));
    }

    @Transactional
    public StatusCode accept(Long opponentId) {
        Friend request = getWaitingFriendRequest(opponentId);
        checkAndSetStatusActive(request);
        return SUCCESS_ACCEPT_FRIEND;
    }

    @Transactional
    public StatusCode refuse(Long opponentId) {
        Friend request = getWaitingFriendRequest(opponentId);
        checkAndSetStatusDelete(request);
        return SUCCESS_REFUSE_FRIEND;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMyFriends() {
        Long currentMember = memberFacade.getCurrentMember().getId();
        List<Member> friends = friendRepository.findAllFriendsByMemberId(currentMember);

        List<MemberResponse> friendList = new ArrayList<>();
        for (Member m : friends) {
            friendList.add(MemberResponse.of(m));
        }
        return friendList;
    }

    private void checkFriendRequest(Member applicant, Member respondent) {
        /* 내가 상대방에게 보낸 친구 신청이 있는지 확인 -> WAIT */
        if (friendFactory.existsRequestToRespondent(applicant.getId(), respondent.getId(), WAIT)) {
            throw new BaseException(USER_ALREADY_FRIEND_REQUEST);
        }
        /* 상대방이 나한테 보낸 친구신청이 있는지 확인 */
        if (friendFactory.existsRequestToRespondent(respondent.getId(), applicant.getId(), WAIT)) {
            throw new BaseException(USER_ALREADY_GET_FRIEND_REQUEST);
        }
        /* 이미 친구를 수락하였는지 확인 -> */
        if (friendFactory.existsRequestToRespondent(applicant.getId(), respondent.getId(), ACTIVE)) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
    }

    private Friend getWaitingFriendRequest(Long opponentId) {
        Member currentMember = memberFacade.getCurrentMember();
        Friend friend = friendFactory.findWaitRequestBy(opponentId, currentMember.getId()).orElseThrow(() -> new BaseException(FRIEND_NOT_FOUND));
        return friend;
    }

    private void checkAndSetStatusActive(Friend friend) {
        if (friend.getStatus().equals(ACTIVE)) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
        friend.setStatus(ACTIVE);
    }

    private void checkAndSetStatusDelete(Friend friend) {
        if (friend.getStatus().equals(ACTIVE)) {
            throw new BaseException(ALREADY_ON_FRIEND);
        }
        friend.setStatus(DELETE);
    }

    /*
    private boolean isMyFriendRequest(Friend f, Long memberId) {
        if (f.getApplicant() == memberId) {
            return true;
        }
        return false;
    }

    private boolean isOpponentFriendRequest(Friend f, Long memberId) {
        if (f.getRespondent() == memberId) {
            return true;
        }
        return false;
    }

    private void addRespondentInfoToFriendList(Friend f, List friendList) {
        Member member = memberRepository.findById(f.getRespondent()).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        friendList.add(MemberResponse.of(member));
    }

    private void addApplicantInfoToFriendList(Friend f, List friendList) {
        Member applicant = f.getApplicant();
        friendList.add(MemberResponse.of(applicant));
    } */
}

