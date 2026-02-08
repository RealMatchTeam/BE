package com.example.RealMatch.notification.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.repository.BrandRepository;
import com.example.RealMatch.business.application.event.CampaignApplySentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalSentEvent;
import com.example.RealMatch.business.application.event.CampaignProposalStatusChangedEvent;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.enums.ProposalDirection;
import com.example.RealMatch.business.domain.enums.ProposalStatus;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.notification.domain.entity.NotificationDelivery;
import com.example.RealMatch.notification.domain.entity.enums.DeliveryStatus;
import com.example.RealMatch.notification.domain.entity.enums.NotificationKind;
import com.example.RealMatch.notification.domain.repository.NotificationDeliveryRepository;
import com.example.RealMatch.notification.domain.repository.NotificationRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.example.RealMatch.user.domain.repository.UserRepository;

/**
 * NotificationEventListener 통합 테스트
 * 실제 이벤트 발행 → 알림 생성 → Delivery 생성까지 전체 플로우 검증
 *
 * <p>주의: @TransactionalEventListener(AFTER_COMMIT)을 테스트하기 위해
 * @Transactional을 제거하고 명시적으로 트랜잭션을 커밋해야 합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NotificationEventListener 통합 테스트")
class NotificationEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationDeliveryRepository notificationDeliveryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CampaignProposalRepository campaignProposalRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("CampaignProposalSentEvent → PROPOSAL_RECEIVED 알림 생성 + PUSH Delivery 생성")
    void handleCampaignProposalSent_shouldCreateNotificationAndDelivery() {
        // given
        User creator = createTestUserInTransaction("크리에이터", Role.CREATOR);
        User brandUser = createTestUserInTransaction("브랜드유저", Role.BRAND);
        createTestBrandInTransaction("라운드랩", brandUser);

        Long proposalId = 1L;
        Long campaignId = 10L;

        CampaignProposalSentEvent event = new CampaignProposalSentEvent(
                proposalId,
                brandUser.getId(),
                creator.getId(),
                campaignId,
                "테스트 캠페인",
                "캠페인 요약",
                ProposalStatus.REVIEWING,
                ProposalDirection.BRAND_TO_CREATOR,
                false
        );

        // when - 트랜잭션 내에서 이벤트 발행 후 커밋 (AFTER_COMMIT 리스너 실행)
        executeInTransaction(() -> {
            eventPublisher.publishEvent(event);
        });

        // then - 알림 생성 확인
        List<Notification> notifications = notificationRepository.findByUserId(creator.getId(), null).getContent();
        assertThat(notifications).hasSize(1);

        Notification notification = notifications.get(0);
        assertThat(notification.getKind()).isEqualTo(NotificationKind.PROPOSAL_RECEIVED);
        assertThat(notification.getUserId()).isEqualTo(creator.getId());
        assertThat(notification.getTitle()).contains("새 캠페인 제안");
        assertThat(notification.getBody()).contains("라운드랩");
        assertThat(notification.getProposalId()).isEqualTo(proposalId);
        assertThat(notification.getCampaignId()).isEqualTo(campaignId);

        // then - Delivery 생성 확인 (PUSH만)
        NotificationDelivery delivery = notificationDeliveryRepository
                .findByNotificationIdAndChannel(notification.getId(), com.example.RealMatch.user.domain.entity.enums.NotificationChannel.PUSH)
                .orElseThrow(() -> new AssertionError("Delivery not found"));
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getChannel()).isEqualTo(com.example.RealMatch.user.domain.entity.enums.NotificationChannel.PUSH);
        assertThat(delivery.getAttemptCount()).isZero();
    }

    @Test
    @DisplayName("CampaignProposalStatusChangedEvent(MATCHED) → CAMPAIGN_MATCHED + PROPOSAL_SENT 알림 생성")
    void handleCampaignProposalStatusChanged_shouldCreateTwoNotifications_whenMatched() {
        // given
        User creator = createTestUserInTransaction("크리에이터", Role.CREATOR);
        User brandUser = createTestUserInTransaction("브랜드유저", Role.BRAND);
        Brand brand = createTestBrandInTransaction("라운드랩", brandUser);

        // CampaignProposal 엔티티 생성 (senderUserId 조회용)
        CampaignProposal proposal = createTestProposalInTransaction(creator, brand, brandUser.getId(), creator.getId());

        CampaignProposalStatusChangedEvent event = new CampaignProposalStatusChangedEvent(
                proposal.getId(),
                null, // campaignId
                brandUser.getId(),
                creator.getId(),
                ProposalStatus.MATCHED,
                creator.getId(), // actorUserId
                ProposalDirection.BRAND_TO_CREATOR
        );

        // when - 트랜잭션 내에서 이벤트 발행 후 커밋 (AFTER_COMMIT 리스너 실행)
        executeInTransaction(() -> {
            eventPublisher.publishEvent(event);
        });

        // then - 크리에이터에게 CAMPAIGN_MATCHED 알림
        List<Notification> creatorNotifications = notificationRepository.findByUserId(creator.getId(), null).getContent();
        assertThat(creatorNotifications).anyMatch(n ->
                n.getKind() == NotificationKind.CAMPAIGN_MATCHED &&
                n.getUserId().equals(creator.getId())
        );

        // then - 제안 보낸 사람(brandUser)에게 PROPOSAL_SENT 알림
        List<Notification> brandNotifications = notificationRepository.findByUserId(brandUser.getId(), null).getContent();
        assertThat(brandNotifications).anyMatch(n ->
                n.getKind() == NotificationKind.PROPOSAL_SENT &&
                n.getUserId().equals(brandUser.getId())
        );

        // then - CAMPAIGN_MATCHED는 PUSH + EMAIL Delivery 생성
        Notification matchedNotification = creatorNotifications.stream()
                .filter(n -> n.getKind() == NotificationKind.CAMPAIGN_MATCHED)
                .findFirst()
                .orElseThrow();

        List<NotificationDelivery> matchedDeliveries = notificationDeliveryRepository.findAll().stream()
                .filter(d -> d.getNotificationId().equals(matchedNotification.getId()))
                .toList();
        assertThat(matchedDeliveries).hasSize(2); // PUSH + EMAIL
    }

    @Test
    @DisplayName("CampaignApplySentEvent → CAMPAIGN_APPLIED 알림 생성")
    void handleCampaignApplySent_shouldCreateNotification() {
        // given
        User creator = createTestUserInTransaction("크리에이터", Role.CREATOR);
        User brandUser = createTestUserInTransaction("브랜드유저", Role.BRAND);

        Long applyId = 3L;
        Long campaignId = 30L;

        CampaignApplySentEvent event = new CampaignApplySentEvent(
                applyId,
                campaignId,
                creator.getId(),
                brandUser.getId(),
                "테스트 캠페인",
                "캠페인 설명",
                "지원 사유"
        );

        // when - 트랜잭션 내에서 이벤트 발행 후 커밋 (AFTER_COMMIT 리스너 실행)
        executeInTransaction(() -> {
            eventPublisher.publishEvent(event);
        });

        // then
        List<Notification> notifications = notificationRepository.findByUserId(brandUser.getId(), null).getContent();
        assertThat(notifications).hasSize(1);

        Notification notification = notifications.get(0);
        assertThat(notification.getKind()).isEqualTo(NotificationKind.CAMPAIGN_APPLIED);
        assertThat(notification.getUserId()).isEqualTo(brandUser.getId());
        assertThat(notification.getBody()).contains("지원했어요");
    }

    @Test
    @DisplayName("멱등성: 동일 이벤트 재발행 시 중복 알림 생성 안됨")
    void handleEvent_shouldNotCreateDuplicate_whenSameEventPublishedTwice() {
        // given
        User creator = createTestUserInTransaction("크리에이터", Role.CREATOR);
        User brandUser = createTestUserInTransaction("브랜드유저", Role.BRAND);
        createTestBrandInTransaction("라운드랩", brandUser);

        CampaignProposalSentEvent event = new CampaignProposalSentEvent(
                100L,
                brandUser.getId(),
                creator.getId(),
                1000L,
                "테스트",
                "요약",
                ProposalStatus.REVIEWING,
                ProposalDirection.BRAND_TO_CREATOR,
                false
        );

        // when - 동일 이벤트 2번 발행 (각각 트랜잭션 커밋)
        executeInTransaction(() -> {
            eventPublisher.publishEvent(event);
        });
        int firstCount = notificationRepository.findByUserId(creator.getId(), null).getContent().size();

        executeInTransaction(() -> {
            eventPublisher.publishEvent(event);
        });
        int secondCount = notificationRepository.findByUserId(creator.getId(), null).getContent().size();

        // then - 알림은 1개만 생성 (멱등성 보장)
        assertThat(firstCount).isEqualTo(1);
        assertThat(secondCount).isEqualTo(1);
    }

    // ==================== 헬퍼 메서드 ====================

    /**
     * 트랜잭션 내에서 작업을 실행하고 커밋합니다.
     * @TransactionalEventListener(AFTER_COMMIT) 리스너가 실행되도록 보장합니다.
     */
    private void executeInTransaction(Runnable task) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            task.run();
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    /**
     * 트랜잭션 내에서 User를 생성하고 커밋합니다.
     */
    private User createTestUserInTransaction(String nickname, Role role) {
        User[] result = new User[1];
        executeInTransaction(() -> {
            User user = User.builder()
                    .name("테스트유저")
                    .nickname(nickname)
                    .email(nickname + "@example.com") // 고유한 이메일
                    .role(role)
                    .build();
            result[0] = userRepository.save(user);
        });
        return result[0];
    }

    /**
     * 트랜잭션 내에서 Brand를 생성하고 커밋합니다.
     */
    private Brand createTestBrandInTransaction(String brandName, User user) {
        Brand[] result = new Brand[1];
        executeInTransaction(() -> {
            Brand brand = Brand.builder()
                    .brandName(brandName)
                    .industryType(com.example.RealMatch.brand.domain.entity.enums.IndustryType.BEAUTY)
                    .user(user)
                    .createdBy(user.getId())
                    .build();
            result[0] = brandRepository.save(brand);
        });
        return result[0];
    }

    /**
     * 트랜잭션 내에서 CampaignProposal을 생성하고 커밋합니다.
     */
    private CampaignProposal createTestProposalInTransaction(User creator, Brand brand, Long senderUserId, Long receiverUserId) {
        CampaignProposal[] result = new CampaignProposal[1];
        executeInTransaction(() -> {
            CampaignProposal proposal = CampaignProposal.builder()
                    .creator(creator)
                    .brand(brand)
                    .whoProposed(Role.BRAND)
                    .senderUserId(senderUserId)
                    .receiverUserId(receiverUserId)
                    .title("테스트 제안")
                    .campaignDescription("테스트 설명")
                    .rewardAmount(100000)
                    .productId(1L)
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(30))
                    .build();
            result[0] = campaignProposalRepository.save(proposal);
        });
        return result[0];
    }
}
