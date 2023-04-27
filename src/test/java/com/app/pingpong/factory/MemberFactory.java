package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Authority;

import static com.app.pingpong.global.common.status.Status.ACTIVE;

public class MemberFactory {

    public static Member createMember() {
        return new Member("123", "email", "nickname", "profileImage", ACTIVE, Authority.ROLE_USER);
    }
}
