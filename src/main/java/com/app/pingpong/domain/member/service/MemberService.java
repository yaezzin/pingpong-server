package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.repository.FriendRepository;
import com.app.pingpong.domain.member.dto.request.MemberAchieveRequest;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.*;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.MemberRepository;

import com.app.pingpong.domain.member.repository.MemberTeamRepository;
import com.app.pingpong.domain.s3.S3Uploader;
import com.app.pingpong.domain.team.dto.response.TeamAchieveResponse;
import com.app.pingpong.domain.team.dto.response.TeamPlanResponse;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.domain.team.repository.TeamRepository;
import com.app.pingpong.global.common.BaseResponse;
import com.app.pingpong.global.common.Status;
import com.app.pingpong.global.exception.BaseException;
import com.app.pingpong.global.exception.StatusCode;
import com.app.pingpong.global.util.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.Status.*;
import static com.app.pingpong.global.exception.StatusCode.*;
import static com.app.pingpong.global.util.RegexUtil.isRegexNickname;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final PlanRepository planRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        Member member = request.toEntity(passwordEncoder);
        Member save = memberRepository.save(member);
        return MemberResponse.of(save);
    }

    @Transactional
    public BaseResponse<String> validateNickname(String nickname) {
        if (!isRegexNickname(nickname)) {
            throw new BaseException(INVALID_NICKNAME);
        }
        if (memberRepository.existsMemberByNicknameAndStatus(nickname)) {
            throw new BaseException(USER_NICKNAME_ALREADY_EXISTS);
        }
        return new BaseResponse(SUCCESS_VALIDATE_NICKNAME);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public MemberDetailResponse getMyPage(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        int friendCount = friendRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse getOppPage(Long id) {
        Member member = memberRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        int friendCount = friendRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    @Transactional(readOnly = true)
    public List<Friend> getMyFriends() {
        Member loginUser = userFacade.getCurrentUser();
        List<Friend> friends = friendRepository.findAllFriendsByUserId(loginUser.getId());

        List friendList = new ArrayList();
        for (Friend f : friends) {
            if (isMyFriendRequest(f, loginUser)) {
                addRespondentInfoToFriendList(f, friendList);
            }
            if (isOpponentFriendRequest(f, loginUser)) {
                addApplicantInfoToFriendList(f, friendList);
            }
        }
        return friendList;
    }

    @Transactional
    public StatusCode saveSearchLog(SearchLogRequest request) {
        Member member = memberRepository.findByIdAndStatus(request.getId(), ACTIVE).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        String loginUserId = "id" + userFacade.getCurrentUser().getId(); // 내 식별자
        String memberId = "id" + member.getId();
        listOps.leftPush(loginUserId, memberId);
        return SUCCESS;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getSearchLog() {
        String loginUserId = "id" + userFacade.getCurrentUser().getId(); // 내 식별자
        List<String> numList = extractNumAndAddToList(loginUserId);
        List<MemberResponse> responses = addMemberToListByExtractedNum(numList);
        return responses;
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<MemberTeamResponse> getMemberTeams() {
        Long loginUserId = userFacade.getCurrentUser().getId();
        List<MemberTeam> memberTeams = memberTeamRepository.findAllByMemberIdAndStatusOrderByParticipatedAtDesc(loginUserId, ACTIVE);
        List<MemberResponse> list = new ArrayList<>();
        List<MemberTeamResponse> teamList = new ArrayList<>();

        for (MemberTeam mt : memberTeams) {
            Team team = mt.getTeam();
            List<MemberTeam> teamMembers = memberTeamRepository.findAllByTeamId(team.getId());
            List<Member> members = teamMembers.stream().map(MemberTeam::getMember).collect(Collectors.toList());
            for (Member m : members) {
                list.add(MemberResponse.builder()
                        .userId(m.getId())
                        .nickname(m.getNickname())
                        .profileImage(m.getProfileImage())
                        .build()
                );
            }
            teamList.add(MemberTeamResponse.of(team, list));
        }
        return teamList;
    }

    @Transactional(readOnly = true)
    public List<MemberAchieveResponse> getMemberAchievementRate(MemberAchieveRequest request) {
        Long currentUserId = userFacade.getCurrentUser().getId();
        List<Plan> plans = planRepository.findAllByManagerIdAndStatusAndDateBetween(currentUserId, ACTIVE,request.getStartDate(), request.getEndDate());
        List<LocalDate> dateList = plans.stream().map(Plan::getDate).collect(Collectors.toList());

        int complete = 0;
        int incomplete = 0;
        double achievement = 0;
        List<MemberAchieveResponse> response = new ArrayList<>();
        for (LocalDate date :dateList) {
            List<Plan> plan = planRepository.findAllByManagerIdAndStatusAndDate(currentUserId, ACTIVE, date);
            List<Status> achieves = plan.stream().map(Plan::getAchievement).collect(Collectors.toList());
            for (Status achieve : achieves) {
                if (achieve == COMPLETE) {
                    complete += 1;
                } else {
                    incomplete += 1;
                }
            }
            achievement = ((double)(complete) / (double) (complete + incomplete) * 100.0);
            response.add(new MemberAchieveResponse(date, achievement));
        }
        return response;
    }

    private boolean isMyFriendRequest(Friend f, Member loginUser) {
        if (f.getApplicant().getId() == loginUser.getId()) {
            return true;
        }
        return false;
    }

    private boolean isOpponentFriendRequest(Friend f, Member loginUser) {
        if (f.getRespondent().getId() == loginUser.getId()) {
            return true;
        }
        return false;
    }

    private void addRespondentInfoToFriendList(Friend f, List friendList) {
        Member respondent = f.getRespondent();
        friendList.add(new MemberResponse(respondent.getId(), respondent.getNickname(), respondent.getProfileImage()));
    }

    private void addApplicantInfoToFriendList(Friend f, List friendList) {
        Member applicant = f.getApplicant();
        friendList.add(new MemberResponse(applicant.getId(), applicant.getNickname(), applicant.getProfileImage()));
    }

    /* String 타입인 Redis의 key값 (ex."id1")에서 숫자만 추출한다. */
    private List<String> extractNumAndAddToList(String loginUserId) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        List<String> list = new ArrayList<>();
        for (Object o : listOps.range(loginUserId, 0, -1)) {
            String userId = o.toString().substring(2,3); //숫자값만 추출
            if (!list.contains(userId) && list.size() <= 10) {
                list.add(userId);
            }
        }
        return list;
    }

    /* 추출한 숫자값으로 Member 정보를 가져온다. */
    private List<MemberResponse> addMemberToListByExtractedNum(List<String> numList) {
        List<MemberResponse> memberList = new ArrayList<>();
        for (String num : numList) {
            Long userId = Long.parseLong(num);
            Member member = memberRepository.findById(userId).orElseThrow(() -> new BaseException(USER_NOT_FOUND));
            memberList.add(MemberResponse.of(member));
        }
        return memberList;
    }

    public List<MemberPlanDetailResponse> getMemberCalendarByDate(LocalDate date) {
        Long currentMemberId = userFacade.getCurrentUser().getId();
        List<MemberTeam> memberTeams = memberTeamRepository.findAllByMemberIdAndStatusOrderByParticipatedAtDesc(currentMemberId, ACTIVE);
        List<Team> teams = memberTeams.stream().map(MemberTeam::getTeam).collect(Collectors.toList());

        List<MemberPlanDetailResponse> response = new ArrayList<>();
        for (Team team : teams) {
            // 팀마다 플랜을 가져옴
            List<Plan> plans = planRepository.findAllByTeamIdAndManagerIdAndStatusAndDate(team.getId(), currentMemberId, ACTIVE, date);

            List<TeamPlanResponse> planList = new ArrayList<>();
            for (Plan plan : plans) {
                planList.add(TeamPlanResponse.builder()
                        .planId(plan.getId())
                        .managerId(plan.getManager().getId())
                        .title(plan.getTitle())
                        .date(plan.getDate())
                        .status(plan.getStatus())
                        .achievement(plan.getAchievement())
                        .build()
                );
            }
            response.add(MemberPlanDetailResponse.of(team, planList));
        }
        return response;
    }
}
