package com.example.RealMatch.user.presentation.dto.response;

import java.util.Arrays;
import java.util.List;

import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

import lombok.Builder;

@Builder
public record MyLoginResponseDto(
        List<SocialLoginInfo> result
) {
    @Builder
    public record SocialLoginInfo(
            AuthProvider provider,
            boolean isLinked
    ) {
        public static SocialLoginInfo of(AuthProvider provider, boolean isLinked) {
            return SocialLoginInfo.builder()
                    .provider(provider)
                    .isLinked(isLinked)
                    .build();
        }
    }

    public static MyLoginResponseDto from(List<AuthProvider> linkedProviders) {
        java.util.Set<AuthProvider> linkedProvidersSet = new java.util.HashSet<>(linkedProviders);
        List<SocialLoginInfo> socialLoginInfos = Arrays.stream(AuthProvider.values())
                .map(provider -> SocialLoginInfo.of(
                        provider,
                        linkedProvidersSet.contains(provider)
                ))
                .toList();

        return MyLoginResponseDto.builder()
                .result(socialLoginInfos)
                .build();
    }
}
