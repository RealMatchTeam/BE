package com.example.RealMatch.user.application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.domain.repository.MatchCampaignHistoryRepository;
import com.example.RealMatch.user.domain.entity.AuthenticationMethod;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.AuthenticationMethodRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.infrastructure.ScrapMockDataProvider;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
import com.example.RealMatch.user.presentation.dto.response.MyCampaignDetailResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyCampaignListResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyEditInfoResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyLoginResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyPageResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyProfileCardResponseDto;
import com.example.RealMatch.user.presentation.dto.response.MyScrapResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MatchCampaignHistoryRepository matchCampaignHistoryRepository;
    private final ScrapMockDataProvider scrapMockDataProvider;
    private final AuthenticationMethodRepository authenticationMethodRepository;
    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignProposalRepository campaignProposalRepository;
    private final BrandRepository brandRepository;

    public MyPageResponseDto getMyPage(Long userId) {
        // 유저 조회 (존재하지 않거나 삭제된 유저 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 매칭 검사 여부 확인 (캠페인 매칭 검사 기록 존재 여부)
        boolean hasMatchingTest = matchCampaignHistoryRepository.existsByUserId(userId);

        // DTO 변환 및 반환
        return MyPageResponseDto.from(user, hasMatchingTest);
    }

    public MyProfileCardResponseDto getMyProfileCard(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 프로필 카드를 볼 자격이 있는지 확인. GUEST 권한이거나, 매칭 테스트 기록이 없다면 예외 발생
        if (user.getRole() == Role.GUEST || !matchCampaignHistoryRepository.existsByUserId(userId)) {
            throw new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND);
        }

        return MyProfileCardResponseDto.sample(user);
    }

    public MyScrapResponseDto getMyScrap(Long userId, String type, String sort) { // QueryDsl 적용 예정
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 내 찜 조회를 볼 자격이 있는지 확인
        if (user.getRole() == Role.GUEST || !matchCampaignHistoryRepository.existsByUserId(userId)) {
            throw new CustomException(UserErrorCode.SCRAP_NOT_FOUND);
        }

        // type이 null일 경우
        if (type == null) {
            throw new CustomException(UserErrorCode.SCRAP_NOT_FOUND);
        }

        // 하드코딩된 Mock 데이터 제공자를 통해 찜 목록 조회
        return switch (type.toLowerCase()) {
            case "brand" -> {
                List<MyScrapResponseDto.BrandScrap> brandList = scrapMockDataProvider.getBrandScraps(sort, null);
                yield MyScrapResponseDto.ofBrandType(brandList);
            }
            case "campaign" -> {
                List<MyScrapResponseDto.CampaignScrap> campaignList = scrapMockDataProvider.getCampaignScraps(sort, null);
                yield MyScrapResponseDto.ofCampaignType(campaignList);
            }
            // 정의되지 않은 type이 들어올 경우
            default -> throw new CustomException(UserErrorCode.SCRAP_NOT_FOUND);
        };
    }

    public MyEditInfoResponseDto getMyEditInfo(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 유저의 모든 소셜 로그인 방법 조회
        List<AuthProvider> providers = authenticationMethodRepository.findByUserId(userId)
                .stream()
                .map(AuthenticationMethod::getProvider)
                .toList();

        // 유저 로그인 정보를 불러오지 못했을 때
        if (providers.isEmpty()) {
            throw new CustomException(UserErrorCode.SOCIAL_INFO_NOT_FOUND);
        }

        // DTO 변환 및 반환
        return MyEditInfoResponseDto.from(user, providers);
    }

    @Transactional
    public void updateMyInfo(Long userId, MyEditInfoRequestDto request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname()) &&
                !user.getNickname().equals(request.nickname())) {
            throw new CustomException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        // 정보 수정
        user.updateInfo(
                request.nickname(),
                request.address(),
                request.detailAddress()
        );
    }

    public MyLoginResponseDto getSocialLoginInfo(Long userId) {
        // 유저 조회
        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        // 해당 유저의 모든 소셜 로그인 방법 조회
        List<AuthProvider> linkedProviders = authenticationMethodRepository.findByUserId(userId)
                .stream()
                .map(AuthenticationMethod::getProvider)
                .toList();

        // DTO 변환 및 반환
        return MyLoginResponseDto.from(linkedProviders);
    }

    /**
     * 내가 진행한 캠페인 상세 조회
     */
    public MyCampaignDetailResponseDto getMyCampaignDetail(Long userId, String applicationId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 프로필 카드를 볼 자격이 있는지 확인
        if (user.getRole() == Role.GUEST || !matchCampaignHistoryRepository.existsByUserId(userId)) {
            throw new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND);
        }

        // "apply-{uuid}" 또는 "proposal-{uuid}" 파싱
        if (applicationId.startsWith("apply-")) {
            UUID applyId = UUID.fromString(applicationId.substring(6));
            CampaignApply apply = campaignApplyRepository.findById(applyId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.CAMPAIGN_APPLICATION_NOT_FOUND));

            // 본인 확인
            if (!apply.getUser().getId().equals(userId)) {
                throw new CustomException(UserErrorCode.CAMPAIGN_APPLICATION_ACCESS_DENIED);
            }
            return convertApplyToDetail(apply);
        } else if (applicationId.startsWith("proposal-")) {
            UUID proposalId = UUID.fromString(applicationId.substring(9));
            CampaignProposal proposal = campaignProposalRepository.findById(proposalId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.CAMPAIGN_APPLICATION_NOT_FOUND));

            // 본인 확인
            if (!proposal.getCreator().getId().equals(userId)) {
                throw new CustomException(UserErrorCode.CAMPAIGN_APPLICATION_ACCESS_DENIED);
            }
            return convertProposalToDetail(proposal);
        }

        throw new CustomException(UserErrorCode.CAMPAIGN_APPLICATION_NOT_FOUND);
    }

    /**
     * 내가 진행한 캠페인 목록 조회 (Apply + Proposal 통합)
     * - 한 페이지당 데이터: 3개 (고정)
     * - 정렬: 최신순/오래된순
     */
    public Page<MyCampaignListResponseDto.CampaignItem> getMyCampaigns(Long userId, int page, String sort) {
        // 1. 사용자 검증 (기존 로직 동일)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.GUEST || !matchCampaignHistoryRepository.existsByUserId(userId)) {
            throw new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND);
        }

        // 2. 전체 데이터 조회 (Apply + Proposal)
        List<MyCampaignListResponseDto.CampaignItem> applyItems =
                campaignApplyRepository.findByUserId(userId).stream()
                        .map(this::convertApplyToItem)
                        .collect(Collectors.toList());

        List<MyCampaignListResponseDto.CampaignItem> proposalItems =
                campaignProposalRepository.findByCreatorId(userId).stream()
                        .map(this::convertProposalToItem)
                        .collect(Collectors.toList());

        // 3. 리스트 통합
        List<MyCampaignListResponseDto.CampaignItem> allItems = new ArrayList<>();
        allItems.addAll(applyItems);
        allItems.addAll(proposalItems);

        // 4. 정렬 로직
        if ("oldest".equals(sort)) {
            allItems.sort(Comparator.comparing(MyCampaignListResponseDto.CampaignItem::appliedDate));
        } else {
            // 기본값: 최신순 (내림차순)
            allItems.sort(Comparator.comparing(MyCampaignListResponseDto.CampaignItem::appliedDate).reversed());
        }

        // 5. 페이징 처리 (핵심 로직)
        // 화면 요구사항: 한 페이지에 3개씩 데이터 노출
        int pageSize = 3;

        // 데이터 가져올 시작 인덱스 계산 (0페이지 -> 0, 1페이지 -> 3, 2페이지 -> 6 ...)
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allItems.size());

        List<MyCampaignListResponseDto.CampaignItem> pagedContent;

        // 범위 유효성 체크 (요청한 페이지가 전체 데이터 범위를 넘어가면 빈 리스트 반환)
        if (start >= allItems.size()) {
            pagedContent = Collections.emptyList();
        } else {
            pagedContent = allItems.subList(start, end);
        }

        // 6. PageImpl 반환
        // 프론트엔드는 이 정보를 받아서 totalPages를 확인하고, 하단에 1~4를 그릴지 5~8을 그릴지 결정합니다.
        return new PageImpl<>(
                pagedContent,
                PageRequest.of(page, pageSize),
                allItems.size()
        );
    }

