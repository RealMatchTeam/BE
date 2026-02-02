package com.example.RealMatch.business.exception;


import org.springframework.http.HttpStatus;

import com.example.RealMatch.global.presentation.code.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements BaseErrorCode {

    CAMPAIGN_APPLY_ALREADY_APPLIED(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_APPLY_400_1", "이미 지원한 캠페인입니다."),
    CAMPAIGN_APPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSINESS_CAMPAIGN_APPLY_404_1", "캠페인 지원 내역이 없습니다."),

    CAMPAIGN_PROPOSAL_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_PROPOSAL_400_1", "현재 상태에서는 캠페인 제안을 수정할 수 없습니다."),
    CAMPAIGN_PROPOSAL_BRAND_IMMUTABLE(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_PROPOSAL_400_2", "캠페인 제안의 브랜드는 변경할 수 없습니다."),
    CAMPAIGN_PROPOSAL_CREATOR_IMMUTABLE(HttpStatus.BAD_REQUEST, "BUSINESS_CREATOR_PROPOSAL_400_3", "캠페인 제안에 크리에이터는 변경할 수 없습니다."),
    CAMPAIGN_PROPOSAL_CAMPAIGN_IMMUTABLE(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_PROPOSAL_400_4", "캠페인 제안에 연결된 캠페인은 변경할 수 없습니다."),
    CAMPAIGN_PROPOSAL_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_PROPOSAL_400_5", "캠페인 제작 시작 날짜가 마감 날짜보나 늦을 수 없습니다."),
    CAMPAIGN_PROPOSAL_NOT_REVIEWING(HttpStatus.BAD_REQUEST, "BUSINESS_CAMPAIGN_PROPOSAL_400_6", "검토중인 캠페인이 아닙니다."),
    CAMPAIGN_PROPOSAL_FORBIDDEN(HttpStatus.FORBIDDEN, "BUSINESS_CAMPAIGN_PROPOSAL_403_1", "해당 캠페인 제안에 대한 권한이 없습니다."),
    CAMPAIGN_PROPOSAL_ROLE_MISMATCH(HttpStatus.FORBIDDEN, "BUSINESS_CAMPAIGN_PROPOSAL_403_2", "캠페인 제안 주체와 요청자의 역할이 일치하지 않습니다."),
    CAMPAIGN_PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSINESS_CAMPAIGN_PROPOSAL_404_2", "캠페인 제안 내역이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
