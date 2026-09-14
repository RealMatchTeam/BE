package com.example.RealMatch.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.RealMatch.chat.application.cache.ChatCacheKeys;
import com.example.RealMatch.chat.application.cache.ChatCachePolicy;
import com.example.RealMatch.chat.application.cache.ChatCacheStore;
import com.example.RealMatch.chat.application.cache.ChatRoomDetailCache;
import com.example.RealMatch.chat.application.cache.ChatRoomListCache;
import com.example.RealMatch.chat.application.mapper.ChatRoomCardAssembler;
import com.example.RealMatch.chat.application.service.room.CampaignSummaryService;
import com.example.RealMatch.chat.application.service.room.ChatRoomMemberService;
import com.example.RealMatch.chat.application.service.room.ChatRoomQueryServiceImpl;
import com.example.RealMatch.chat.application.service.room.OpponentInfoService;
import com.example.RealMatch.chat.application.util.SystemMessagePayloadSerializer;
import com.example.RealMatch.chat.domain.repository.ChatMessageRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomMemberRepository;
import com.example.RealMatch.chat.domain.repository.ChatRoomRepository;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomDetailResponse;
import com.example.RealMatch.chat.presentation.dto.response.ChatRoomListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

class ChatCacheConsistencyTest {

    @Test
    void unavailableVersionCannotBeUsedForDetailCacheWrite() {
        ChatCacheStore store = mock(ChatCacheStore.class);
        ChatRoomDetailCache cache = new ChatRoomDetailCache(store);
        cache.put(1L, 2L, -1L, mock(ChatRoomDetailResponse.class));
        verify(store, never()).set(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void missingVersionDiffersFromFirstIncrementAndRedisFailureFallsBack() {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        ValueOperations<String, Object> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.increment("version")).thenReturn(1L);
        ChatCacheStore cache = new ChatCacheStore(redis, new ObjectMapper());

        assertEquals(0, cache.getVersion("version"));
        assertEquals(1, cache.bumpVersion("version"));
        when(values.get(anyString())).thenThrow(new DataAccessResourceFailureException("Redis down"));
        assertEquals(-1, cache.getVersion("version"));
        assertTrue(cache.get("entry", ChatRoomListResponse.class).isEmpty());
    }

    @Test
    void invalidationDuringDbLoadCannotPublishOldResultsUnderNewVersion() {
        ChatCacheStore cache = mock(ChatCacheStore.class);
        ChatRoomRepository rooms = mock(ChatRoomRepository.class);
        ChatRoomListCache listCache = new ChatRoomListCache(cache);
        String versionKey = ChatCacheKeys.roomListVersionKey(1L);
        when(cache.getVersion(versionKey)).thenReturn(0L, 1L);
        when(rooms.findRoomsByUser(any(), any(), any(), any(Integer.class), any())).thenReturn(List.of());
        ChatRoomQueryServiceImpl service = new ChatRoomQueryServiceImpl(
                rooms, mock(ChatRoomMemberRepository.class), mock(ChatMessageRepository.class),
                mock(ChatRoomMemberService.class), mock(OpponentInfoService.class), mock(CampaignSummaryService.class),
                mock(ChatRoomCardAssembler.class), listCache, new ChatRoomDetailCache(cache),
                mock(SystemMessagePayloadSerializer.class));
        when(rooms.countTotalUnreadMessages(1L)).thenAnswer(call -> {
            cache.bumpVersion(versionKey);
            return 3L;
        });

        ChatRoomListResponse response = service.getRoomList(1L, null, null, 20, null);

        verify(cache).set(ChatCacheKeys.roomListKey(1L, 0L, null, 20, null),
                response, ChatCachePolicy.ROOM_LIST_TTL);
        // The writer must not fetch the new version after loading old data.
        verify(cache).getVersion(versionKey);
    }
}