// ===== Private 메서드 =====

    /**
     * 전체 페이지 수 계산
     * 1페이지: 3개
     * 2페이지부터: 4개씩
     *
     * 예시:
     * - 총 0개: 0페이지
     * - 총 1~3개: 1페이지
     * - 총 4~7개: 2페이지 (1페이지=3개, 2페이지=1~4개)
     * - 총 8~11개: 3페이지 (1페이지=3개, 2페이지=4개, 3페이지=1~4개)
     * - 총 12~15개: 4페이지
     */
    private int calculateTotalPages(int totalElements) {
        if (totalElements == 0) {
            return 0;
        }
        if (totalElements <= 3) {
            return 1;
        }

        // 첫 3개를 제외한 나머지
        int remaining = totalElements - 3;

        // 나머지를 4개씩 나눔
        int additionalPages = (int) Math.ceil((double) remaining / 4);

        return 1 + additionalPages;
    }

    /**
     * CampaignApply -> CampaignItem 변환
     */
    private MyCampaignListResponseDto.CampaignItem convertApplyToItem(CampaignApply apply) {
        Campaign campaign = apply.getCampaign();

        // Brand 정보는 campaign.createdBy로 조회 가능 (TODO)
        String brandName = "브랜드명"; // 실제로는 brandRepository.findById(campaign.getCreatedBy())

        return MyCampaignListResponseDto.CampaignItem.builder()
                .applicationId("apply-" + apply.getId().toString())
                .brandName(brandName)
                .title(campaign.getTitle())
                .status(apply.getProposalStatus().name())
                .appliedDate(apply.getCreatedAt().toLocalDate())
                .matchingRate(99)
                .build();
    }

    /**
     * CampaignProposal -> CampaignItem 변환
     */
    private MyCampaignListResponseDto.CampaignItem convertProposalToItem(CampaignProposal proposal) {
        Brand brand = proposal.getBrand();

        return MyCampaignListResponseDto.CampaignItem.builder()
                .applicationId("proposal-" + proposal.getId().toString())
                .brandName(brand != null ? brand.getBrandName() : "브랜드명")
                .title(proposal.getTitle())
                .status(proposal.getStatus().name())
                .appliedDate(proposal.getCreatedAt().toLocalDate())
                .matchingRate(brand != null ? brand.getMatchingRate() : 95)
                .build();
    }

    /**
     * CampaignApply -> 상세 DTO 변환
     */
    private MyCampaignDetailResponseDto convertApplyToDetail(CampaignApply apply) {
        Campaign campaign = apply.getCampaign();

        return MyCampaignDetailResponseDto.builder()
                .applicationId("apply-" + apply.getId().toString())
                .brandName("브랜드명") // TODO: Brand 조회
                .brandLogoUrl(null)
                .matchingRate(99)
                .status(apply.getProposalStatus().name())
                .type("APPLY")
                .campaignName(campaign.getTitle())
                .campaignDescription(campaign.getDescription())
                .benefit(MyCampaignDetailResponseDto.BenefitSection.builder()
                        .productName(campaign.getProduct())
                        .rewardAmount(campaign.getRewardAmount())
                        .build())
                .schedule(MyCampaignDetailResponseDto.ScheduleSection.builder()
                        .startDate(campaign.getStartDate())
                        .endDate(campaign.getEndDate())
                        .build())
                .additionalSections(List.of(
                        "영상 스펙: " + campaign.getVideoSpec(),
                        "선호 스킬: " + campaign.getPreferredSkills(),
                        "일정: " + campaign.getSchedule()
                ))
                .build();
    }

    /**
     * CampaignProposal -> 상세 DTO 변환
     */
    private MyCampaignDetailResponseDto convertProposalToDetail(CampaignProposal proposal) {
        Brand brand = proposal.getBrand();

        return MyCampaignDetailResponseDto.builder()
                .applicationId("proposal-" + proposal.getId().toString())
                .brandName(brand != null ? brand.getBrandName() : "브랜드명")
                .brandLogoUrl(brand != null ? brand.getLogoUrl() : null)
                .matchingRate(brand != null ? brand.getMatchingRate() : 95)
                .status(proposal.getStatus().name())
                .type("PROPOSAL")
                .campaignName(proposal.getTitle())
                .campaignDescription(proposal.getCampaignDescription())
                .benefit(MyCampaignDetailResponseDto.BenefitSection.builder()
                        .productName("협찬품") // TODO: productId로 조회
                        .rewardAmount(proposal.getRewardAmount().longValue())
                        .build())
                .schedule(MyCampaignDetailResponseDto.ScheduleSection.builder()
                        .startDate(proposal.getStartDate())
                        .endDate(proposal.getEndDate())
                        .build())
                .additionalSections(List.of())
                .build();
    }
}
