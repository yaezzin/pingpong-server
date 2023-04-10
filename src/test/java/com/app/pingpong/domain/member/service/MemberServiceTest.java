package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.friend.service.FriendService;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberDetailResponse;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.dto.response.MemberSearchResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.util.MemberFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.app.pingpong.domain.member.entity.Authority.ROLE_USER;
import static com.app.pingpong.global.common.Status.ACTIVE;
import static com.app.pingpong.global.exception.StatusCode.SUCCESS_DELETE_MEMBER;
import static com.app.pingpong.global.exception.StatusCode.SUCCESS_VALIDATE_NICKNAME;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @PersistenceContext EntityManager em;

    @Mock private MemberRepository memberRepository;
    @Mock private FriendRepository friendRepository;
    @Mock private MemberTeamRepository memberTeamRepository;
    @Mock private PlanRepository planRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private MemberFacade memberFacade;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private S3Uploader s3Uploader;
    @InjectMocks private MemberService memberService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        memberService = new MemberService(memberRepository, friendRepository, memberTeamRepository, planRepository,
                redisTemplate, memberFacade, passwordEncoder, s3Uploader);
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입")
    public void signup() {
        // given
        SignUpRequest request = new SignUpRequest("1234", "test@email.com", "nickname", "profileImage");
        Member member = request.toEntity(passwordEncoder);
        member.setId(1L);
        given(memberRepository.save(any(Member.class))).willReturn(member);

        // when
        MemberResponse response = memberService.signup(request);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
        assertThat(response.getNickname()).isEqualTo(request.getNickname());
        assertThat(response.getProfileImage()).isEqualTo(request.getProfileImage());
    }

    @Test
    @DisplayName("회원가입 시 닉네임 유효성 테스트")
    public void validateNickname() {
        Member member1 = new Member("123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        Member member2 = new Member("123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 유효한 닉네임인 경우 SUCCESS_VALIDATE_NICKNAME, 200을 리턴함
        String validNickname = member1.getNickname();
        BaseResponse<String> response = memberService.validateNickname(validNickname);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals(SUCCESS_VALIDATE_NICKNAME.getMessage(), response.getMessage());

        // 이미 존재하는 닉네임인 경우 BaseException을 Throw
        String duplicateNickname = member2.getNickname();
        given(memberRepository.existsMemberByNicknameAndStatus(duplicateNickname)).willReturn(true);
        assertThrows(BaseException.class, () -> memberService.validateNickname(duplicateNickname));

        // 유효하지 않은 닉네임인 경우 BaseException을 Throw
        String invalidNickname = "@!#dsaflah11dfadfa";
        assertThrows(BaseException.class, () -> memberService.validateNickname(invalidNickname));
    }

    @Test
    @DisplayName("id로 멤버 조회")
    public void findById() {
        // given
        Member member = new Member("123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);

        when(memberRepository.findByIdAndStatus(member.getId(), ACTIVE)).thenReturn(Optional.of(member));
        MemberResponse memberResponse = memberService.findById(member.getId());

        assertNotNull(memberResponse);
        assertEquals(member.getNickname(), memberResponse.getNickname());
        assertEquals(member.getId(), memberResponse.getMemberId());
    }

    @Test
    @DisplayName("닉네임, 프로필 수정")
    public void update() {
        // given
        Member save = createMember();
        UpdateRequest request = new UpdateRequest("new", "newProfileImage");
        when(memberRepository.findByIdAndStatus(save.getId(), ACTIVE)).thenReturn(Optional.of(save));
        doNothing().when(s3Uploader).deleteFile(anyString());

        // when
        MemberResponse memberResponse = memberService.update(save.getId(), request);

        // then
        assertNotNull(memberResponse);
        assertEquals(request.getNickname(), memberResponse.getNickname());
        assertEquals(request.getProfileImage(), memberResponse.getProfileImage());
    }

    @Test
    @DisplayName("회원 탈퇴")
    public void delete() {
        // given
        Member member = createMember();
        when(memberRepository.findByIdAndStatus(member.getId(), ACTIVE)).thenReturn(Optional.of(member));

        // when
        BaseResponse<String> response = memberService.delete(member.getId());

        // then
        assertEquals(SUCCESS_DELETE_MEMBER.getCode(), response.getCode());
    }

    @Test
    @DisplayName("나의 페이지 조회")
    public void getMyPage() {
        // given
        Member member = createMember();
        when(memberRepository.findByIdAndStatus(member.getId(), ACTIVE)).thenReturn(Optional.of(member));
        given(friendRepository.findFriendCount(eq(member.getId()))).willReturn(10);

        // when
        MemberDetailResponse response = memberService.getMyPage(member.getId());

        // then
        assertNotNull(response);
        assertEquals(response.getUserId(), member.getId());
        assertThat(response.getFriendCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("상대방 페이지 조회")
    public void getOppPage() {

    }

    @Test
    @DisplayName("나의 친구 조회")
    public void getMyFriends() {
        // given
        Friend friend1 = Friend.builder()
                .applicant(Member.builder().id(2L).build())
                .respondent(Member.builder().id(1L).build())
                .status(ACTIVE)
                .build();

        Friend friend2 = Friend.builder()
                .applicant(Member.builder().id(1L).build())
                .respondent(Member.builder().id(3L).build())
                .status(ACTIVE)
                .build();

        when(friendRepository.findAllFriendsByMemberId(1L)).thenReturn(Arrays.asList(friend1, friend2));

        // when
        doNothing().when(memberFacade).getCurrentMember();
        List<Friend> result = memberService.getMyFriends(memberFacade.getCurrentMember().getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getApplicant().getId()).isEqualTo(2L);
        assertThat(result.get(0).getRespondent().getId()).isEqualTo(1L);
        assertThat(result.get(0).getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    @DisplayName("검색기록 저장")
    public void saveSearchLog() {


    }

    @Test
    @DisplayName("검색기록 조회")
    public void getSearchLog() {

    }

    @Test
    @DisplayName("닉네임으로 유저조회")
    public void findByNickname() {
        // given
        Member currentMember = createMember("유저1");
        Member searchMember1 = createMember("유저2");
        Member searchMember2 = createMember("유저3");
        String searchNickname = "유저";


        List<Member> findMembers = Arrays.asList(searchMember1, searchMember2, currentMember);
        when(memberRepository.findByStatusAndNicknameContains(ACTIVE, searchNickname)).thenReturn(Optional.of(findMembers));
        when(memberFacade.getCurrentMember()).thenReturn(currentMember);

        // when
        List<MemberSearchResponse> result = memberService.findByNickname(searchNickname);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(MemberSearchResponse::getNickname)
                .containsExactlyInAnyOrder(currentMember.getNickname(), searchMember1.getNickname(), searchMember2.getNickname());
    }

    @Test
    @DisplayName("유저가 속한 팀 전체 조")
    public void getMemberTeams() {

    }

    @Test
    @DisplayName("유저의 성취율 조회")
    public void getMemberAchievementRate() {

    }

    @Test
    @DisplayName("오늘 할일 조회 by All teams")
    public void getMemberCalendarByDate() {

    }

    private Member createMember() {
        Member member = new Member("123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        when(memberRepository.save(member)).thenReturn(member);
        return memberRepository.save(member);
    }

    private Member createMember(String nickname) {
        Member member = new Member("123", "email", nickname, "profileImage", ACTIVE, ROLE_USER);
        when(memberRepository.save(member)).thenReturn(member);
        return memberRepository.save(member);
    }
}