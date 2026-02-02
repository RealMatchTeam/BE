package com.example.RealMatch.chat.presentation.rest.swagger;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.chat.application.conversion.MessageCursor;
import com.example.RealMatch.chat.application.conversion.RoomCursor;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.presentation.dto.request.ChatRoomCreateRequest;
import com.example.RealMatch.chat.presentation.dto.response.ChatMessageListResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomCreateResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Chat", description = "채팅 REST API")
@RequestMapping("/api/v1/chat")
public interface ChatSwagger {

    @Operation(summary = "채팅방 생성/조회 API By 여채현",
            description = """
                    brandId, creatorId 기준으로 1:1 채팅방을 생성하거나 기존 방을 반환합니다.
                    생성 직후 방 정보를 내려주며, 필요 시 상세 헤더는 별도 조회로 보완합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 생성/조회 성공"),
            @ApiResponse(responseCode = "COMMON400_1", description = "잘못된 요청입니다. (요청 데이터 검증 실패)"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "CHAT400_2", description = "채팅방 생성 요청이 올바르지 않습니다. (brandId/creatorId가 null이거나 동일한 경우)"),
            @ApiResponse(responseCode = "CHAT403_2", description = "채팅방 멤버가 아닙니다. (요청한 사용자가 brandId나 creatorId가 아닌 경우)")
    })
    CustomResponse<ChatRoomCreateResponse> createOrGetRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ChatRoomCreateRequest request
    );

    @Operation(summary = "채팅방 목록 조회 API By 여채현",
            description = """
                    status 기준으로 채팅방 목록을 조회합니다.
                    search가 있으면 상대방 이름(닉네임) 또는 메시지 내용으로 검색합니다 (부분 일치, 대소문자 무시).
                    정렬은 항상 lastMessageAt desc, roomId desc 기준입니다.
                    cursor는 lastMessageAt|roomId 포맷을 그대로 재사용하세요.
                    메시지가 없는 방은 목록에서 제외됩니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다.")
    })
    CustomResponse<ChatRoomListResponse> getRoomList(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "채팅방 필터 상태 (LATEST: 최신순, COLLABORATING: 협업중)") @RequestParam(name = "status", required = false) ChatRoomFilterStatus filterStatus,
            @Parameter(description = "페이지네이션 커서 (lastMessageAt|roomId 형식)") @RequestParam(name = "cursor", required = false) RoomCursor roomCursor,
            @Parameter(description = "페이지 크기 (기본값: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "검색어 (상대방 이름 또는 메시지 내용, 미입력 시 전체 목록)") @RequestParam(name = "search", required = false) String search
    );

    @Operation(summary = "채팅방 헤더 조회 API By 여채현",
            description = """
                    채팅방 헤더에 필요한 상대 정보와 상태 값을 반환합니다.
                    협업중 여부, 협업 요약 바 등 UI 구성에 필요한 필드를 포함합니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅방 헤더 조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "CHAT404_1", description = "채팅방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "CHAT403_2", description = "채팅방 멤버가 아닙니다."),
            @ApiResponse(responseCode = "CHAT403_3", description = "이미 나간 채팅방입니다.")
    })
    CustomResponse<ChatRoomDetailResponse> getChatRoomDetailWithOpponent(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId
    );

    @Operation(summary = "채팅 메시지 조회 API By 여채현",
            description = """
                    messageId desc 기준으로 메시지를 조회합니다.
                    cursor는 해당 id보다 작은 메시지를 조회하는 기준입니다.
                    렌더링 기준은 messageType이며, 타입별 필드는 배타적으로 사용됩니다.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채팅 메시지 조회 성공"),
            @ApiResponse(responseCode = "COMMON401_1", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "CHAT404_1", description = "채팅방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "CHAT403_2", description = "채팅방 멤버가 아닙니다."),
            @ApiResponse(responseCode = "CHAT403_3", description = "이미 나간 채팅방입니다.")
    })
    CustomResponse<ChatMessageListResponse> getMessages(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Parameter(description = "페이지네이션 커서 (messageId 형식)") @RequestParam(name = "cursor", required = false) MessageCursor messageCursor,
            @Parameter(description = "페이지 크기 (기본값: 20)") @RequestParam(defaultValue = "20") int size
    );
}
