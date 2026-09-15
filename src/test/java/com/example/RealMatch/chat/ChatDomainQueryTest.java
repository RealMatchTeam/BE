package com.example.RealMatch.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.RealMatch.attachment.domain.entity.Attachment;
import com.example.RealMatch.attachment.domain.enums.AttachmentType;
import com.example.RealMatch.attachment.domain.enums.AttachmentUsage;
import com.example.RealMatch.attachment.infrastructure.persistence.JpaAttachmentRepository;
import com.example.RealMatch.brand.domain.entity.Brand;
import com.example.RealMatch.brand.domain.entity.enums.IndustryType;
import com.example.RealMatch.business.domain.entity.CampaignProposal;
import com.example.RealMatch.business.domain.repository.CampaignProposalRepository;
import com.example.RealMatch.campaign.domain.entity.Campaign;
import com.example.RealMatch.campaign.domain.enums.CampaignOriginType;
import com.example.RealMatch.chat.domain.entity.ChatMessage;
import com.example.RealMatch.chat.domain.entity.ChatRoom;
import com.example.RealMatch.chat.domain.entity.ChatRoomMember;
import com.example.RealMatch.chat.domain.enums.ChatMessageType;
import com.example.RealMatch.chat.domain.enums.ChatRoomFilterStatus;
import com.example.RealMatch.chat.domain.enums.ChatRoomMemberRole;
import com.example.RealMatch.chat.infrastructure.persistence.JpaChatMessageRepository;
import com.example.RealMatch.chat.infrastructure.persistence.JpaChatRoomRepository;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.Role;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
        "spring.config.location=optional:classpath:/isolated-domain-tests.yml",
        "spring.datasource.url=jdbc:h2:mem:domain_queries;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false"})
