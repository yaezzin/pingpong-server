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

    @Scheduled(cron = "0 40 18 * * *")
    public void addBadges() {
        List<Member> members = memberRepository.findAllByStatus(ACTIVE);

        for (Member member : members) {
            List<Plan> plans = planRepository.findAllByManagerIdAndAchievementAndStatus(member.getId(), COMPLETE, ACTIVE);
            int planCount = plans.size();

            if (planCount >= 1) {
                addBadge(member.getId(), 1L);
            }

            if (planCount >= 100) {
                addBadge(member.getId(), 2L);
            }

            if (planCount >= 200) {
                addBadge(member.getId(), 3L);
            }
        }
    }

    private void addBadge(Long memberId, Long badgeId) {
        Member member = memberRepository.findByIdAndStatus(memberId, ACTIVE).orElseThrow(() -> new BaseException(MEMBER_NOT_FOUND));
        boolean exist = memberBadgeRepository.existsByMemberIdAndBadgeIdAndStatus(memberId, badgeId, ACTIVE);
        Badge badge = badgeRepository.findById(badgeId).orElseThrow(() -> new BaseException(BADGE_NOT_FOUND));

        if (!exist) {
            MemberBadge memberBadge = new MemberBadge();
            memberBadge.setMember(member);
            memberBadge.setBadge(badge);
            memberBadge.setStatus(ACTIVE);
            memberBadgeRepository.save(memberBadge);
        }
    }
}
