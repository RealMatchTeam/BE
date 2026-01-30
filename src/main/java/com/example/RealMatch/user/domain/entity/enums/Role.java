package com.example.RealMatch.user.domain.entity.enums;

import java.util.Arrays;

import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.global.presentation.code.GeneralErrorCode;

public enum Role {
    ADMIN,
    GUEST,
    BRAND,
    CREATOR;

    // !!! customDetails에서 role을 string에서 Role로 바꾸면 이거 없앨거임
    public static Role from(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(GeneralErrorCode.INVALID_DATA)
                );
    }
}
