package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.entity.QFriend;
import com.app.pingpong.global.common.Status;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.app.pingpong.global.common.Status.ACTIVE;
import static com.app.pingpong.global.common.Status.WAIT;

@RequiredArgsConstructor
@Repository
public class FriendFactory {

    private final JPAQueryFactory queryFactory;

    public boolean existsRequestToRespondent(Long applicantId, Long respondentId, Status status) {
        BooleanExpression activeFriendRequest = QFriend.friend.applicant.id.eq(applicantId)
                .and(QFriend.friend.respondent.id.eq(respondentId))
                .and(QFriend.friend.status.eq(status));

        Boolean exists = queryFactory.selectOne()
                .from(QFriend.friend)
                .where(activeFriendRequest)
                .fetchOne() != null;
        return exists;
    }

    public Optional<Friend> findWaitRequestBy(Long applicantId, Long respondentId) {
        QFriend friend = QFriend.friend;
        BooleanExpression waitFriendRequest = friend.applicant.id.eq(applicantId)
                .and(friend.respondent.id.eq(respondentId))
                .and(friend.status.eq(WAIT));

        return Optional.ofNullable(queryFactory.selectFrom(friend)
                .where(waitFriendRequest)
                .fetchOne());
    }

    public boolean isFriend(Long loginUserId, Long searchedUserId) {
        BooleanExpression isActiveFriend = QFriend.friend.status.eq(Status.ACTIVE);
        BooleanExpression isLoginUser = QFriend.friend.applicant.id.eq(loginUserId).or(QFriend.friend.respondent.id.eq(loginUserId));
        BooleanExpression isSearchedUser = QFriend.friend.applicant.id.eq(searchedUserId).or(QFriend.friend.respondent.id.eq(searchedUserId));

        return queryFactory.selectFrom(QFriend.friend)
                .where(isActiveFriend, isLoginUser, isSearchedUser)
                .fetchCount() > 0;
    }

    public int findFriendCount(Long id) {
        BooleanExpression friendCondition = QFriend.friend.applicant.id.eq(id)
                .or(QFriend.friend.respondent.id.eq(id))
                .and(QFriend.friend.status.eq(ACTIVE));

        Long count_long = queryFactory.select(friendCondition.count())
                .from(QFriend.friend)
                .fetchOne();
        return Long.valueOf(count_long).intValue();
    }
}

