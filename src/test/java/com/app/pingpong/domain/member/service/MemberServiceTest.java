package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.MemberResponse;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.util.UserFacade;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

import static com.app.pingpong.domain.member.entity.Authority.ROLE_USER;
import static com.app.pingpong.global.common.Status.ACTIVE;
import static com.app.pingpong.global.exception.StatusCode.SUCCESS_VALIDATE_NICKNAME;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Mock private UserFacade userFacade;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private S3Uploader s3Uploader;
    @Mock private ListOperations<String, Object> listOps;
    @InjectMocks private MemberService memberService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        memberService = new MemberService(memberRepository, friendRepository, memberTeamRepository, planRepository,
                redisTemplate, userFacade, passwordEncoder, s3Uploader);
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
        Member member1 = new Member(1L, "123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        Member member2 = new Member(2L, "123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
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
        Member member = new Member(1L, "123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);

        when(memberRepository.findByIdAndStatus(member.getId(), ACTIVE)).thenReturn(Optional.of(member));
        MemberResponse memberResponse = memberService.findById(member.getId());

        assertNotNull(memberResponse);
        assertEquals(member.getNickname(), memberResponse.getNickname());
        assertEquals(member.getId(), memberResponse.getUserId());
    }

    @Test
    @DisplayName("닉네임, 프로필 수정")
    public void updateMember() {
        // given
        Member member = new Member(1L, "123", "email", "nickname", "profileImage", ACTIVE, ROLE_USER);
        when(memberRepository.save(member)).thenReturn(member);
        Member save = memberRepository.save(member);

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

}