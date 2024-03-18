package com.app.pingpong.domain.admin.dto;

import com.app.pingpong.domain.admin.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminResponse {
    private Long id;
    private String title;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public static AdminResponse of(Admin post) {
        return new AdminResponse(post.getId(), post.getTitle(), post.getCratedAt(), post.getUpdatedAt());
    }

    public static List<AdminResponse> of(List<Admin> posts) {
        return posts.stream()
                .map(AdminResponse::of)
                .collect(Collectors.toList());
    }
}
