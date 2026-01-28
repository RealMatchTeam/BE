package com.example.RealMatch.oauth.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.example.RealMatch.user.domain.entity.enums.Gender;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.entity.enums.TermName;

public record SignupCompleteRequest(
        String nickname,
        LocalDate birth,
        Gender gender,
        Role role,
        List<TermAgreementDto> terms,
        List<Long> signupPurposeIds,
        List<Long> contentCategoryIds
) {
    public record TermAgreementDto(
            TermName type,
            boolean agreed
    ) {}
}