@ContextConfiguration(classes = ChatDomainQueryTest.Config.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatDomainQueryTest {
    @Configuration
    @EnableJpaAuditing
    @EntityScan("com.example.RealMatch")
    @EnableJpaRepositories(basePackageClasses = {JpaChatRoomRepository.class, CampaignProposalRepository.class, JpaAttachmentRepository.class})
    static class Config {
        @Bean JPAQueryFactory queries(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }
    @Autowired EntityManager em;
    @Autowired JpaChatRoomRepository rooms;
    @Autowired JpaChatMessageRepository messages;
    @Autowired CampaignProposalRepository proposals;
    @Autowired JpaAttachmentRepository attachments;

    @Test
    void storageKeyLookupPreservesLongKeysAndCase() {
        String key = "chat/" + "a".repeat(900) + "/File.png";
        var attachment = Attachment.createUploading(99L, AttachmentType.IMAGE, "image/png", "File.png", 8L, AttachmentUsage.CHAT);
        attachment.setStorageKey(key);
        attachments.saveAndFlush(attachment);
        em.clear();
        assertEquals(attachment.getId(), attachments.findByStorageKey(key).orElseThrow().getId());
        assertTrue(attachments.findByStorageKey(key.toLowerCase(java.util.Locale.ROOT)).isEmpty());
    }

    @Test
    void groupedUnreadAndRoomSearchStayWithinActiveMembership() {
        var room = rooms.saveAndFlush(ChatRoom.createDirectRoom("counts"));
        var other = rooms.saveAndFlush(ChatRoom.createDirectRoom("not-my-room"));
        em.persist(ChatRoomMember.create(room.getId(), 1L, ChatRoomMemberRole.CREATOR));
        var incoming = messages.saveAndFlush(ChatMessage.createUserMessage(room.getId(), 2L, ChatMessageType.TEXT, "한글검색", null, "incoming"));
        messages.saveAndFlush(ChatMessage.createUserMessage(room.getId(), 1L, ChatMessageType.TEXT, "own", null, "own"));
        var hidden = messages.saveAndFlush(ChatMessage.createUserMessage(other.getId(), 2L, ChatMessageType.TEXT, "한글검색", null, "hidden"));
        rooms.updateLastMessageIfNewer(room.getId(), incoming.getId(), incoming.getCreatedAt(), "preview", ChatMessageType.TEXT);
        rooms.updateLastMessageIfNewer(other.getId(), hidden.getId(), hidden.getCreatedAt(), "preview", ChatMessageType.TEXT);
        assertEquals(java.util.Map.of(room.getId(), 1L), rooms.countUnreadMessagesByUser(1L));
        assertEquals(1L, rooms.countTotalUnreadMessages(1L));
        assertEquals(List.of(room.getId()), rooms.findRoomsByUser(1L, ChatRoomFilterStatus.LATEST, null, 20, "글검")
                .stream().map(ChatRoom::getId).toList());
    }

    @Test
    void cleanupExcludesRetainedFilesAndDeletedUploadsStillConsumeQuota() {
        var now = LocalDateTime.now();
        var retained = Attachment.createUploading(99L, AttachmentType.IMAGE, "image/png", "kept.png", 8L, AttachmentUsage.CHAT);
        retained.setStorageKey("chat/kept.png");
        retained.ready();
        retained.retain(now);
        attachments.saveAndFlush(retained);
        var abandoned = Attachment.createUploading(99L, AttachmentType.IMAGE, "image/png", "abandoned.png", 8L, AttachmentUsage.CHAT);
        abandoned.setStorageKey("chat/abandoned.png");
        abandoned.ready();
        attachments.saveAndFlush(abandoned);
        var future = now.plusDays(2);
        assertEquals(List.of(abandoned.getId()), attachments.findCleanupCandidates(future, PageRequest.of(0, 10)));

        abandoned.softDelete();
        attachments.flush();
        assertEquals(2, attachments.countByUploaderIdAndCreatedAtAfter(99L, now.minusMinutes(1)));
        em.clear();
        assertTrue(attachments.findById(abandoned.getId()).isEmpty());
        assertTrue(attachments.findCleanupCandidates(future, PageRequest.of(0, 10)).isEmpty());
    }

    @Test
    void rejectedNewProposalDoesNotHideAnotherActiveCampaignAndCompletedCampaignDisappears() {
        User brandUser = User.builder().email("brand-query@example.com").role(Role.BRAND).build();
        User creator = User.builder().email("creator-query@example.com").role(Role.CREATOR).build();
        em.persist(brandUser); em.persist(creator);
        Brand brand = Brand.builder().user(brandUser).brandName("Brand").industryType(IndustryType.BEAUTY).build();
        em.persist(brand);
        ChatRoom room = ChatRoom.createDirectRoom("query:" + UUID.randomUUID());
        em.persist(room);
        em.persist(ChatRoomMember.create(room.getId(), brandUser.getId(), ChatRoomMemberRole.BRAND));
        em.persist(ChatRoomMember.create(room.getId(), creator.getId(), ChatRoomMemberRole.CREATOR));
        Campaign campaign = Campaign.builder().brand(brand).title("Active").description("").preferredSkills("")
                .schedule("").videoSpec("").product("").rewardAmount(1L).originType(CampaignOriginType.DIRECT)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1)).recruitStartDate(LocalDateTime.now())
                .recruitEndDate(LocalDateTime.now().plusDays(1)).quota(1).createdBy(brandUser.getId()).build();
        campaign.activate(); em.persist(campaign);
        var active = proposal(brand, creator, campaign);
        active.match(); em.persist(active);
        var rejected = proposal(brand, creator, campaign);
        rejected.reject("declined"); em.persist(rejected); em.flush();
        assertTrue(rooms.findCollaboratingRoomIds(List.of(room.getId())).contains(room.getId()));
        assertEquals(active.getId(), proposals.findActiveForRoom(room.getId(), PageRequest.of(0, 1)).getFirst().getId());
        campaign.complete(); em.flush();
        assertTrue(rooms.findCollaboratingRoomIds(List.of(room.getId())).isEmpty());
        assertTrue(proposals.findActiveForRoom(room.getId(), PageRequest.of(0, 1)).isEmpty());
    }

    @Test
    void busyRoomCannotConsumeAnotherRoomsSearchResultBudget() {
        var first = rooms.saveAndFlush(ChatRoom.createDirectRoom("search:first"));
        var second = rooms.saveAndFlush(ChatRoom.createDirectRoom("search:second"));
        var expected = messages.saveAndFlush(ChatMessage.createUserMessage(second.getId(), 2L, ChatMessageType.TEXT, "needle", null, "old"));
        for (int i = 0; i < 150; i++) {
            messages.saveAndFlush(ChatMessage.createUserMessage(first.getId(), 1L, ChatMessageType.TEXT, "needle", null, "new-" + i));
        }
        var matches = messages.findLatestMatchingMessageByRoomIds(List.of(first.getId(), second.getId()), "needle");
        assertEquals(2, matches.size());
        assertEquals(expected.getId(), matches.get(second.getId()).getId());
    }

    @Test
    void exitedMemberIsExcludedFromRoomListAndUnreadCount() {
        var room = rooms.saveAndFlush(ChatRoom.createDirectRoom("left"));
        var member = ChatRoomMember.create(room.getId(), 1L, ChatRoomMemberRole.CREATOR);
        ReflectionTestUtils.setField(member, "leftAt", LocalDateTime.now()); em.persist(member);
        var message = messages.saveAndFlush(ChatMessage.createUserMessage(room.getId(), 2L, ChatMessageType.TEXT, "message", null, "left-test"));
        rooms.updateLastMessageIfNewer(room.getId(), message.getId(), message.getCreatedAt(), "message", ChatMessageType.TEXT);
        assertEquals(0, rooms.countTotalUnreadMessages(1L));
        assertTrue(rooms.findRoomsByUser(1L, ChatRoomFilterStatus.LATEST, null, 20, null).isEmpty());
    }

    private CampaignProposal proposal(Brand brand, User creator, Campaign campaign) {
        return CampaignProposal.builder().brand(brand).creator(creator).campaign(campaign).whoProposed(Role.BRAND)
                .senderUserId(brand.getUser().getId()).receiverUserId(creator.getId()).title("Proposal")
                .campaignDescription("").rewardAmount(1).productId(1L).build();
    }
}
