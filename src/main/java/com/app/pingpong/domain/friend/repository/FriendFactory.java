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
        BooleanExpression activeFriendRequest = QFriend.friend.applicant.eq(applicantId)
                .and(QFriend.friend.respondent.eq(respondentId))
                .and(QFriend.friend.status.eq(status));

        Integer exists = queryFactory.selectOne()
                .from(QFriend.friend)
                .where(activeFriendRequest)
                .fetchFirst();
        return exists != null;
    }

    public Optional<Friend> findWaitRequestBy(Long applicantId, Long respondentId) {
        QFriend friend = QFriend.friend;
        BooleanExpression waitFriendRequest = friend.applicant.eq(applicantId)
                .and(friend.respondent.eq(respondentId))
                .and(friend.status.eq(WAIT));

        return Optional.ofNullable(queryFactory.selectFrom(friend)
                .where(waitFriendRequest)
                .fetchOne());
    }

    public boolean isFriend(Long loginUserId, Long searchedUserId) {
        BooleanExpression isActiveFriend = QFriend.friend.status.eq(Status.ACTIVE);
        BooleanExpression isLoginUser = QFriend.friend.applicant.eq(loginUserId).or(QFriend.friend.respondent.eq(loginUserId));
        BooleanExpression isSearchedUser = QFriend.friend.applicant.eq(searchedUserId).or(QFriend.friend.respondent.eq(searchedUserId));

        return queryFactory.selectFrom(QFriend.friend)
                .where(isActiveFriend, isLoginUser, isSearchedUser)
                .fetchCount() > 0;
    }

    public int findFriendCount(Long id) {
        BooleanExpression friendCondition = QFriend.friend.applicant.eq(id)
                .or(QFriend.friend.respondent.eq(id))
                .and(QFriend.friend.status.eq(ACTIVE));

        Long count_long = queryFactory.select(friendCondition.count())
                .from(QFriend.friend)
                .fetchOne();
        return Long.valueOf(count_long).intValue();
    }
}

