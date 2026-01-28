package com.example.RealMatch.user.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

public record MyEditInfoResponseDto(
        String name,
        String nickname,
        String address,
        String detailAddress,
        List<String> socialType
) {
    public static MyEditInfoResponseDto from(User user, List<AuthProvider> providers) {
        return new MyEditInfoResponseDto(
                user.getName(),
                user.getNickname(),
                user.getAddress(),
                user.getDetailAddress(),
                providers.stream()
                        .map(AuthProvider::name)
                        .toList()
        );
    }
}
