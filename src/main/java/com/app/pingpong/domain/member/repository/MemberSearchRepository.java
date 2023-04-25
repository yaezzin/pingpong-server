package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Member;
import com.app.pingpong.global.common.status.Status;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.app.pingpong.domain.member.entity.QMember.member;

@RequiredArgsConstructor
@Repository
public class MemberSearchRepository {

    private final JPAQueryFactory queryFactory;

    public List<Member> findByNicknameContainsWithNoOffset(Status status, String nickname, Long lastMemberId, int pageSize) {
        BooleanExpression whereClause = member.status.eq(status).and(member.nickname.contains(nickname));
        BooleanExpression lastIdCondition = lastMemberId != null ? member.id.gt(lastMemberId) : null;
        return queryFactory
                .selectFrom(member)
                .where(whereClause, lastIdCondition)
                .orderBy(member.id.asc())
                .limit(pageSize)
                .fetch();
    }
}
