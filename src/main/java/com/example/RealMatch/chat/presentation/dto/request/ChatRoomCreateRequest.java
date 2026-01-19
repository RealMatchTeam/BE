package com.example.RealMatch.chat.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequest(
        @NotNull Long brandId,
        @NotNull Long creatorId
) {
}
