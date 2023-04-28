package com.app.pingpong.domain.member.controller;

import com.app.pingpong.domain.member.dto.request.MemberAchieveRequest;
import com.app.pingpong.domain.member.dto.request.SearchLogRequest;
import com.app.pingpong.domain.member.dto.request.SignUpRequest;
import com.app.pingpong.domain.member.dto.request.UpdateRequest;
import com.app.pingpong.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @InjectMocks
    MemberController memberController;

    @Mock
    MemberService memberService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @Test
    @DisplayName("Member Controller - 회원가입")
    public void signup() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("socialId", "email", "nickname", "profile");

        // when, then
        mockMvc.perform(post("/api/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Member Controller - 유효성 검증")
    public void validateNickname() throws Exception {
        // given
        String nickname = "member";

        // when, then
        mockMvc.perform(post("/api/members/validate")
                        .param("nickname", nickname))
                .andExpect(status().isOk());
        verify(memberService).validateNickname(nickname);
    }

    @Test
    @DisplayName("Member Controller - id로 회원 조회")
    public void findById() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(get("/api/members/{id}", id))
                .andExpect(status().isOk());
        verify(memberService).findById(id);
    }

    @Test
    @DisplayName("Member Controller - 회원정보 수정")
    public void update() throws Exception {
        // given
        Long id = 1L;
        UpdateRequest update = new UpdateRequest("nickname", "profile");

        // when, then
        mockMvc.perform(patch("/api/members/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
        verify(memberService).update(anyLong(), any());
    }

    @Test
    @DisplayName("Member Controller - 회원탈퇴")
    public void deleteMember() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/api/members/{id}", id))
                .andExpect(status().isOk());
        verify(memberService).delete(id);
    }

    @Test
    @DisplayName("Member Controller - 마이페이지 조회")
    public void getMyPage() throws Exception {
        Long id = 1L;
        mockMvc.perform(get("/api/members/{id}/mypage", id))
                .andExpect(status().isOk());
        verify(memberService).getMyPage(anyLong());
    }

    @Test
    @DisplayName("Member Controller - 상대방 페이지 조회")
    public void getOppPage() throws Exception {
        Long id = 1L;
        mockMvc.perform(get("/api/members/{id}/profile", id))
                .andExpect(status().isOk());
        verify(memberService).getOppPage(anyLong());
    }

    @Test
    @DisplayName("Member Controller - 닉네임로 회원 조회")
    public void findByNickname() throws Exception {
        String nickname = "test";
        Long id = 1L;

        mockMvc.perform(get("/api/members/search")
                        .param("nickname", nickname)
                        .param("id", id.toString()))
                .andExpect(status().isOk());
        verify(memberService).findByNickname(anyString(), anyLong());
    }

    @Test
    @DisplayName("MemberController - 검색 기록 저장")
    public void saveSearchLog() throws Exception {
        // given
        Long loginMemberId = 1L;
        SearchLogRequest request = new SearchLogRequest(2L);

        // when, then
        mockMvc.perform(post("/api/members/search-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());
        verify(memberService).saveSearchLog(eq(request), eq(loginMemberId));
    }

    @Test
    @DisplayName("MemberController - 검색 기록 조회")
    public void getSearchLog() throws Exception {
        // given
        Long loginMemberId = 1L;
        SearchLogRequest request = new SearchLogRequest(2L);

        // when, then
        mockMvc.perform(get("/api/members/search-log")
                        .param("id", loginMemberId.toString())
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());
        verify(memberService).getSearchLog(anyLong());
    }

    @Test
    @DisplayName("Member Controller - 회원 팀 조회")
    public void getMemberTeams() throws Exception {
        Long id = 1L;
        mockMvc.perform(get("/api/members/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());
        verify(memberService).getMemberTeams(any());
    }

    @Test
    @DisplayName("Member Controller - 멤버 성취율 조회")
    public void getMemberAchievement() throws Exception {
        // given
        Long id = 1L;
        MemberAchieveRequest request = new MemberAchieveRequest(LocalDate.now(), LocalDate.now());

        // when, then
        mockMvc.perform(get("/api/members/calendars/achievement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());
        verify(memberService).getMemberAchievementRate(any(), any());
    }

    @Test
    @DisplayName("Member Controller - 멤버 캘린더 조회")
    public void getMemberCalendarByDate() throws Exception {
        // given
        Long id = 1L;
        LocalDate date = LocalDate.now();

        // when, then
        mockMvc.perform(get("/api/members/calendars")
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk());
        verify(memberService).getMemberCalendarByDate(any(), any());
    }
}
