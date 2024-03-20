package com.app.pingpong.domain.admin.controller;

import com.app.pingpong.domain.admin.dto.AdminDetailResponse;
import com.app.pingpong.domain.admin.dto.AdminRequest;
import com.app.pingpong.domain.admin.dto.AdminResponse;
import com.app.pingpong.domain.admin.dto.AdminUpdateRequest;
import com.app.pingpong.domain.admin.service.AdminService;
import com.app.pingpong.global.common.exception.StatusCode;
import com.app.pingpong.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @ResponseBody
    @PostMapping("/posts")
    public BaseResponse<AdminDetailResponse> create(@RequestBody AdminRequest request) {
        return new BaseResponse<>(adminService.create(request));
    }

    @ResponseBody
    @PatchMapping("/posts/{id}")
    public BaseResponse<AdminDetailResponse> update(@PathVariable Long id, @RequestBody AdminUpdateRequest request) {
        return new BaseResponse<>(adminService.update(id, request));
    }

    @ResponseBody
    @DeleteMapping("/posts/{id}")
    public BaseResponse<StatusCode> delete(@PathVariable Long id) {
        return new BaseResponse<>(adminService.delete(id));
    }

    @ResponseBody
    @GetMapping("/posts/{id}")
    public BaseResponse<AdminDetailResponse> findById(@PathVariable Long id) {
        return new BaseResponse<>(adminService.findById(id));
    }

    @ResponseBody
    @GetMapping("/posts")
    public BaseResponse<List<AdminResponse>> findAll() {
        return new BaseResponse<>(adminService.findAll());
    }
}
