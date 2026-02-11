//package com.example.RealMatch.notification.application.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import com.example.RealMatch.notification.application.service.NotificationMessageTemplateService.MessageTemplate;
//
///**
// * NotificationMessageTemplateService 단위 테스트
// */
//@DisplayName("NotificationMessageTemplateService 테스트")
//class NotificationMessageTemplateServiceTest {
//
//    private final NotificationMessageTemplateService service = new NotificationMessageTemplateService();
//
//    @Test
//    @DisplayName("PROPOSAL_RECEIVED 메시지 생성 - 브랜드명 포함")
//    void createProposalReceivedMessage_shouldContainBrandName() {
//        // when
//        MessageTemplate template = service.createProposalReceivedMessage("라운드랩");
//
//        // then
//        assertThat(template.title()).isEqualTo("새 캠페인 제안");
//        assertThat(template.body()).contains("라운드랩");
//        assertThat(template.body()).contains("새로운 캠페인 제안을 보냈어요");
//    }
//
//    @Test
//    @DisplayName("PROPOSAL_SENT 메시지 생성 - 수락 시")
//    void createProposalSentMessage_shouldContainAccepted_whenAccepted() {
//        // when
//        MessageTemplate template = service.createProposalSentMessage("라운드랩", true);
//
//        // then
//        assertThat(template.title()).isEqualTo("제안 결과");
//        assertThat(template.body()).contains("라운드랩");
//        assertThat(template.body()).contains("수락되었어요");
//    }
//
//    @Test
//    @DisplayName("PROPOSAL_SENT 메시지 생성 - 거절 시")
//    void createProposalSentMessage_shouldContainRejected_whenRejected() {
//        // when
//        MessageTemplate template = service.createProposalSentMessage("라운드랩", false);
//
//        // then
//        assertThat(template.title()).isEqualTo("제안 결과");
//        assertThat(template.body()).contains("라운드랩");
//        assertThat(template.body()).contains("거절되었어요");
//    }
//
//    @Test
//    @DisplayName("CAMPAIGN_MATCHED 메시지 생성")
//    void createCampaignMatchedMessage_shouldContainBrandName() {
//        // when
//        MessageTemplate template = service.createCampaignMatchedMessage("라운드랩");
//
//        // then
//        assertThat(template.title()).isEqualTo("캠페인 매칭");
//        assertThat(template.body()).contains("라운드랩");
//        assertThat(template.body()).contains("매칭되었어요");
//    }
//
//    @Test
//    @DisplayName("CAMPAIGN_APPLIED 메시지 생성 - 크리에이터명 포함")
//    void createCampaignAppliedMessage_shouldContainCreatorName() {
//        // when
//        MessageTemplate template = service.createCampaignAppliedMessage("크리에이터A");
//
//        // then
//        assertThat(template.title()).isEqualTo("캠페인 지원");
//        assertThat(template.body()).contains("크리에이터A");
//        assertThat(template.body()).contains("지원했어요");
//    }
//}
