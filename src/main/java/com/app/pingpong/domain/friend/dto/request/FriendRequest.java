package com.app.pingpong.domain.friend.dto.request;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

import static com.app.pingpong.global.common.Status.WAIT;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequest {
    private Long applicantId;
    private Long respondentId; // 피신청인 id (= 내가 친구 걸은 사람)

    @Builder
    public Friend toEntity(Long applicant, Long respondent) {
        return Friend.builder()
                .applicant(applicant)
                .respondent(respondent)
                .status(WAIT)
                .createdAt(new Date())
                .build();
    }
}
