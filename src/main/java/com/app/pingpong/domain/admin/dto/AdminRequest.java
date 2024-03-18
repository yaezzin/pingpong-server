package com.app.pingpong.domain.admin.dto;

import com.app.pingpong.domain.admin.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.app.pingpong.global.common.status.Status.ACTIVE;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest {
    private String title;
    private String contents;

    @Builder
    public Admin toEntity() {
        Admin post = Admin.builder()
                .title(title)
                .contents(contents)
                .status(ACTIVE)
                .build();

        return post;
    }
}
