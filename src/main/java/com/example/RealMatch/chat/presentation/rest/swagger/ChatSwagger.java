package com.example.RealMatch.chat.presentation.rest.swagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomSort;
import com.example.RealMatch.chat.presentation.dto.enums.ChatRoomTab;
import com.example.RealMatch.chat.presentation.dto.request.ChatAttachmentUploadRequest;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatAttachmentUploadResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "chat", description = "채팅 REST API")
@RequestMapping("/api/chat")
public interface ChatSwagger {

    @Operation(summary = "채팅방 생성/조회 API By 여채현",
            description = """
                    brandId, creatorId 기준으로 1:1 채팅방을 생성하거나 기존 방을 반환합니다.
                    생성 직후 방 정보를 내려주며, 필요 시 상세 헤더는 별도 조회로 보완합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 생성/조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다.")
    })
    CustomResponse<ChatRoomCreateResponse> createOrGetRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChatRoomCreateRequest request
    );

    @Operation(summary = "채팅방 목록 조회 API By 여채현",
            description = """
                    tab/status/sort 기준으로 채팅방 목록을 조회합니다.
                    sort=LATEST는 lastMessageAt desc, roomId desc 기준으로 정렬합니다.
                    cursor는 lastMessageAt|roomId 포맷을 그대로 재사용하세요.
                    메시지가 없는 방은 목록에서 제외됩니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다.")
    })
    CustomResponse<ChatRoomListResponse> getRoomList(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) ChatRoomTab tab,
            @RequestParam(name = "status", required = false) ChatRoomFilterStatus filterStatus,
            @RequestParam(required = false) ChatRoomSort sort,
            @RequestParam(name = "cursor", required = false) RoomCursor roomCursor,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "채팅방 상세 조회 API By 여채현",
            description = """
                    채팅방 헤더에 필요한 상대 정보와 상태 값을 반환합니다.
                    태그/상태 라벨 등 UI 구성에 필요한 필드를 포함합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 상세 조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다.")
    })
    CustomResponse<ChatRoomDetailResponse> getRoomDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId
    );

    @Operation(summary = "채팅 메시지 조회 API By 여채현",
            description = """
                    messageId desc 기준으로 메시지를 조회합니다.
                    cursor는 해당 id보다 작은 메시지를 조회하는 기준입니다.
                    렌더링 기준은 messageType이며, 타입별 필드는 배타적으로 사용됩니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅 메시지 조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다.")
    })
    CustomResponse<ChatMessageListResponse> getMessages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam(name = "cursor", required = false) MessageCursor messageCursor,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "첨부 업로드 API By 여채현",
            description = """
                    첨부 파일을 업로드하고 메타 정보를 반환합니다.
                    UPLOADED 상태여도 accessUrl을 즉시 사용할 수 있습니다.
                    READY는 내부 상태값입니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "첨부 업로드 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다.")
    })
    CustomResponse<ChatAttachmentUploadResponse> uploadAttachment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("request") ChatAttachmentUploadRequest request,
            @RequestPart("file") MultipartFile file
    );
}
