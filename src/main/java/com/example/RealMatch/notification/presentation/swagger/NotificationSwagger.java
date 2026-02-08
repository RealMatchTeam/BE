package com.example.RealMatch.notification.presentation.swagger;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.notification.presentation.dto.response.NotificationListResponse;
import com.example.RealMatch.notification.presentation.dto.response.ReadAllResponse;
import com.example.RealMatch.notification.presentation.dto.response.UnreadCountResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface NotificationSwagger {

    @Operation(
            summary = "알림 목록 조회 API",
            description = """
                    로그인한 유저의 알림 목록을 조회합니다.
                    
                    - filter: ALL(전체), PROPOSAL(받은 제안), MATCHING(캠페인 매칭) 중 선택. 기본값 ALL.
                    - 날짜별 그룹(groups)과 미읽음 개수(unreadCount)가 함께 반환됩니다.
                    - 최신순(createdAt DESC) 정렬, offset 기반 페이징.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    })
    CustomResponse<NotificationListResponse> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "알림 단건 읽음 처리 API",
            description = """
                    특정 알림을 읽음 처리합니다.
                    
                    - 본인의 알림만 읽음 처리 가능합니다.
                    - 이미 읽은 경우에도 멱등하게 200을 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "403", description = "NOTIFICATION_403_1 - 해당 알림에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "NOTIFICATION_404_1 - 알림을 찾을 수 없음")
    })
    CustomResponse<Void> markAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    );

    @Operation(
            summary = "알림 전체 읽기 API",
            description = """
                    로그인한 유저의 미읽음 알림을 모두 읽음 처리합니다.
                    벌크 UPDATE로 처리되며, 업데이트된 건수가 반환됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 읽기 처리 성공")
    })
    CustomResponse<ReadAllResponse> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "미읽음 알림 개수 조회 API",
            description = "로그인한 유저의 미읽음 알림 총 개수를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "미읽음 개수 조회 성공")
    })
    CustomResponse<UnreadCountResponse> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
