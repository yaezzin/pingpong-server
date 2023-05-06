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

import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_SAVE_SEARCH_LOG;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    public void signupTest() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("socialId", "email", "nickname", "profile");

        // when, then
        mockMvc.perform(post("/api/members/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(memberService).signup(any());
    }

    @Test
    public void validateNicknameTest() throws Exception {
        // given
        String nickname = "member";

        // when, then
        mockMvc.perform(post("/api/members/validate")
                        .param("nickname", nickname))
                .andExpect(status().isOk());
        verify(memberService).validateNickname(any());
    }

    @Test
    public void findByIdTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(get("/api/members/{id}", id))
                .andExpect(status().isOk());
        verify(memberService).findById(any());
    }

    @Test
    public void updateTest() throws Exception {
        // given
        Long id = 1L;
        UpdateRequest request = new UpdateRequest("nickname", "profile");

        // when, then
        mockMvc.perform(patch("/api/members/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(memberService).update(any(), any());
    }

    @Test
    public void deleteMemberTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(delete("/api/members/{id}", id))
                .andExpect(status().isOk());
        verify(memberService).delete(any());
    }

    @Test
    public void getMyPageTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(get("/api/members/{id}/mypage", id))
                .andExpect(status().isOk());
        verify(memberService).getMyPage(any());
    }

    @Test
    public void getOppPageTest() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(get("/api/members/{id}/profile", id))
                .andExpect(status().isOk());
        verify(memberService).getOppPage(any());
    }

    @Test
    public void findByNicknameTest() throws Exception {
        // given
        String nickname = "test";
        Long id = 1L;

        // when, then
        mockMvc.perform(get("/api/members/search")
                        .param("nickname", nickname)
                        .param("id", id.toString()))
                .andExpect(status().isOk());
        verify(memberService).findByNickname(any(), any());
    }

    @Test
    public void saveSearchLogTest() throws Exception {
        // given
        Long loginMemberId = 1L;
        SearchLogRequest request = new SearchLogRequest(2L);

        given(memberService.saveSearchLog(any(), any())).willReturn(SUCCESS_SAVE_SEARCH_LOG);

        // when, then
        mockMvc.perform(post("/api/members/search-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(memberService).saveSearchLog(any(), any());
    }

    @Test
    public void getSearchLog() throws Exception {
        // given
        Long loginMemberId = 1L;
        SearchLogRequest request = new SearchLogRequest(2L);

        // when, then
        mockMvc.perform(get("/api/members/search-log")
                        .param("id", loginMemberId.toString()))
                .andExpect(status().isOk());
        verify(memberService).getSearchLog(any());
    }

    @Test
    public void getMemberTeams() throws Exception {
        // given
        Long id = 1L;

        // when, then
        mockMvc.perform(get("/api/members/teams"))
                .andExpect(status().isOk());
        verify(memberService).getMemberTeams(any());
    }

    @Test
    public void getMemberAchievement() throws Exception {
        // given
        Long id = 1L;
        MemberAchieveRequest request = new MemberAchieveRequest(LocalDate.now(), LocalDate.now());

        // when, then
        mockMvc.perform(get("/api/members/calendars/achievement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(memberService).getMemberCalendarByDate(any(), any());
    }
}
