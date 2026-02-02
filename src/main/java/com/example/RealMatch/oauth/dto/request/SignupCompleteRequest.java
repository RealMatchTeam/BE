package com.example.RealMatch.oauth.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.example.RealMatch.user.domain.entity.enums.Gender;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.entity.enums.TermName;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

public record SignupCompleteRequest(
        @Schema(description = "닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "생년월일", example = "1995-05-20")
        LocalDate birth,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @Schema(description = "회원 역할", example = "CREATOR")
        Role role,

        @ArraySchema(
                schema = @Schema(implementation = TermAgreementDto.class, description = "약관 동의 상세"),
                arraySchema = @Schema(description = "약관 동의 목록")
        )
        // Swagger UI의 'Example' 탭에 표시될 구체적인 예시 데이터
        @Schema(example = """
            [
              {"type":"AGE","agreed":true},
              {"type":"SERVICE_TERMS","agreed":true},
              {"type":"PRIVACY_COLLECTION","agreed":true},
              {"type":"PRIVACY_THIRD_PARTY","agreed":true}
            ]
            """)
        List<TermAgreementDto> terms,

        @Schema(description = "가입 목적 ID 리스트", example = "[1, 2, 3, 6]")
        List<Long> signupPurposeIds,

        @Schema(description = "관심 콘텐츠 카테고리 ID 리스트", example = "[1, 2]")
        List<Long> contentCategoryIds
) {
    public record TermAgreementDto(
            @Schema(description = "약관 이름", example = "AGE")
            TermName type,
            @Schema(description = "동의 여부", example = "true")
            boolean agreed
    ) {}
}
