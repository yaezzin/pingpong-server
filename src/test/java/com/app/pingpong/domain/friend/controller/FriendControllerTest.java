package com.app.pingpong.domain.friend.controller;

import com.app.pingpong.domain.friend.dto.request.FriendRefuseRequest;
import com.app.pingpong.domain.friend.dto.request.FriendRequest;
import com.app.pingpong.domain.friend.service.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_ACCEPT_FRIEND;
import static com.app.pingpong.global.common.exception.StatusCode.SUCCESS_REFUSE_FRIEND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
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
    public void applyTest() throws Exception {
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
    public void acceptTest() throws Exception {
        // given
        Long opponentId = 1L;
        given(friendService.accept(any(), any())).willReturn(SUCCESS_ACCEPT_FRIEND);

        // when, then
        mockMvc.perform(post("/api/friends/accept")
                        .param("opponentId", String.valueOf(opponentId)))
                .andExpect(status().isOk());
        verify(friendService).accept(any(), any());
    }

    @Test
    public void refuseTest() throws Exception {
        // given
        Long opponentId = 1L;
        FriendRefuseRequest request = new FriendRefuseRequest(opponentId);
        given(friendService.refuse(any(), any())).willReturn(SUCCESS_REFUSE_FRIEND);

        // when, then
        mockMvc.perform(post("/api/friends/refuse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(friendService).refuse(any(), any());
    }

    @Test
    public void getMyFriendsTest() throws Exception {
        // given
        Long memberId = 1L;

        // when, then
        mockMvc.perform(get("/api/friends")
                        .param("id", memberId.toString()))
                .andExpect(status().isOk());
        verify(friendService).getMyFriends(anyLong());
    }
}

