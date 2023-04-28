package com.app.pingpong.domain.friend.controller;

import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.service.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FriendControllerTest {

    @InjectMocks
    FriendController friendController;

    @Mock
    FriendService friendService;

    ObjectMapper objectMapper = new ObjectMapper();

    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(friendController).build();
    }

    @Test
    @DisplayName("friendController - 친구 신청")
    public void apply() throws Exception {
        // given
        FriendRequest request = new FriendRequest(1L, 2L);

        // when, then
        mockMvc.perform(post("/api/friends/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(friendService).apply(any());
    }

    @Test
    @DisplayName("friendController - getMyFriends")
    public void getMyFriends() throws Exception {
        // given
        Long memberId = 1L;

        // when, then
        mockMvc.perform(get("/api/friends")
                        .param("id", memberId.toString()))
                .andExpect(status().isOk());
        verify(friendService).getMyFriends(anyLong());
    }
}

