package com.app.pingpong.domain.team.controller;

import com.app.pingpong.domain.team.dto.request.TeamAchieveRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanPassRequest;
import com.app.pingpong.domain.team.dto.request.TeamPlanRequest;
import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.dto.response.TeamPlanResponse;
import com.app.pingpong.domain.team.service.TeamService;
import com.app.pingpong.global.common.status.Status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static com.app.pingpong.global.common.exception.StatusCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TeamControllerTest {

    @InjectMocks
    TeamController teamController;

    @Mock
    TeamService teamService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamController).build();
    }

    @Test
    public void createTeamTest() throws Exception {
        // given
        TeamRequest request = new TeamRequest("teamName", List.of(1L, 2L));

        // when, then
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteTeamTest() throws Exception {
        // given
        Long id = 1L;
        doReturn(SUCCESS_DELETE_TEAM).when(teamService).delete(id);

        // when, then
        mockMvc.perform(delete("/api/teams/{id}", id))
                .andExpect(status().isOk());
        verify(teamService).delete(id);
    }

    @Test
    public void updateHostTest() throws Exception {
        // given
        Long teamId = 1L;
        Long delegatorId = 1L;

        // when, then
        mockMvc.perform(patch("/api/teams/{id}/host", teamId)
                        .param("delegatorId", String.valueOf(delegatorId)))
                .andExpect(status().isOk());
        verify(teamService).updateHost(any(), any(), any());
    }

    @Test
    public void emitTest() throws Exception {
        // given
        Long teamId = 1L;
        Long emitterId = 1L;

        // when, then
        mockMvc.perform(patch("/api/teams/{id}/emit", teamId)
                        .param("emitterId", String.valueOf(emitterId)))
                .andExpect(status().isOk());
        verify(teamService).emit(any(), any());
    }

    @Test
    public void getTeamMembersTest() throws Exception {
        // given
        Long teamId = 1L;

        // when, then
        mockMvc.perform(get("/api/teams/{id}/members", teamId))
                .andExpect(status().isOk());
        verify(teamService).getTeamMembers(any());
    }

    @Test
    public void acceptTest() throws Exception {
        // given
        Long teamId = 1L;
        given(teamService.accept(any())).willReturn(SUCCESS_ACCEPT_TEAM_INVITATION);

        // when, then
        mockMvc.perform(post("/api/teams/{id}/accept", teamId))
                .andExpect(status().isOk());
        verify(teamService).accept(any());
    }

    @Test
    public void refuseTest() throws Exception {
        // given
        Long teamId = 1L;
        given(teamService.refuse(any())).willReturn(SUCCESS_REFUSE_TEAM_INVITATION);

        // when, then
        mockMvc.perform(post("/api/teams/{id}/refuse", teamId))
                .andExpect(status().isOk());
        verify(teamService).refuse(any());
    }

    @Test
    public void resignTest() throws Exception {
        // given
        Long teamId = 1L;
        given(teamService.resign(any(), any())).willReturn(SUCCESS_RESIGN_TEAM);

        // when, then
        mockMvc.perform(post("/api/teams/{id}/resign", teamId))
                .andExpect(status().isOk());
        verify(teamService).resign(any(), any());
    }

    @Test
    public void createPlanTest() throws Exception {
        // given
        Long teamId = 1L;
        Long managerId = 1L;
        TeamPlanRequest request = new TeamPlanRequest(managerId, "plan title", LocalDate.now());
        TeamPlanResponse response = new TeamPlanResponse(1L, managerId, "title", LocalDate.now(), Status.ACTIVE, Status.INCOMPLETE);
        given(teamService.createPlan(any(), any())).willReturn(response);

        // when, then
        mockMvc.perform(post("/api/teams/{id}/plans", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(teamService).createPlan(any(), any());
    }

    @Test
    public void deletePlanTest() throws Exception {
        // given
        Long teamId = 1L;
        Long planId = 1L;

        // when, then
        mockMvc.perform(delete("/api/teams/{teamId}/plans/{planId}", teamId, planId))
                .andExpect(status().isOk());
        verify(teamService).deletePlan(any(), any());
    }

    @Test
    public void passPlanTest() throws Exception {
        // given
        Long teamId = 1L;
        Long planId = 1L;
        Long mandatorId = 1L;
        TeamPlanPassRequest request = new TeamPlanPassRequest(planId, mandatorId);

        // when, then
        mockMvc.perform(patch("/api/teams/{teamId}/plans/pass", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(teamService).passPlan(any(), any(), any());
    }

    @Test
    public void completePlanTest() throws Exception {
        // given
        Long teamId = 1L;
        Long planId = 1L;
        given(teamService.completePlan(any(), any(), any())).willReturn(SUCCESS_COMPLETE_PLAN);

        // when, then
        mockMvc.perform(patch("/api/teams/{teamId}/plans/{planId}/complete", teamId, planId))
                .andExpect(status().isOk());
        verify(teamService).completePlan(any(), any(), any());
    }

    @Test
    public void incompletePlanTest() throws Exception {
        // given
        Long teamId = 1L;
        Long planId = 1L;
        given(teamService.incompletePlan(any(), any(), any())).willReturn(SUCCESS_INCOMPLETE_PLAN);


        // when, then
        mockMvc.perform(patch("/api/teams/{teamId}/plans/{planId}/incomplete", teamId, planId))
                .andExpect(status().isOk());
        verify(teamService).incompletePlan(any(), any(), any());
    }

    @Test
    public void getTeamCalendarByDate() throws Exception {
        // given
        Long teamId = 1L;
        LocalDate date = LocalDate.now();

        // when, then
        mockMvc.perform(get("/api/teams/{teamId}/calendars", teamId)
                        .param("date", String.valueOf(date)))
                .andExpect(status().isOk());
    }

    @Test
    public void getTeamAchievementRateTest() throws Exception {
        // given
        Long teamId = 1L;
        LocalDate startDate = LocalDate.of(2023, 05, 01);
        LocalDate endDate = LocalDate.of(2023, 05, 30);
        TeamAchieveRequest request = new TeamAchieveRequest(startDate, endDate);

        // when, then
        mockMvc.perform(get("/api/teams/{teamId}/calendars/achievement", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(teamService).getTeamAchievementRate(any(), any());
    }

    @Test
    public void getTrashTest() throws Exception {
        // given
        Long teamId = 1L;

        // when, then
        mockMvc.perform(get("/api/teams/{teamId}/trash", teamId))
                .andExpect(status().isOk());
        verify(teamService).getTrash(any(), any());
    }

    @Test
    public void deleteAllTrashTest() throws Exception {
        // given
        Long teamId = 1L;
        given(teamService.deleteAllTrash(any(), any())).willReturn(SUCCESS_DELETE_ALL_TRASH);

        // when, then
        mockMvc.perform(delete("/api/teams/{teamId}/all-trash", teamId))
                .andExpect(status().isOk());
        verify(teamService).deleteAllTrash(any(), any());
    }

    @Test
    public void deleteTrashTest() throws Exception {
        // given
        Long teamId = 1L;
        Long planId = 1L;
        given(teamService.deleteTrash(any(), any(), any())).willReturn(SUCCESS_DELETE_TRASH);

        // when, then
        mockMvc.perform(delete("/api/teams/{teamId}/trash/{planId}", teamId, planId))
                .andExpect(status().isOk());
        verify(teamService).deleteTrash(any(), any(), any());
    }

    @Test
    public void recoverTrashTest() throws Exception {
        // given
        Long teamId = 1L;
        Long planId = 1L;
        given(teamService.recoverTrash(any(), any(), any())).willReturn(SUCCESS_RECOVER_TRASH);

        // when, then
        mockMvc.perform(patch("/api/teams/{teamId}/trash/{planId}", teamId, planId))
                .andExpect(status().isOk());
        verify(teamService).recoverTrash(any(), any(), any());
    }
}
