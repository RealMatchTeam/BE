package com.example.RealMatch.chat.presentation.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.RealMatch.chat.application.service.ChatService;
import com.example.RealMatch.chat.presentation.config.ChatCursorConverterConfig;
import com.example.RealMatch.chat.presentation.controller.fixture.ChatFixtureFactory;
import com.example.RealMatch.chat.presentation.dto.enums.ChatAttachmentType;
import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.global.config.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("null")
@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ChatCursorConverterConfig.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("채팅방 생성/조회: 200 + 공통 응답 포맷")
    void createOrGetRoom_returnsOk() throws Exception {
        ChatRoomCreateRequest request = new ChatRoomCreateRequest(101L, 202L);

        given(chatService.createOrGetRoom(any(), any()))
                .willReturn(ChatFixtureFactory.sampleRoomCreateResponse());

        mockMvc.perform(post("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200_1"))
                .andExpect(jsonPath("$.message").value("정상적인 요청입니다."))
                .andExpect(jsonPath("$.result.roomId").value(3001))
                .andExpect(jsonPath("$.result.roomKey").value("direct:101:202"))
                .andExpect(jsonPath("$.result.lastProposalDirection").value("NONE"))
                .andExpect(jsonPath("$.result.createdAt").value("2025-01-01T00:00:00"));
    }

    @Test
    @DisplayName("채팅방 목록 조회: 200 + 공통 응답 포맷")
    void getRoomList_returnsOk() throws Exception {
        given(chatService.getRoomList(any(), any(), any(), any(), any(), anyInt()))
                .willReturn(ChatFixtureFactory.sampleRoomListResponse());

        mockMvc.perform(get("/api/chat/rooms")
                        .param("tab", "SENT")
                        .param("status", "ALL")
                        .param("sort", "LATEST")
                        .param("cursor", "2025-01-01T10:00:00|3001")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200_1"))
                .andExpect(jsonPath("$.message").value("정상적인 요청입니다."))
                .andExpect(jsonPath("$.result.sentTabUnreadCount").value(5))
                .andExpect(jsonPath("$.result.receivedTabUnreadCount").value(2))
                .andExpect(jsonPath("$.result.totalUnreadCount").value(7))
                .andExpect(jsonPath("$.result.rooms", hasSize(1)))
                .andExpect(jsonPath("$.result.rooms[0].roomId").value(3001))
                .andExpect(jsonPath("$.result.rooms[0].opponentUserId").value(202))
                .andExpect(jsonPath("$.result.rooms[0].opponentName").value("라운드랩"))
                .andExpect(jsonPath("$.result.rooms[0].proposalStatus").value("REVIEWING"))
                .andExpect(jsonPath("$.result.rooms[0].lastMessageType").value("TEXT"))
                .andExpect(jsonPath("$.result.rooms[0].tabCategory").value("SENT"))
                .andExpect(jsonPath("$.result.rooms[0].unreadCount").value(3))
                .andExpect(jsonPath("$.result.nextCursor").value("2025-01-01T09:59:59|2999"))
                .andExpect(jsonPath("$.result.hasNext", is(true)));
    }

    @Test
    @DisplayName("채팅방 상세 조회: 200 + 공통 응답 포맷")
    void getRoomDetail_returnsOk() throws Exception {
        given(chatService.getRoomDetail(any(), anyLong()))
                .willReturn(ChatFixtureFactory.sampleRoomDetailResponse(3001L));

        mockMvc.perform(get("/api/chat/rooms/{roomId}", 3001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200_1"))
                .andExpect(jsonPath("$.message").value("정상적인 요청입니다."))
                .andExpect(jsonPath("$.result.roomId").value(3001))
                .andExpect(jsonPath("$.result.opponentUserId").value(202))
                .andExpect(jsonPath("$.result.opponentName").value("라운드랩"))
                .andExpect(jsonPath("$.result.opponentTags", hasSize(3)))
                .andExpect(jsonPath("$.result.proposalStatus").value("REVIEWING"))
                .andExpect(jsonPath("$.result.proposalStatusLabel").value("검토중"));
    }

    @Test
    @DisplayName("채팅 메시지 조회: 200 + 공통 응답 포맷")
    void getMessages_returnsOk() throws Exception {
        given(chatService.getMessages(any(), anyLong(), any(), anyInt()))
                .willReturn(ChatFixtureFactory.sampleMessageListResponse(3001L));

        mockMvc.perform(get("/api/chat/rooms/{roomId}/messages", 3001L)
                        .param("cursor", "7001")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200_1"))
                .andExpect(jsonPath("$.message").value("정상적인 요청입니다."))
                .andExpect(jsonPath("$.result.messages", hasSize(6)))
                .andExpect(jsonPath("$.result.messages[0].messageId").value(7006))
                .andExpect(jsonPath("$.result.messages[0].senderType").value("SYSTEM"))
                .andExpect(jsonPath("$.result.messages[0].messageType").value("SYSTEM"))
                .andExpect(jsonPath("$.result.messages[0].attachment").doesNotExist())
                .andExpect(jsonPath("$.result.messages[0].systemMessage.kind").value("MATCHED_CAMPAIGN_CARD"))
                .andExpect(jsonPath("$.result.messages[1].messageId").value(7005))
                .andExpect(jsonPath("$.result.messages[1].senderType").value("SYSTEM"))
                .andExpect(jsonPath("$.result.messages[1].messageType").value("SYSTEM"))
                .andExpect(jsonPath("$.result.messages[1].attachment").doesNotExist())
                .andExpect(jsonPath("$.result.messages[1].systemMessage.kind").value("PROPOSAL_STATUS_NOTICE"))
                .andExpect(jsonPath("$.result.messages[2].messageId").value(7004))
                .andExpect(jsonPath("$.result.messages[2].senderType").value("SYSTEM"))
                .andExpect(jsonPath("$.result.messages[2].messageType").value("SYSTEM"))
                .andExpect(jsonPath("$.result.messages[2].attachment").doesNotExist())
                .andExpect(jsonPath("$.result.messages[2].systemMessage.kind").value("PROPOSAL_CARD"))
                .andExpect(jsonPath("$.result.messages[3].messageId").value(7003))
                .andExpect(jsonPath("$.result.messages[3].senderType").value("USER"))
                .andExpect(jsonPath("$.result.messages[3].messageType").value("FILE"))
                .andExpect(jsonPath("$.result.messages[3].attachment.attachmentId").value(9002))
                .andExpect(jsonPath("$.result.messages[3].systemMessage").doesNotExist())
                .andExpect(jsonPath("$.result.messages[4].messageId").value(7002))
                .andExpect(jsonPath("$.result.messages[4].senderType").value("USER"))
                .andExpect(jsonPath("$.result.messages[4].messageType").value("IMAGE"))
                .andExpect(jsonPath("$.result.messages[4].attachment.attachmentId").value(9001))
                .andExpect(jsonPath("$.result.messages[4].systemMessage").doesNotExist())
                .andExpect(jsonPath("$.result.messages[5].messageId").value(7001))
                .andExpect(jsonPath("$.result.messages[5].senderType").value("USER"))
                .andExpect(jsonPath("$.result.messages[5].messageType").value("TEXT"))
                .andExpect(jsonPath("$.result.messages[5].attachment").doesNotExist())
                .andExpect(jsonPath("$.result.messages[5].systemMessage").doesNotExist())
                .andExpect(jsonPath("$.result.nextCursor").value("6999"))
                .andExpect(jsonPath("$.result.hasNext", is(true)));
    }

    @Test
    @DisplayName("첨부 업로드: 200 + 공통 응답 포맷")
    void uploadAttachment_returnsOk() throws Exception {
        ChatAttachmentUploadRequest request = new ChatAttachmentUploadRequest(
                ChatAttachmentType.IMAGE);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "request",
                "request",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "photo.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy".getBytes()
        );

        given(chatService.uploadAttachment(any(), any(), any()))
                .willAnswer(invocation -> ChatFixtureFactory.sampleAttachmentUploadResponse(
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));

        mockMvc.perform(multipart("/api/chat/attachments")
                        .file(jsonPart)
                        .file(filePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200_1"))
                .andExpect(jsonPath("$.message").value("정상적인 요청입니다."))
                .andExpect(jsonPath("$.result.attachmentId").value(9001))
                .andExpect(jsonPath("$.result.attachmentType").value("IMAGE"))
                .andExpect(jsonPath("$.result.status").value("UPLOADED"));
    }
}
