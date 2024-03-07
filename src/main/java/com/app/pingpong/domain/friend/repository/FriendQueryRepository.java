package com.app.pingpong.domain.friend.repository;

import com.app.pingpong.domain.friend.entity.Friend;
import com.app.pingpong.domain.friend.entity.QFriend;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.status.Status;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.app.pingpong.global.common.exception.StatusCode.FRIEND_NOT_FOUND;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.WAIT;

@RequiredArgsConstructor
@Repository
public class FriendQueryRepository {

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
                .fetchFirst() != null;
    }

    public Status findFriendStatus(Long loginMemberId, Long searchMemberId) {
        BooleanExpression condition1 = QFriend.friend.applicant.eq(loginMemberId).and(QFriend.friend.respondent.eq(searchMemberId));
        BooleanExpression condition2 = QFriend.friend.applicant.eq(searchMemberId).and(QFriend.friend.respondent.eq(loginMemberId));

        Friend friend = queryFactory.selectFrom(QFriend.friend)
                .where(condition1.or(condition2))
                .fetchFirst();

        if (friend != null) {
            switch (friend.getStatus()) {
                case ACTIVE:
                    return ACTIVE;
                case WAIT:
                    return WAIT;
                case DELETE:
                default:
                    return Status.INACTIVE; // Set a default status when no friend relationship is found
            }
        } else {
            return Status.INACTIVE;
        }
    }

    public boolean checkFriendship(Long loginUserId, Long searchedUserId) {
        BooleanExpression isActiveFriend = QFriend.friend.status.eq(Status.ACTIVE);
        BooleanExpression isLoginUser = QFriend.friend.applicant.eq(loginUserId).or(QFriend.friend.respondent.eq(loginUserId));
        BooleanExpression isSearchedUser = QFriend.friend.applicant.eq(searchedUserId).or(QFriend.friend.respondent.eq(searchedUserId));

        Friend friendship = queryFactory.selectFrom(QFriend.friend)
                .where(isActiveFriend, isLoginUser, isSearchedUser)
                .fetchFirst();

        if (friendship == null) {
            throw new BaseException(FRIEND_NOT_FOUND);
        }

        return true;
    }

    public int findFriendCount(Long id) {
        BooleanExpression friendCondition = QFriend.friend.applicant.eq(id)
                .or(QFriend.friend.respondent.eq(id))
                .and(QFriend.friend.status.eq(ACTIVE));

        int count_long = queryFactory.select(QFriend.friend.count())
                .from(QFriend.friend)
                .where(friendCondition)
                .fetchOne().intValue();
        return count_long;
    }

    public Optional<Friend> findFriendById(Long friendId, Long userId) {
        QFriend friend = QFriend.friend;

        Friend result = queryFactory
                .selectFrom(friend)
                .where(
                        friend.status.eq(ACTIVE)
                                .and(
                                        friend.applicant.eq(userId).and(friend.respondent.eq(friendId))
                                                .or(friend.applicant.eq(friendId).and(friend.respondent.eq(userId)))
                                )
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
