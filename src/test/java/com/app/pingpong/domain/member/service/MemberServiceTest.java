package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendFactory;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.friend.service.FriendService;
import com.app.pingpong.domain.member.dto.request.MemberAchieveRequest;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.*;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.app.pingpong.domain.member.entity.Authority.ROLE_USER;
import static com.app.pingpong.global.common.Status.ACTIVE;

import static com.app.pingpong.global.common.Status.COMPLETE;
import static com.app.pingpong.global.exception.StatusCode.*;
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
    @Mock private FriendFactory friendFactory;
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
        memberService = new MemberService(memberRepository, friendFactory, memberTeamRepository, planRepository,
                redisTemplate, memberFacade, passwordEncoder, s3Uploader);
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입")
    public void signup() {
        // given
        SignUpRequest request = new SignUpRequest("1234", "test@email.com", "nickname", "profileImage");
        Member member = request.toEntity(passwordEncoder);
        given(memberRepository.save(any(Member.class))).willReturn(member);
        em.persist(member);

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
        given(friendFactory.findFriendCount(eq(member.getId()))).willReturn(10);

        // when
        MemberDetailResponse response = memberService.getMyPage(member.getId());

        // then
        assertNotNull(response);
        assertEquals(response.getMemberId(), member.getId());
        assertThat(response.getFriendCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("상대방 페이지 조회")
    public void getOppPage() {
        Member member = createMember();
        when(memberRepository.findByIdAndStatus(member.getId(), ACTIVE)).thenReturn(Optional.of(member));
        given(friendFactory.findFriendCount(eq(member.getId()))).willReturn(10);

        MemberDetailResponse response = memberService.getOppPage(member.getId());
        assertNotNull(response);
        assertEquals(response.getMemberId(), member.getId());
        assertThat(response.getFriendCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("나의 친구 조회")
    public void getMyFriends() {

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
    @DisplayName("유저가 속한 팀 전체 조회")
    public void getMemberTeams() {
        // given
        Member currentMember = createMember();

        Team team1 = createTeam("팀1", currentMember);
        Team team2 = createTeam("팀2", currentMember);

        MemberTeam memberTeam1 = setTeamToMember(currentMember, team1);
        MemberTeam memberTeam2 = setTeamToMember(currentMember, team2);

        List<MemberTeam> memberTeams = Arrays.asList(memberTeam1, memberTeam2);
        List<Member> members = Arrays.asList(currentMember);

        when(memberFacade.getCurrentMember()).thenReturn(currentMember);
        when(memberTeamRepository.findAllByMemberIdAndStatusOrderByParticipatedAtDesc(currentMember.getId(), ACTIVE)).thenReturn(memberTeams);
        when(memberTeamRepository.findAllByTeamIdAndStatus(team1.getId(), ACTIVE)).thenReturn(memberTeams);

        // when
        List<MemberTeamResponse> teamList = memberService.getMemberTeams();

        // then
        assertNotNull(teamList);
        assertThat(teamList.size()).isEqualTo(2);
        assertThat(teamList.get(0).getTeamName()).isEqualTo("팀1");
        assertThat(teamList.get(1).getTeamName()).isEqualTo("팀2");
    }

    @Test
    @DisplayName("유저의 성취율 조회")
    public void getMemberAchievementRate() {
        // given
        Member member = createMember("Member1");
        Team team = createTeam("Team1", member);
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 30);

        Plan plan1 = createPlan("Plan1", startDate, member, team);
        plan1.setAchievement(COMPLETE);

        Plan plan2 = createPlan("Plan1", startDate, member, team);
        Plan plan3 = createPlan("Plan1", startDate, member, team);
        Plan plan4 = createPlan("Plan1", startDate, member, team);
        Plan plan5 = createPlan("Plan1", startDate, member, team);

        List<Plan> plans = Arrays.asList(plan1, plan2, plan3, plan4, plan5);
        planRepository.saveAll(plans);

        when(memberFacade.getCurrentMember()).thenReturn(member);
        when(planRepository.findAllByManagerIdAndStatusAndDateBetween(member.getId(), ACTIVE, startDate, endDate)).thenReturn(plans);
        when(planRepository.findAllByManagerIdAndStatusAndDate(member.getId(), ACTIVE, startDate)).thenReturn(plans);

        // when
        List<MemberAchieveResponse> response = memberService.getMemberAchievementRate(new MemberAchieveRequest(startDate, endDate));

        // then
        assertNotNull(response);
        assertEquals(response.get(0).getAchievement(), 20);
    }

    @Test
    @DisplayName("유저가 속한 팀에서 오늘 할일 조회 ")
    public void getMemberCalendarByDate() {
        // given
        Member member = createMember();

        Team team1 = createTeam("Team1", member);
        Team team2 = createTeam("Team2", member);

        MemberTeam memberTeam1 = setTeamToMember(member, team1);
        MemberTeam memberTeam2 = setTeamToMember(member, team2);
        List<MemberTeam> memberTeams = Arrays.asList(memberTeam1, memberTeam2);

        Plan plan1 = createPlan("title1", LocalDate.now(), member, team1);
        Plan plan2 = createPlan("title2", LocalDate.now(), member, team1);
        Plan plan3 = createPlan("title2", LocalDate.now(), member, team2);
        List<Plan> plansFroTeam1 = Arrays.asList(plan1, plan2);
        List<Plan> plansFroTeam2 = Arrays.asList(plan3);


        when(memberFacade.getCurrentMember()).thenReturn(member);
        when(memberTeamRepository.findAllByMemberIdAndStatusOrderByParticipatedAtDesc(member.getId(), ACTIVE)).thenReturn(memberTeams);
        when(planRepository.findAllByTeamIdAndManagerIdAndStatusAndDate(team1.getId(), member.getId(), ACTIVE, LocalDate.now())).thenReturn(plansFroTeam1);
        when(planRepository.findAllByTeamIdAndManagerIdAndStatusAndDate(team2.getId(), member.getId(), ACTIVE, LocalDate.now())).thenReturn(plansFroTeam2);

        // when
        List<MemberPlanDetailResponse> response = memberService.getMemberCalendarByDate(LocalDate.now());

        // then
        assertEquals(response.get(0).getPlanList().size(), 1);
        assertEquals(response.get(1).getPlanList().size(), 1);
    }

    private Member createMember() {
        Member member = new Member("123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        when(memberRepository.save(member)).thenReturn(member);
        return memberRepository.save(member);
    }

    private Member createMember(String nickname) {
        Member member = new Member("1234", "email", nickname, "profileImage", ACTIVE, ROLE_USER);
        when(memberRepository.save(member)).thenReturn(member);
        return memberRepository.save(member);
    }

    private Team createTeam(String name, Member member) {
        Team team = new Team(name);
        team.setHost(member);
        return team;
    }

    private MemberTeam setTeamToMember(Member member, Team team) {
        MemberTeam memberTeam1 = new MemberTeam();
        memberTeam1.setMember(member);
        memberTeam1.setTeam(team);
        return memberTeam1;
    }

    private Plan createPlan(String title, LocalDate startDate, Member member, Team team) {
        Plan plan = new Plan(title, startDate);
        plan.setManager(member);
        plan.setTeam(team);
        return plan;
    }
}
