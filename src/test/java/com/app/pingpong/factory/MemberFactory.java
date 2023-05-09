package com.app.pingpong.factory;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Authority;

import java.util.ArrayList;
import java.util.List;

import static com.app.pingpong.global.common.status.Status.ACTIVE;

public class MemberFactory {

    public static Member createMember() {
        return new Member("123", "email", "nickname", "profileImage", ACTIVE, Authority.ROLE_USER);
    }

    public static Member createMember(String email, String nickname) {
        return new Member("123", email, nickname, "profileImage", ACTIVE, Authority.ROLE_USER);
    }

    public static Member createMember(String nickname) {
        return new Member("123", "email", nickname, "profileImage", ACTIVE, Authority.ROLE_USER);
    }

    public static List<Member> createMultipleMemberByCount(int count) {
        List<Member> list = new ArrayList<>();
        for (int i = 0; i <= count; i++) {
            list.add(createMember());
        }
        return list;
    }
}
