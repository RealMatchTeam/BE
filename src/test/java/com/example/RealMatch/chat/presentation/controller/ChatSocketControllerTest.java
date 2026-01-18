package com.example.RealMatch.chat.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.example.RealMatch.chat.presentation.dto.enums.ChatMessageType;
import com.example.RealMatch.chat.presentation.dto.enums.ChatSendMessageAckStatus;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatMessageCreatedEvent;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageAck;
import com.example.RealMatch.chat.presentation.dto.websocket.ChatSendMessageCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatSocketControllerTest {
    private static final String WS_ENDPOINT = "/ws/chat";
    private static final String APP_SEND_DESTINATION = "/app/chat.send";
    private static final String ROOM_TOPIC_PREFIX = "/topic/rooms/";
    private static final String ACK_QUEUE = "/user/queue/chat.ack";
    private static final long ROOM_ID = 3001L;
    private static final String CLIENT_MESSAGE_ID = "11111111-1111-1111-1111-111111111111";
    private static final String IMAGE_CLIENT_MESSAGE_ID = "22222222-2222-2222-2222-222222222222";
    private static final String FILE_CLIENT_MESSAGE_ID = "33333333-3333-3333-3333-333333333333";
    private static final long ATTACHMENT_ID = 9001L;
    private static final int TIMEOUT_SECONDS = 3;
    private static final long SUBSCRIBE_WAIT_MILLIS = 200L;

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @Test
    @DisplayName("채팅 메시지 전송: ACK 수신")
    void sendMessage_returnsAck() throws Exception {
        StompSession session = connect();
        CompletableFuture<ChatSendMessageAck> ackFuture = new CompletableFuture<>();

        session.subscribe(ACK_QUEUE, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatSendMessageAck.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                ackFuture.complete((ChatSendMessageAck) payload);
            }
        });
        waitForSubscriptions();

        ChatSendMessageCommand command = createCommand(
                ChatMessageType.TEXT,
                "안녕하세요!",
                null,
                CLIENT_MESSAGE_ID
        );
        session.send(APP_SEND_DESTINATION, Objects.requireNonNull(command));

        ChatSendMessageAck ack = ackFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(ack.status()).isEqualTo(ChatSendMessageAckStatus.SUCCESS);
        assertThat(ack.clientMessageId()).isEqualTo(command.clientMessageId());
    }

    @Test
    @DisplayName("채팅 메시지 전송: 브로드캐스트 수신")
    void sendMessage_broadcastsMessage() throws Exception {
        StompSession session = connect();
        CompletableFuture<ChatMessageCreatedEvent> messageFuture = new CompletableFuture<>();

        session.subscribe(ROOM_TOPIC_PREFIX + ROOM_ID, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatMessageCreatedEvent.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                messageFuture.complete((ChatMessageCreatedEvent) payload);
            }
        });
        waitForSubscriptions();

        ChatSendMessageCommand command = createCommand(ChatMessageType.TEXT, "안녕하세요!", null, CLIENT_MESSAGE_ID);
        session.send(APP_SEND_DESTINATION, Objects.requireNonNull(command));

        ChatMessageCreatedEvent event = messageFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(event.roomId()).isEqualTo(ROOM_ID);
        assertThat(event.message().messageType()).isEqualTo(ChatMessageType.TEXT);
    }

    @Test
    @DisplayName("이미지 메시지 전송: 브로드캐스트 수신")
    void sendImageMessage_broadcastsMessage() throws Exception {
        StompSession session = connect();
        CompletableFuture<ChatMessageCreatedEvent> messageFuture = new CompletableFuture<>();

        session.subscribe(ROOM_TOPIC_PREFIX + ROOM_ID, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatMessageCreatedEvent.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                messageFuture.complete((ChatMessageCreatedEvent) payload);
            }
        });
        waitForSubscriptions();

        ChatSendMessageCommand command = createCommand(ChatMessageType.IMAGE, null, ATTACHMENT_ID, IMAGE_CLIENT_MESSAGE_ID);
        session.send(APP_SEND_DESTINATION, Objects.requireNonNull(command));

        ChatMessageCreatedEvent event = messageFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(event.message().messageType()).isEqualTo(ChatMessageType.IMAGE);
        assertThat(event.message().attachment()).isNotNull();
    }

    @Test
    @DisplayName("파일 메시지 전송: 브로드캐스트 수신")
    void sendFileMessage_broadcastsMessage() throws Exception {
        StompSession session = connect();
        CompletableFuture<ChatMessageCreatedEvent> messageFuture = new CompletableFuture<>();

        session.subscribe(ROOM_TOPIC_PREFIX + ROOM_ID, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatMessageCreatedEvent.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                messageFuture.complete((ChatMessageCreatedEvent) payload);
            }
        });
        waitForSubscriptions();

        ChatSendMessageCommand command = createCommand(ChatMessageType.FILE, null, ATTACHMENT_ID, FILE_CLIENT_MESSAGE_ID);
        session.send(APP_SEND_DESTINATION, Objects.requireNonNull(command));

        ChatMessageCreatedEvent event = messageFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(event.message().messageType()).isEqualTo(ChatMessageType.FILE);
        assertThat(event.message().attachment()).isNotNull();
    }

    private StompSession connect() throws Exception {
        return stompClient
                .connectAsync("ws://localhost:" + port + WS_ENDPOINT, new StompSessionHandlerAdapter() {
                })
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private @NonNull ChatSendMessageCommand createCommand(
            ChatMessageType messageType,
            String content,
            Long attachmentId,
            String clientMessageId
    ) {
        return new ChatSendMessageCommand(
                ROOM_ID,
                messageType,
                content,
                attachmentId,
                clientMessageId
        );
    }

    private void waitForSubscriptions() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(SUBSCRIBE_WAIT_MILLIS);
    }

    @TestConfiguration
    static class WebSocketTestConfig {

        @Bean
        HandshakeHandler handshakeHandler() {
            return new DefaultHandshakeHandler() {
                @Override
                protected Principal determineUser(
                        @NonNull ServerHttpRequest request,
                        @NonNull WebSocketHandler wsHandler,
                        @NonNull Map<String, Object> attributes
                ) {
                    return () -> "test-user";
                }
            };
        }
    }
}
