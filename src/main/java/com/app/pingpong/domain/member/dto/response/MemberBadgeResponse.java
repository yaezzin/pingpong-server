package com.app.pingpong.domain.member.dto.response;

import com.app.pingpong.domain.member.entity.Badge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBadgeResponse {
    private Long badgeId;
    private String badgeName;
    private boolean hasBadge;

    public static MemberBadgeResponse of(Badge badge, boolean hasBadge) {
        return new MemberBadgeResponse(badge.getId(), badge.getName(), hasBadge);
    }
}
