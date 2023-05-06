package com.app.pingpong.factory;

import com.app.pingpong.domain.friend.entity.Friend;

import java.util.Date;

import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.WAIT;

public class FriendFactory {

    public static Friend createFriend(Long applicantId, Long respondentId) {
        return new Friend(applicantId, respondentId, WAIT, new Date());
    }

    public static Friend createActiveFriend(Long applicantId, Long respondentId) {
        return new Friend(applicantId, respondentId, ACTIVE, new Date());
    }
}
