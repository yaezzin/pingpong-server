package com.app.pingpong.domain.admin.service;

import com.app.pingpong.domain.admin.dto.AdminRequest;
import com.app.pingpong.domain.admin.dto.AdminResponse;
import com.app.pingpong.domain.admin.dto.AdminUpdateRequest;
import com.app.pingpong.domain.admin.entity.Admin;
import com.app.pingpong.domain.admin.repository.AdminRepository;
import com.app.pingpong.global.common.exception.BaseException;
import com.app.pingpong.global.common.exception.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.ADMiN_POST_NOT_FOUND;
import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_DELETE_ADMIN_POST;
import static com.app.pingpong.global.common.status.Status.ACTIVE;
import static com.app.pingpong.global.common.status.Status.DELETE;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminResponse create(AdminRequest request) {
        Admin post = request.toEntity();
        Admin adminPost = adminRepository.save(post);
        return AdminResponse.of(adminPost);
    }

    public AdminResponse update(Long id, AdminUpdateRequest request) {
        Admin post = adminRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(ADMiN_POST_NOT_FOUND));
        post.setTitle(request.getTitle());
        post.setContents(request.getContents());
        return AdminResponse.of(post);
    }

    public StatusCode delete(Long id) {
        Admin post = adminRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(ADMiN_POST_NOT_FOUND));
        post.setStatus(DELETE);
        return SUCCESS_DELETE_ADMIN_POST;
    }

    public AdminResponse findById(Long id) {
        Admin post = adminRepository.findByIdAndStatus(id, ACTIVE).orElseThrow(() -> new BaseException(ADMiN_POST_NOT_FOUND));
        return AdminResponse.of(post);
    }

    public List<AdminResponse> findAll() {
        List<Admin> posts = adminRepository.findAllByStatus(ACTIVE);
        return AdminResponse.of(posts);
    }
}
