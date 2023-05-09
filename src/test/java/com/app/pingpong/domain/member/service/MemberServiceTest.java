package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendQueryRepository;
import com.app.pingpong.domain.image.S3Uploader;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberDetailResponse;
import com.app.pingpong.domain.member.dto.response.MemberKeywordResponse;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberSearchRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.util.MemberFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.app.pingpong.factory.FriendFactory.createMultipleFriendsByCount;
import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.factory.MemberFactory.createMultipleMemberByCount;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    MemberService memberService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberSearchRepository memberSearchRepository;

    @Mock
    FriendQueryRepository friendQueryRepository;

    @Mock
    MemberFacade memberFacade;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    S3Uploader s3Uploader;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

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
    public void validateNicknameExceptionByInvalidNickname() {
        // given
        Member member = createMember("nicknameIsInvalid");

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.validateNickname(member.getNickname()));
        assertThat(exception.getStatus()).isEqualTo(INVALID_NICKNAME);
    }

    @Test
    public void findById() {
        // given
        Member member = createMember();
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.findById(member.getId());

        // then
        assertThat(response.getMemberId()).isEqualTo(member.getId());
    }

    @Test
    public void findByIdExceptionByMemberNotFound() {
        // given
        Long memberId = 1L;
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.findById(memberId));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void update() {
        // given
        Member member = createMember();
        UpdateRequest request = new UpdateRequest("update", "update");
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.update(member.getId(), request);

        // then
        verify(s3Uploader, times(1)).deleteFile(any());
        assertThat(response.getMemberId()).isEqualTo(member.getId());
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage()).isEqualTo(member.getProfileImage());
    }

    @Test
    public void updateExceptionByInvalidNickname() {
        // given
        Member member = createMember();
        UpdateRequest request = new UpdateRequest("update123Nickname!!!", "updateProfile");
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.update(member.getId(), request));
        assertThat(exception.getStatus()).isEqualTo(INVALID_NICKNAME);
    }

    @Test
    public void delete() {
        // given
        Member member = createMember();
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));

        // when
        StatusCode code = memberService.delete(member.getId());

        // then
        assertThat(code).isEqualTo(SUCCESS_DELETE_MEMBER);
        assertThat(member.getStatus()).isEqualTo(DELETE);
    }

    @Test
    public void deleteExceptionMemberNotFound() {
        // given
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.delete(1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void getMyPage() {
        // given
        Member member = createMember();
        List<Friend> friends = createMultipleFriendsByCount(member.getId(), 10);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(member));
        given(friendQueryRepository.findFriendCount(any())).willReturn(friends.size());

        // when
        MemberDetailResponse response = memberService.getMyPage(member.getId());

        // then
        assertThat(member.getId()).isEqualTo(response.getMemberId());
        assertThat(friends.size()).isEqualTo(response.getFriendCount());
    }

    @Test
    public void getMyPageExceptionByMemberNotFound() {
        // given
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.getMyPage(1L));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void getOppPage() {
        // given
        Member opponent = createMember();
        List<Friend> friends = createMultipleFriendsByCount(opponent.getId(), 10);

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(opponent));
        given(friendQueryRepository.findFriendCount(any())).willReturn(friends.size());

        // when
        MemberDetailResponse response = memberService.getOppPage(opponent.getId());

        // then
        assertThat(opponent.getId()).isEqualTo(response.getMemberId());
        assertThat(friends.size()).isEqualTo(response.getFriendCount());
    }

    @Test
    public void getOppPageExceptionByMemberNotFound() {
        // given
        Member opponent = createMember();
        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.getOppPage(opponent.getId()));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void findByNickname() {
        // given
        Member member = createMember();
        List<Member> members = createMultipleMemberByCount(100);
        List<Member> offsetMembers = members.subList(10, 20); // 10
        ListOperations<String, Object> listOps = mock(ListOperations.class);

        given(memberSearchRepository.findByNicknameContainsWithNoOffset(any(), any(), any(), anyInt()))
                .willReturn(offsetMembers);
        given(memberFacade.getCurrentMember()).willReturn(member);
        given(friendQueryRepository.isFriend(any(), any())).willReturn(true);
        given(redisTemplate.opsForList()).willReturn(listOps);
        given(listOps.leftPush(any(), any())).willReturn(1L);

        // when
        List<MemberSearchResponse> response = memberService.findByNickname("nickname", member.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(offsetMembers.size());
        for (int i = 0; i < offsetMembers.size(); i++) {
            assertThat(response.get(i).getMemberId()).isEqualTo(offsetMembers.get(i).getId());
        }
    }

    @Test
    public void saveSearchLog() {
        // given
        Member member = createMember();
        Member opponent = createMember();
        SearchLogRequest request = new SearchLogRequest(opponent.getId());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.of(opponent));
        ListOperations<String, Object> listOps = mock(ListOperations.class);
        given(redisTemplate.opsForList()).willReturn(listOps);
        given(listOps.leftPush(any(), any())).willReturn(1L);

        // when
        StatusCode code = memberService.saveSearchLog(request, member.getId());

        // then
        assertThat(code).isEqualTo(SUCCESS_SAVE_SEARCH_LOG);
    }

    @Test
    public void saveSearchLogExceptionByMemberNotFound() {
        // given
        Member opponent = createMember();
        SearchLogRequest request = new SearchLogRequest(opponent.getId());

        given(memberRepository.findByIdAndStatus(any(), any())).willReturn(Optional.empty());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> memberService.saveSearchLog(request, opponent.getId()));
        assertThat(exception.getStatus()).isEqualTo(MEMBER_NOT_FOUND);
    }

    @Test
    public void getSearchLog() {
        // given
        Member member = createMember();
        List<Object> redisValues = new ArrayList<>();
        redisValues.add(new MemberResponse(1L, "nickname", "profileImage"));
        redisValues.add(new MemberKeywordResponse("keyword"));

        ListOperations<String, Object> listOps = mock(ListOperations.class);
        given(redisTemplate.opsForList()).willReturn(listOps);
        given(listOps.range(any(), anyLong(), anyLong())).willReturn(redisValues);

        // when
        List<Object> response = memberService.getSearchLog(member.getId());

        // then
        verify(redisTemplate).opsForList();
        assertThat(response.size()).isEqualTo(redisValues.size());
    }

    @Test
    public void

}