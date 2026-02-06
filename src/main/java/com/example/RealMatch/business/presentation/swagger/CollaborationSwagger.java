package com.example.RealMatch.business.presentation.swagger;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.presentation.dto.response.CollaborationResponse;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

public interface CollaborationSwagger {

    @Operation(
            summary = "나의 캠페인 조회 API by 박지영",
            description = """
                    내가 지원, 제안 보냄, 제안 받음을 한 모든 캠페인 내역을 조회합니다.
                    사용자와 연관된 캠페인을 조회할 때는 해당 api를 사용해주세요. (부족한 정보가 있다면 말해주세요. 응답에 추가하겠습니다.
                    쿼리 스트링으로 아래의 옵션들을 선택할 수 있습니다.

                    1) 참여 타입
                    - APPLIED : 지원
                    - SENT : 제안 보냄
                    - RECEIVED : 제안 받음

                    * 지원은 proposalId가 null입니다.
                    * 신규 캠페인에 제안하거나/받은 경우 campaignId가 null이고, 기존 캠페인에 제안하거나/받은 경우 campaignId가 존재합니다.

                    2) 상태
                    - REVIEWING : 검토중    
                    - MATCHED : 매칭됨    
                    - REJECTED : 거절    

                    3) 날짜
                    - startDate : 제작 시작 날짜  
                    - endDate : 제작 마감 날짜      
                    * 날짜가 겹치는 기간이 모두 조회됩니다.         
                    startDate : 2월 1일, endDate : 2월 10일 이라면, 2월 1일 ~ 2월 10일 사이의 제작기간에 해당되는 캠페인이 모두 조회됩니다.
                    """
    )
    CustomResponse<List<CollaborationResponse>> getMyCollaborations(
            @AuthenticationPrincipal CustomUserDetails principal,

            @Parameter(description = "참여 타입 (APPLIED, SENT, RECEIVED)")
            @RequestParam(required = false)
            CollaborationType type,

            @Parameter(description = "상태 (REVIEWING, MATCHED, REJECTED)")
            @RequestParam(required = false)
            ProposalStatus status,

            @Parameter(description = "제작 시작 날짜")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(description = "제작 마감 날짜")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );
}
