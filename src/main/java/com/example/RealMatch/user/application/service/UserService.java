package com.example.RealMatch.user.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.business.domain.entity.CampaignApply;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.repository.CampaignApplyRepository;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.global.exception.CustomException;
import com.example.RealMatch.match.domain.repository.MatchCampaignHistoryRepository;
import com.example.RealMatch.user.domain.entity.AuthenticationMethod;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.UserMatchingDetail;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.AuthenticationMethodRepository;
import com.example.RealMatch.user.domain.repository.UserContentCategoryRepository;
import com.example.RealMatch.user.domain.repository.UserMatchingDetailRepository;
import com.example.RealMatch.user.domain.repository.UserRepository;
import com.example.RealMatch.user.infrastructure.ScrapMockDataProvider;
import com.example.RealMatch.user.presentation.code.UserErrorCode;
import com.example.RealMatch.user.presentation.dto.request.MyEditInfoRequestDto;
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
    private final UserMatchingDetailRepository userMatchingDetailRepository;
    private final CampaignApplyRepository campaignApplyRepository;
    private final CampaignProposalRepository campaignProposalRepository;
    private final UserContentCategoryRepository userContentCategoryRepository;

    public MyPageResponseDto getMyPage(Long userId) {
        // 유저 조회 (존재하지 않거나 삭제된 유저 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 매칭 검사 여부 확인 (캠페인 매칭 검사 기록 존재 여부)
        boolean hasMatchingTest = matchCampaignHistoryRepository.existsByUserId(userId);

        // DTO 변환 및 반환
        return MyPageResponseDto.from(user, hasMatchingTest);
    }

    // 파라미터 cursor를 page로 변경
    public MyProfileCardResponseDto getMyProfileCard(Long userId, int page, int size) {

        if (page < 0) {
            throw new CustomException(UserErrorCode.INVALID_PAGE_INDEX);
        }
        if (size <= 0) {
            throw new CustomException(UserErrorCode.INVALID_PAGE_SIZE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.GUEST || !matchCampaignHistoryRepository.existsByUserId(userId)) {
            throw new CustomException(UserErrorCode.PROFILE_CARD_NOT_FOUND);
        }

        UserMatchingDetail detail = userMatchingDetailRepository
                .findByUserIdAndIsDeprecatedFalse(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_MATCHING_DETAIL_NOT_FOUND));

        // 2️⃣ 모든 데이터 수집 (개선된 private 메서드 사용)
        List<MyProfileCardResponseDto.CampaignInfo> allCampaigns = new ArrayList<>();
        getAppliedCampaigns(userId, allCampaigns);
        getReceivedProposals(userId, allCampaigns);
        getSentProposals(userId, allCampaigns);

        // 3️⃣ 정렬 및 페이징 (메모리 방식 유지 - 데이터 100건 내외면 충분)
        allCampaigns.sort(Comparator.comparing(MyProfileCardResponseDto.CampaignInfo::endDate).reversed());

        int totalElements = allCampaigns.size();
        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);

        List<MyProfileCardResponseDto.CampaignInfo> pagedCampaigns = (start < totalElements)
                ? allCampaigns.subList(start, end)
                : Collections.emptyList();

        List<String> interests = userContentCategoryRepository.findByUserId(userId)
                .stream().map(uc -> uc.getContentCategory().getCategoryName()).toList();

        return MyProfileCardResponseDto.from(user, detail, interests, pagedCampaigns, totalElements, size);
    }

    private void getAppliedCampaigns(Long userId, List<MyProfileCardResponseDto.CampaignInfo> campaigns) {
        // 전체 조회를 위해 status, startDate, endDate 위치에 null 전달
        List<CampaignApply> applies = campaignApplyRepository.findMyApplies(
                userId,
                null,
                null,
                null
        );

        for (CampaignApply apply : applies) {
            Campaign c = apply.getCampaign();
            Brand b = c.getBrand(); // JOIN FETCH 덕분에 추가 쿼리 없이 즉시 접근

            campaigns.add(new MyProfileCardResponseDto.CampaignInfo(
                    c.getId(),
                    b.getId(),
                    b.getBrandName(),
                    c.getTitle(),
                    "APPLIED",
                    apply.getApplyStatus().name(),
                    c.getRecruitEndDate().toLocalDate()
            ));
        }
    }

    private void getReceivedProposals(Long userId, List<MyProfileCardResponseDto.CampaignInfo> campaigns) {
        List<Long> ids = campaignProposalRepository.findReceivedProposalIds(userId, null);
        if (ids.isEmpty()) {
            return;
        }

        // ✅ FETCH JOIN으로 N+1 방지
        List<CampaignProposal> proposals = campaignProposalRepository.findAllByIdWithDetails(ids);
        for (CampaignProposal p : proposals) {
            campaigns.add(convertToCampaignInfo(p, "RECEIVED"));
        }
    }

    private void getSentProposals(Long userId, List<MyProfileCardResponseDto.CampaignInfo> campaigns) {
        List<Long> ids = campaignProposalRepository.findSentProposalIds(userId, null);
        if (ids.isEmpty()) {
            return;
        }

        // ✅ FETCH JOIN으로 N+1 방지
        List<CampaignProposal> proposals = campaignProposalRepository.findAllByIdWithDetails(ids);
        for (CampaignProposal p : proposals) {
            campaigns.add(convertToCampaignInfo(p, "SENT"));
        }
    }

    private MyProfileCardResponseDto.CampaignInfo convertToCampaignInfo(CampaignProposal p, String type) {
        boolean hasCampaign = p.getCampaign() != null;
        Long campaignId = hasCampaign ? p.getCampaign().getId() : null;

        // 브랜드 정보 추출 (Campaign이 있으면 거기서, 없으면 Proposal에서)
        Brand brand = hasCampaign ? p.getCampaign().getBrand() : p.getBrand();
        String title = hasCampaign ? p.getCampaign().getTitle() : p.getTitle();
        LocalDate endDate = hasCampaign ? p.getCampaign().getRecruitEndDate().toLocalDate() : p.getEndDate();

        return new MyProfileCardResponseDto.CampaignInfo(
                campaignId,
                brand.getId(),
                brand.getBrandName(),
                title,
                type,
                p.getStatus().name(),
                endDate
        );
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

        // 닉네임 형식 체크 (한글, 영문, 숫자만)
        if (!request.nickname().matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new CustomException(UserErrorCode.INVALID_NICKNAME_FORMAT);
        }

        // 닉네임 길이 체크 (2~10자)
        if (request.nickname().length() < 2 || request.nickname().length() > 10) {
            throw new CustomException(UserErrorCode.INVALID_NICKNAME_LENGTH);
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
}
