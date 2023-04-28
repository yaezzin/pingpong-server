package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberDetailResponse;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.member.repository.MemberSearchRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.response.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.app.pingpong.factory.MemberFactory.createMember;
import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.DateUtil.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberSearchRepository memberSearchRepository;

    @Autowired
    FriendFactory friendFactory;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입")
    public void signup() {
        // given
        SignUpRequest request = new SignUpRequest("1234", "test@email.com", "nickname", "profileImage");

        // when
        MemberResponse response = memberService.signup(request);
        Member save = memberRepository.findById(response.getMemberId()).orElseThrow();

        // then
        assertThat(response).isNotNull();
        assertThat(save).isNotNull();
        assertThat(response.getNickname()).isEqualTo(request.getNickname());
        assertThat(save.getNickname()).isEqualTo(response.getNickname());
    }

    @Test
    @DisplayName("닉네임 유효성 검사 - 성공")
    public void validateNickname() {
        // given
        Member member = createMember();

        // when
        BaseResponse<String> response = memberService.validateNickname(member.getNickname());

        // then
        assertEquals(response.getMessage(), SUCCESS_VALIDATE_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("닉네임 유효성 검사 - 중복되는 닉네임")
    public void existsNickname() {
        // given
        Member member = memberRepository.save(createMember());

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> {
            memberService.validateNickname(member.getNickname());
        });
        assertEquals(exception.getStatus(), USER_NICKNAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("닉네임 유효성 검사 - 유효하지 않은 닉네임")
    public void invalidNickname() {
        // given
        String invalidNickname = "invalidNicknameBecauseItsTooLong";
        Member member = createMember(invalidNickname);

        // when, then
        BaseException exception = assertThrows(BaseException.class, () -> {
            memberService.validateNickname(member.getNickname());
        });
        assertEquals(exception.getStatus(), INVALID_NICKNAME);
    }

    @Test
    @DisplayName("Id로 유저 조회")
    public void findById() {
        // given
        Member member = memberRepository.save(createMember());

        // when
        MemberResponse response = memberService.findById(member.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원 수정 테스트")
    public void update() {
        // given
        Member member = memberRepository.save(createMember());
        UpdateRequest request = new UpdateRequest("update", "profile.jpg");

        // when
        MemberResponse response = memberService.update(member.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo(request.getNickname());
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    public void delete() {
        // given
        Member member = memberRepository.save(createMember());

        // when
        BaseResponse<String> response = memberService.delete(member.getId());

        // then
        assertThat(response.getMessage()).isEqualTo(SUCCESS_DELETE_MEMBER.getMessage());
    }

    @Test
    @DisplayName("마이페이지 조회")
    public void getMyPage() {
        // given
        Member loginMember = memberRepository.save(createMember());
        Member opponentMember = memberRepository.save(createMember());
        Friend friend = new Friend(loginMember.getId(), opponentMember.getId(), ACTIVE, now());
        int friendCount = friendFactory.findFriendCount(loginMember.getId());

        // when
        MemberDetailResponse response = memberService.getMyPage(loginMember.getId());

        // then
        assertThat(response.getMemberId()).isEqualTo(loginMember.getId());
        assertThat(response.getFriendCount()).isEqualTo(friendCount);
    }

    @Test
    @DisplayName("상대방 페이지 조회")
    public void getOppPage() {
        // given
        Member loginMember = memberRepository.save(createMember());
        Member opponentMember = memberRepository.save(createMember());
        Friend friend = new Friend(loginMember.getId(), opponentMember.getId(), ACTIVE, now());
        int friendCount = friendFactory.findFriendCount(opponentMember.getId());

        // when
        MemberDetailResponse response = memberService.getMyPage(opponentMember.getId());

        // then
        assertThat(response.getMemberId()).isEqualTo(opponentMember.getId());
        assertThat(response.getFriendCount()).isEqualTo(friendCount);
    }

    @Test
    @DisplayName("닉네임으로 회원 검색")
    public void testFindByNickname() {
        // given
        for (int i = 1; i < 30; i++) {
            memberRepository.save(createMember("test" + i + "@test.com", "nickname" + i));
        }

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test1@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);


        // when
        List<MemberSearchResponse> response = memberService.findByNickname("nickname", 10L);

        // then
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.get(0).getMemberId()).isEqualTo(11L);
        System.out.println(response.get(0).getMemberId());
    }

    @Test
    @DisplayName("검색기록 저장")
    public void saveSearchLog() {
        //given
        Member loginMember = memberRepository.save(createMember());
        Member searchMember = memberRepository.save(createMember());

        SearchLogRequest request = new SearchLogRequest(searchMember.getId());


        //when
        StatusCode response = memberService.saveSearchLog(request, loginMember.getId());

        //then
        assertThat(response).isEqualTo(SUCCESS_SAVE_SEARCH_LOG);
    }

    @Test
    @DisplayName("검색기록 저장 실패 - 현재 로그인한 유저가 아닌 경우")
    public void saveSearchLogFail() {
        //given
        Member searchMember = memberRepository.save(createMember());

        SearchLogRequest request = new SearchLogRequest(searchMember.getId());

        // when, then : 원래는 로그인한 유저의 pk를 전달해야하나, 실패 테스트를 위해 searchMember pk 전달
        assertThrows(BaseException.class, () -> {
            memberService.saveSearchLog(request, searchMember.getId());
        });
    }
}