package com.app.pingpong.global.common.util;

import com.app.pingpong.domain.member.entity.Badge;
import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.domain.member.entity.MemberBadge;
import com.app.pingpong.domain.member.repository.BadgeRepository;
import com.app.pingpong.domain.member.repository.MemberBadgeRepository;
import com.app.pingpong.domain.member.repository.MemberRepository;
import com.app.pingpong.domain.team.entity.Plan;
import com.app.pingpong.domain.team.repository.PlanRepository;
import com.app.pingpong.global.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.BADGE_NOT_FOUND;
import static com.app.pingpong.global.common.exception.StatusCode.MEMBER_NOT_FOUND;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.COMPLETE;

@RequiredArgsConstructor
@Component
public class BadgeScheduler {

    private final MemberRepository memberRepository;

    private final PlanRepository planRepository;

    private final MemberBadgeRepository memberBadgeRepository;

    private final BadgeRepository badgeRepository;

    private static final int[] PLAN_COUNT_THRESHOLDS = {1, 3, 5, 10, 100};

    @Scheduled(cron = "0 0 17 * * *")
    public void addBadges() {
        List<Member> members = memberRepository.findAllByStatus(ACTIVE);

        for (Member member : members) {
            int planCount = getCompletedPlanCount(member.getId());

            for (int i = 0; i < PLAN_COUNT_THRESHOLDS.length; i++) {
                if (planCount >= PLAN_COUNT_THRESHOLDS[i]) {
                    addBadge(member.getId(), i + 1);
                }
            }
        }
    }

    private int getCompletedPlanCount(Long memberId) {
        List<Plan> plans = planRepository.findAllByManagerIdAndAchievementAndStatus(memberId, COMPLETE, ACTIVE);
        return plans.size();
    }

    private void addBadge(Long memberId, int badgeLevel) {
        Member member = memberRepository.findByIdAndStatus(memberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        System.out.println(badgeLevel);
        Badge badge = badgeRepository.findById(Long.valueOf(badgeLevel)).orElseThrow(() -> new BaseException(BADGE_NOT_FOUND));
        boolean exist = memberBadgeRepository.existsByMemberIdAndBadgeIdAndStatus(memberId, badge.getId(), ACTIVE);

        if (!exist) {
            MemberBadge memberBadge = new MemberBadge();
            memberBadge.setMember(member);
            memberBadge.setBadge(badge);
            memberBadge.setStatus(ACTIVE);
            memberBadgeRepository.save(memberBadge);
        }
    }
}
