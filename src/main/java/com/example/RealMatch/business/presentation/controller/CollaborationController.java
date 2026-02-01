package com.example.RealMatch.business.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.RealMatch.business.application.service.CollaborationQueryService;
import com.example.RealMatch.business.domain.enums.CollaborationType;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.presentation.dto.response.CollaborationResponse;
import com.example.RealMatch.business.presentation.swagger.CollaborationSwagger;
import com.example.RealMatch.global.config.jwt.CustomUserDetails;
import com.example.RealMatch.global.presentation.CustomResponse;
import com.example.RealMatch.global.presentation.code.GeneralSuccessCode;
import com.example.RealMatch.user.domain.entity.enums.Role;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Business", description = "비즈니스 API")
@RestController
@RequestMapping("/api/v1/campaigns/collaborations")
@RequiredArgsConstructor
public class CollaborationController implements CollaborationSwagger {

    private final CollaborationQueryService collaborationQueryService;

    @Override
    @GetMapping("/me")
    public CustomResponse<List<CollaborationResponse>> getMyCollaborations(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) CollaborationType type,
            @RequestParam(required = false) ProposalStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return CustomResponse.onSuccess(GeneralSuccessCode.FOUND,
                collaborationQueryService.getMyCollaborations(
                        principal.getUserId(),
                        Role.from(principal.getRole()),
                        type,
                        status,
                        startDate,
                        endDate
                )
        );
    }
}
