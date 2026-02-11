package com.example.RealMatch.user.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyProfileCardUpdateRequestDto {

    @jakarta.validation.constraints.NotBlank
    private String profileImageUrl;
}
