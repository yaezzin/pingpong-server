package com.app.pingpong.factory;

import com.app.pingpong.domain.friend.entity.Friend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.WAIT;

public class FriendFactory {

    public static Friend createFriend(Long applicantId, Long respondentId) {
        return new Friend(applicantId, respondentId, WAIT, new Date());
    }

    public static Friend createActiveFriend(Long applicantId, Long respondentId) {
        return new Friend(applicantId, respondentId, ACTIVE, new Date());
    }

    public static List<Friend> createMultipleFriendsByCount(Long applicantId, int count) {
        List<Friend> list = new ArrayList<>();
        for (long i = 2L; i <= count; i++) {
            list.add(createActiveFriend(applicantId, i));
        }
        return list;
    }
}
