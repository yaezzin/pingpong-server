package com.app.pingpong.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AdminDetailResponse {
    private Long id;
    private String title;
    private String contents;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
