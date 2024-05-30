package com.app.pingpong.domain.member.repository;

import com.app.pingpong.domain.member.entity.Badge;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findAllByStatus(Status status);
}
