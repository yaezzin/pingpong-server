package com.app.pingpong.domain.friend.service;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.dto.response.FriendResponse;
import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.common.Status;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.MemberFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static com.app.pingpong.domain.member.entity.Authority.ROLE_USER;
import static com.app.pingpong.global.common.Status.*;
import static com.app.pingpong.global.exception.StatusCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FriendServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private FriendRepository friendRepository;
    @Mock private FriendFactory friendFactory;
    @Mock private MemberFacade memberFacade;
    @InjectMocks private FriendService friendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("친구 신청")
    @Transactional
    void add() {
        // given
        Member applicant = createMember(1L);
        Member respondent = createMember(2L);
        FriendRequest request = new FriendRequest(applicant.getId(), respondent.getId());

        when(friendRepository.save(any(Friend.class))).thenAnswer(invocation -> {
            Friend savedFriend = invocation.getArgument(0);
            savedFriend.setId(1L);
            return savedFriend;
        });

        when(memberRepository.findByIdAndStatus(request.getApplicantId(), ACTIVE)).thenReturn(Optional.of(applicant));
        when(memberRepository.findByIdAndStatus(request.getRespondentId(), ACTIVE)).thenReturn(Optional.of(respondent));
        when(friendFactory.existsRequestToRespondent(applicant.getId(), respondent.getId(), WAIT)).thenReturn(false);
        when(friendFactory.existsRequestToRespondent(respondent.getId(), applicant.getId(), WAIT)).thenReturn(false);
        when(friendFactory.existsRequestToRespondent(applicant.getId(), respondent.getId(), Status.ACTIVE)).thenReturn(false);

        // when
        FriendResponse friendResponse = friendService.add(request);

        //then
        assertNotNull(friendResponse);
        assertEquals(friendResponse.getApplicantId(), applicant.getId());
        assertEquals(friendResponse.getRespondentId(), respondent.getId());
        verify(memberRepository, times(2)).findByIdAndStatus(anyLong(), eq(Status.ACTIVE));
        verify(friendFactory, times(2)).existsRequestToRespondent(anyLong(), anyLong(), eq(WAIT));
        verify(friendFactory, times(1)).existsRequestToRespondent(anyLong(), anyLong(), eq(Status.ACTIVE));
    }

    @Test
    @DisplayName("친구 신청 수락")
    void accept_success() {
        // given
        Member currentMember = createMember(1L);
        Member opponent = createMember(2L);
        Friend friendRequest = new Friend(currentMember, opponent, WAIT, new Date());

        when(memberFacade.getCurrentMember()).thenReturn(currentMember);
        when(friendFactory.findWaitRequestBy(opponent.getId(), currentMember.getId())).thenReturn(Optional.of(friendRequest));

        // when
        StatusCode statusCode = friendService.accept(opponent.getId());

        // then
        assertEquals(SUCCESS_ACCEPT_FRIEND, statusCode);
        assertEquals(ACTIVE, friendRequest.getStatus());
        verify(memberFacade, times(1)).getCurrentMember();
        verify(friendFactory, times(1)).findWaitRequestBy(opponent.getId(), currentMember.getId());
    }

    @Test
    @DisplayName("친구 신청 수락 시 이미 친구인지 확인한다.")
    void accept_already_on_friend() {
        // given
        Member currentMember = createMember(1L);
        Member opponent = createMember(1L);
        Friend friendRequest = new Friend(currentMember, opponent, ACTIVE, new Date());

        when(memberFacade.getCurrentMember()).thenReturn(currentMember);
        when(friendFactory.findWaitRequestBy(opponent.getId(), currentMember.getId())).thenReturn(Optional.of(friendRequest));

        // when, then
        assertThrows(BaseException.class, () -> friendService.accept(opponent.getId()));
        assertEquals(ACTIVE, friendRequest.getStatus());
        verify(memberFacade, times(1)).getCurrentMember();
        verify(friendFactory, times(1)).findWaitRequestBy(opponent.getId(), currentMember.getId());
    }

    @Test
    @DisplayName("친구 신청 거절")
    void refuse() {
        // given
        Member currentMember = createMember(1L);
        Member opponent = createMember(2L);
        Friend friendRequest = new Friend(currentMember, opponent, WAIT, new Date());

        when(memberFacade.getCurrentMember()).thenReturn(currentMember);
        when(friendFactory.findWaitRequestBy(opponent.getId(), currentMember.getId())).thenReturn(Optional.of(friendRequest));

        // when
        StatusCode statusCode = friendService.refuse(opponent.getId());

        // then
        assertEquals(SUCCESS_REFUSE_FRIEND, statusCode);
        assertEquals(DELETE, friendRequest.getStatus());
        verify(memberFacade, times(1)).getCurrentMember();
        verify(friendFactory, times(1)).findWaitRequestBy(opponent.getId(), currentMember.getId());
    }

    private Member createMember(Long Id) {
        Member member = new Member("1234", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        member.setId(Id);
        return member;
    }
}
