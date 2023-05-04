package com.app.pingpong.domain.team.controller;

import com.app.pingpong.domain.team.dto.request.TeamRequest;
import com.app.pingpong.domain.team.service.TeamService;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void create() throws Exception {
        // given
        TeamRequest request = new TeamRequest("teamName", List.of(1L, 2L));

        // when, then
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void delete() {
        
    }
}
