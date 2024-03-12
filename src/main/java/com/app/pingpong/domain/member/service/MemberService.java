package com.app.pingpong.domain.member.service;

import com.app.pingpong.domain.friend.repository.FriendQueryRepository;
import com.app.pingpong.domain.image.S3Uploader;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.dto.response.*;
import com.app.pingpong.domain.member.entity.Badge;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberBadge;
import com.app.pingpong.domain.member.entity.MemberTeam;
import com.app.pingpong.domain.member.repository.*;
import com.app.pingpong.domain.team.dto.response.TeamPlanResponse;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.entity.Team;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.status.Status;
import com.app.pingpong.global.common.util.MemberFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static com.app.pingpong.global.common.status.Status.*;
import static com.app.pingpong.global.common.util.RegexUtil.isRegexNickname;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberSearchRepository memberSearchRepository;
    private final FriendQueryRepository friendQueryRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final PlanRepository planRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberFacade memberFacade;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        Member member = request.toEntity(passwordEncoder);
        return MemberResponse.of(memberRepository.save(member));
    }

    @Transactional
    public StatusCode validateNickname(String nickname) {
        if (!isRegexNickname(nickname)) {
            throw new BaseException(INVALID_NICKNAME);
        }
        if (memberRepository.existsMemberByNicknameAndStatus(nickname)) {
            throw new BaseException(MEMBER_NICKNAME_ALREADY_EXISTS);
        }
        return SUCCESS_VALIDATE_NICKNAME;
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {
        return MemberResponse.of(findMemberByIdAndStatus(id, ACTIVE));
    }

    @Transactional
    public MemberResponse update(Long id, UpdateRequest request) {
        Member member = findMemberByIdAndStatus(id, ACTIVE);

        validateNickname(request.getNickname());
        member.setNickname(request.getNickname());
        member.setProfileImage(request.getProfileImage());

        String ImageUrl = s3Uploader.getFilePath(member.getProfileImage());
        s3Uploader.deleteFile(ImageUrl);

        return MemberResponse.of(member);
    }

    @Transactional
    public StatusCode delete(Long id) {
        Member member = findMemberByIdAndStatus(id, ACTIVE);
        member.setStatus(DELETE);
        return SUCCESS_DELETE_MEMBER;
    }

    @Transactional(readOnly = true)
    public MemberDetailResponse getMyPage(Long id) {
        Member member = findMemberByIdAndStatus(id, ACTIVE);
        int friendCount = friendQueryRepository.findFriendCount(id);
        return MemberDetailResponse.of(member, friendCount);
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getOppPage(Long oppId, Long myId) {
        Member member = findMemberByIdAndStatus(oppId, ACTIVE);
        int friendCount = friendQueryRepository.findFriendCount(oppId);
        Status friendStatus = friendQueryRepository.findFriendStatus(myId, oppId);
        return MemberProfileResponse.of(member, friendCount, friendStatus);
    }

    @Transactional(readOnly = true)
    public List<MemberSearchResponse> findByNickname(String nickname, Long id) {
        List<Member> findMembers = memberSearchRepository.findByNicknameContainsWithNoOffset(ACTIVE, nickname, id, 10)
                .orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));

        /* save log into Redis */
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        String loginUserId = "id" + memberFacade.getCurrentMember().getId();
        String keyword = nickname;

        listOps.remove(loginUserId, 0, keyword);
        listOps.leftPush(loginUserId, keyword);

        List<MemberSearchResponse> friendshipList = new ArrayList<>();
        for (Member findMember : findMembers) {

            Status friendStatus = friendQueryRepository.findFriendStatus(memberFacade.getCurrentMember().getId(), findMember.getId());
            friendshipList.add(MemberSearchResponse.of(findMember, friendStatus));
        }
        return friendshipList;
    }

    @Transactional
    public StatusCode saveSearchLog(SearchLogRequest request, Long loginMemberId) {
        if (request.getId().equals(loginMemberId)) {
            throw new BaseException(INVALID_SAVE_SEARCH_LOG);
        }

        /* save search log into Redis */
        Member member = findMemberByIdAndStatus(request.getId(), ACTIVE);
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        String currentMemberId = "id" + loginMemberId;
        String memberId = "id" + member.getId();
        listOps.leftPush(currentMemberId, memberId);

        return SUCCESS_SAVE_SEARCH_LOG;
    }

    /* When the member is clicked, the member information is recorded in Redis.*/
    @Transactional(readOnly = true)
    public List<Object> getSearchLog(Long id) {
        String loginMemberId = "id" + id;
        List<String> numList = extractNumberAndAddToList(loginMemberId);
        List<Object> responses = addMemberToListByExtractedNum(numList);
        return responses;
    }

    /* Retrieves all the teams that a current member belongs to, and then retrieves all the members belonging to each team. */
    @Transactional(readOnly = true)
    public List<MemberTeamResponse> getMemberTeams(Long loginMemberId) {
        List<MemberTeam> memberTeams = memberTeamRepository.findAllByMemberIdAndStatusOrderByParticipatedAtDesc(loginMemberId, ACTIVE);

        List<MemberTeamResponse> teamList = new ArrayList<>();
        for (MemberTeam mt : memberTeams) {
            Team team = mt.getTeam();
            List<MemberTeam> memberTeam = memberTeamRepository.findAllByTeamIdAndStatus(team.getId(), ACTIVE);
            List<Member> members = memberTeam.stream().map(MemberTeam::getMember).collect(Collectors.toList());
            teamList.add(MemberTeamResponse.of(team, MemberResponse.of(members)));
        }
        return teamList;
    }

    @Transactional(readOnly = true)
    public List<MemberAchieveResponse> getMemberAchievementRate(LocalDate startDate, LocalDate endDate, Long loginMemberId) {
        List<Plan> plans = planRepository.findAllByManagerIdAndStatusAndDateBetweenOrderByDateAsc(loginMemberId, ACTIVE, startDate, endDate);

        return plans.stream().map(Plan::getDate).distinct().map(date -> {
            List<Plan> plansOnDate = planRepository.findAllByManagerIdAndStatusAndDateOrderByDateAsc(loginMemberId, ACTIVE, date);
            long complete = plansOnDate.stream().filter(plan -> plan.getAchievement() == COMPLETE).count();
            long incomplete = plansOnDate.size() - complete;
            double achievement = (complete + incomplete > 0) ? ((double) complete / (double) (complete + incomplete) * 100.0) : 0.0;
            return new MemberAchieveResponse(date, achievement);
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<MemberPlanDetailResponse> getMemberCalendarByDate(LocalDate date, Long loginMemberId) {
        List<MemberTeam> memberTeams = memberTeamRepository.findAllByMemberIdAndStatusOrderByParticipatedAtDesc(loginMemberId, ACTIVE);
        List<Team> teams = memberTeams.stream().map(MemberTeam::getTeam).collect(Collectors.toList());

        List<MemberPlanDetailResponse> response = new ArrayList<>();
        for (Team team : teams) {
            List<Plan> plans = planRepository.findAllByTeamIdAndManagerIdAndStatusAndDate(team.getId(), loginMemberId, ACTIVE, date);
            List<TeamPlanResponse> planList = plans.stream().map(this::createTeamPlanResponse).collect(Collectors.toList());
            response.add(MemberPlanDetailResponse.of(team, planList));
        }
        return response;
    }

    @Transactional
    public List<MemberBadgeResponse> getMemberBadges(Long id) {
        Member member = findMemberByIdAndStatus(id, ACTIVE);

        List<MemberBadge> memberBadges = memberBadgeRepository.findByMemberId(member.getId());

        List<MemberBadgeResponse> response = new ArrayList<>();
        for (MemberBadge memberBadge : memberBadges) {
            Long badgeId = memberBadge.getBadge().getId();
            Badge badge = badgeRepository.findById(badgeId).orElseThrow(() -> new BaseException(BADGE_NOT_FOUND));
            response.add(MemberBadgeResponse.of(badge));
        }

        return response;
    }

    @Transactional
    public List<MemberBadgeResponse> getMemberPreBadges(Long id) {
        Member member = findMemberByIdAndStatus(id, ACTIVE);

        List<MemberBadge> memberBadges = memberBadgeRepository.findTop8ByMemberIdAndStatusOrderByBadgeIdAsc(id, ACTIVE);

        List<MemberBadgeResponse> response = new ArrayList<>();
        for (MemberBadge memberBadge : memberBadges) {
            Long badgeId = memberBadge.getBadge().getId();
            Badge badge = badgeRepository.findById(badgeId).orElseThrow(() -> new BaseException(BADGE_NOT_FOUND));
            response.add(MemberBadgeResponse.of(badge));
        }

        return response;
    }

    private TeamPlanResponse createTeamPlanResponse(Plan plan) {
        return TeamPlanResponse.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .date(plan.getDate())
                .status(plan.getStatus())
                .achievement(plan.getAchievement())
                .manager(MemberResponse.of(plan.getManager()))
                .build();
    }

    /* Here's extracted Method from Main Method */
    private Member findMemberByIdAndStatus(Long id, Status status) {
        return memberRepository.findByIdAndStatus(id, status).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
    }

    private List<String> extractNumberAndAddToList(String loginMemberId) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        List<String> list = new ArrayList<>();
        List<Object> redisData = listOps.range(loginMemberId, 0, 20);

        for (Object o : redisData) {
            if (o != null) {
                String str = o.toString();

                if (str.length() >= 2) {
                    str = str.substring(0, 2);

                    if (!str.equals("id") && list.size() < 10) { // keyword
                        list.add(str);
                    } else {
                        String memberId = o.toString().substring(2);
                        if (!list.contains(memberId) && list.size() < 10) {
                            list.add(memberId);
                        }
                    }
                }
            }
        }

        return list;
    }

    /* 추출한 숫자값으로 Member 정보를 가져온다. */
    private List<Object> addMemberToListByExtractedNum(List<String> numList) {
        List<Object> memberList = new ArrayList<>();
        for (String num : numList) {
            if (isLong(num)) {
                Long memberId = Long.parseLong(num);
                Member member = findMemberByIdAndStatus(memberId, ACTIVE);
                memberList.add(MemberResponse.of(member));
            } else {
                memberList.add(MemberKeywordResponse.of(num));
            }
        }
        return memberList;
    }

    private boolean isLong(String strValue) {
        try {
            Long.parseLong(strValue);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
