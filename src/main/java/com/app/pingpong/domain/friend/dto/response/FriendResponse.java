package com.app.pingpong.domain.friend.dto.response;

import com.app.pingpong.domain.friend.entity.Friend;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private Long applicantId;
    private Long respondentId;

    public static FriendResponse of(Friend friend) {
        return new FriendResponse(friend.getApplicant(), friend.getRespondent());
    }
}
