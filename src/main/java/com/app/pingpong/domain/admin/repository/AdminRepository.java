package com.app.pingpong.domain.admin.repository;

import com.app.pingpong.domain.admin.entity.Admin;
import com.app.pingpong.global.common.status.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByIdAndStatus(Long id, Status status);

    List<Admin> findAllByStatus(Status status);
}
