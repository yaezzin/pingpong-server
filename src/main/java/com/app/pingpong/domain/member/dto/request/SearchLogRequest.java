package com.app.pingpong.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchLogRequest {
    private Long id; // 내가 검색한 후 누른 유저의 아이디값
}
