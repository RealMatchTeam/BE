package com.example.RealMatch.brand.presentation.dto.response;

import java.util.List;

import com.example.RealMatch.brand.domain.entity.BrandAvailableSponsor;
import com.example.RealMatch.brand.domain.entity.BrandSponsorImage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "협찬 가능 제품 리스트 응답 DTO")
public class SponsorProductListResponseDto {

    @Schema(description = "협찬 제품 ID")
    private Long id;

    @Schema(description = "제품명")
    private String name;

    @Schema(description = "제품 대표 이미지 URL")
    private String thumbnailImageUrl;

    @Schema(description = "총 모집 인원 (또는 제공 가능 수량)")
    private Integer totalCount;

    @Schema(description = "현재 신청 인원 (또는 소진 수량)")
    private Integer currentCount;

    public static SponsorProductListResponseDto from(BrandAvailableSponsor sponsor) {
        // 이미지가 있다면 첫 번째 이미지를 썸네일로 사용
        String thumbnail = null;
        List<BrandSponsorImage> images = sponsor.getImages();
        if (images != null && !images.isEmpty()) {
            thumbnail = images.get(0).getImageUrl();
        }

        return SponsorProductListResponseDto.builder()
                .id(sponsor.getId())
                .name(sponsor.getName())
                .thumbnailImageUrl(thumbnail)
                .totalCount(sponsor.getTotalCount()) // 엔티티 필드명에 맞춰 조정 필요 (예: quantity, capacity 등)
                .currentCount(sponsor.getCurrentCount()) // 엔티티 필드명에 맞춰 조정 필요
                .build();
    }
}
