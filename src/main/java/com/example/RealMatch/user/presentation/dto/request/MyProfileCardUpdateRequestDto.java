package com.example.RealMatch.user.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyProfileCardUpdateRequestDto {

    @Size(max = 255)
    private String profileImageUrl;
}
