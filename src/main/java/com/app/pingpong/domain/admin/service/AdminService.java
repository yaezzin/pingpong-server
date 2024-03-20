package com.app.pingpong.domain.admin.service;

import com.app.pingpong.domain.admin.dto.AdminDetailResponse;
import com.app.pingpong.domain.admin.dto.AdminRequest;
import com.app.pingpong.domain.admin.dto.AdminResponse;
import com.app.pingpong.domain.admin.dto.AdminUpdateRequest;
import com.app.pingpong.domain.admin.entity.Admin;
import com.app.pingpong.domain.admin.repository.AdminRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.ADMiN_POST_NOT_FOUND;
import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_DELETE_ADMIN_POST;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.DELETE;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminDetailResponse create(AdminRequest request) {
        Admin post = request.toEntity();
        Admin adminPost = adminRepository.save(post);
        return AdminDetailResponse.of(adminPost);
    }

    @Transactional
    public AdminDetailResponse update(Long id, AdminUpdateRequest request) {
        Admin post = adminRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(ADMiN_POST_NOT_FOUND));
        post.setTitle(request.getTitle());
        post.setContents(request.getContents());
        return AdminDetailResponse.of(post);
    }

    @Transactional
    public StatusCode delete(Long id) {
        Admin post = adminRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(ADMiN_POST_NOT_FOUND));
        post.setStatus(DELETE);
        return SUCCESS_DELETE_ADMIN_POST;
    }

    public AdminDetailResponse findById(Long id) {
        Admin post = adminRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(ADMiN_POST_NOT_FOUND));
        return AdminDetailResponse.of(post);
    }

    public List<AdminResponse> findAll() {
        List<Admin> posts = adminRepository.findAllByStatusOrderByIdDesc(ACTIVE);
        return AdminResponse.of(posts);
    }
}
