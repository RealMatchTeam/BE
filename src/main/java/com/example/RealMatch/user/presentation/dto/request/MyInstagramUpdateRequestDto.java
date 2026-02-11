package com.example.RealMatch.user.presentation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyInstagramUpdateRequestDto {

    @Size(max = 30, message = "인스타그램 아이디는 30자를 초과할 수 없습니다")
    @Pattern(regexp = "^[a-zA-Z0-9._]*$", message = "인스타그램 아이디는 영문, 숫자, ., _ 만 사용 가능합니다")
    private String snsAccount;
}
