package com.mindsoccer.realtime.handler;

import com.mindsoccer.protocol.dto.websocket.WsEventType;
import com.mindsoccer.protocol.dto.websocket.WsMessage;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.realtime.service.GameBroadcastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameWebSocketHandler Tests")
class GameWebSocketHandlerTest {

    @Mock
    private GameBroadcastService broadcastService;

    @Mock
    private Principal principal;

    private GameWebSocketHandler handler;
    private final UUID matchId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new GameWebSocketHandler(broadcastService);
    }

    @Nested
    @DisplayName("subscribeToMatch Tests")
    class SubscribeToMatchTests {

        @Test
        @DisplayName("Should return connected message on subscription")
        void shouldReturnConnectedMessage() {
            when(principal.getName()).thenReturn(playerId.toString());

            WsMessage<?> result = handler.subscribeToMatch(matchId, principal);

            assertThat(result.type()).isEqualTo(WsEventType.CONNECTED);
        }

        @Test
        @DisplayName("Should handle null principal")
        void shouldHandleNullPrincipal() {
            WsMessage<?> result = handler.subscribeToMatch(matchId, null);

            assertThat(result.type()).isEqualTo(WsEventType.CONNECTED);
        }
    }

    @Nested
    @DisplayName("handleBuzzer Tests")
    class HandleBuzzerTests {

        @Test
        @DisplayName("Should broadcast buzzer when principal is valid")
        void shouldBroadcastBuzzerWhenPrincipalValid() {
            when(principal.getName()).thenReturn(playerId.toString());
            GameWebSocketHandler.BuzzerRequest request = new GameWebSocketHandler.BuzzerRequest(
                    TeamSide.A, System.currentTimeMillis());

            handler.handleBuzzer(matchId, request, principal);

            verify(broadcastService).broadcastBuzzer(matchId, playerId, TeamSide.A);
        }

        @Test
        @DisplayName("Should not broadcast buzzer when principal is null")
        void shouldNotBroadcastBuzzerWhenPrincipalNull() {
            GameWebSocketHandler.BuzzerRequest request = new GameWebSocketHandler.BuzzerRequest(
                    TeamSide.A, System.currentTimeMillis());

            handler.handleBuzzer(matchId, request, null);

            verifyNoInteractions(broadcastService);
        }
    }

    @Nested
    @DisplayName("handleAnswer Tests")
    class HandleAnswerTests {

        @Test
        @DisplayName("Should process answer when principal is valid")
        void shouldProcessAnswerWhenPrincipalValid() {
            when(principal.getName()).thenReturn(playerId.toString());
            GameWebSocketHandler.AnswerRequest request = new GameWebSocketHandler.AnswerRequest(
                    "Paris", UUID.randomUUID(), TeamSide.A,
                    System.currentTimeMillis(), "idempotency-key");

            // Should not throw
            assertThatNoException().isThrownBy(() ->
                    handler.handleAnswer(matchId, request, principal));
        }

        @Test
        @DisplayName("Should not process answer when principal is null")
        void shouldNotProcessAnswerWhenPrincipalNull() {
            GameWebSocketHandler.AnswerRequest request = new GameWebSocketHandler.AnswerRequest(
                    "Paris", UUID.randomUUID(), TeamSide.A,
                    System.currentTimeMillis(), "idempotency-key");

            handler.handleAnswer(matchId, request, null);

            // Should simply return without processing
            verifyNoInteractions(broadcastService);
        }
    }

    @Nested
    @DisplayName("handleThemeSelection Tests")
    class HandleThemeSelectionTests {

        @Test
        @DisplayName("Should process theme selection when principal is valid")
        void shouldProcessThemeSelectionWhenPrincipalValid() {
            when(principal.getName()).thenReturn(playerId.toString());
            GameWebSocketHandler.ThemeSelectionRequest request = new GameWebSocketHandler.ThemeSelectionRequest(
                    UUID.randomUUID(), TeamSide.A);

            assertThatNoException().isThrownBy(() ->
                    handler.handleThemeSelection(matchId, request, principal));
        }

        @Test
        @DisplayName("Should not process theme selection when principal is null")
        void shouldNotProcessThemeSelectionWhenPrincipalNull() {
            GameWebSocketHandler.ThemeSelectionRequest request = new GameWebSocketHandler.ThemeSelectionRequest(
                    UUID.randomUUID(), TeamSide.A);

            handler.handleThemeSelection(matchId, request, null);

            verifyNoInteractions(broadcastService);
        }
    }

    @Nested
    @DisplayName("handleSuspensionChoice Tests")
    class HandleSuspensionChoiceTests {

        @Test
        @DisplayName("Should process suspension choice when principal is valid")
        void shouldProcessSuspensionChoiceWhenPrincipalValid() {
            when(principal.getName()).thenReturn(playerId.toString());
            GameWebSocketHandler.SuspensionChoiceRequest request = new GameWebSocketHandler.SuspensionChoiceRequest(
                    "FOUR_QUESTIONS", TeamSide.B);

            assertThatNoException().isThrownBy(() ->
                    handler.handleSuspensionChoice(matchId, request, principal));
        }

        @Test
        @DisplayName("Should not process suspension choice when principal is null")
        void shouldNotProcessSuspensionChoiceWhenPrincipalNull() {
            GameWebSocketHandler.SuspensionChoiceRequest request = new GameWebSocketHandler.SuspensionChoiceRequest(
                    "IMMEDIATE_40", TeamSide.B);

            handler.handleSuspensionChoice(matchId, request, null);

            verifyNoInteractions(broadcastService);
        }
    }

    @Nested
    @DisplayName("handlePing Tests")
    class HandlePingTests {

        @Test
        @DisplayName("Should return pong message")
        void shouldReturnPongMessage() {
            WsMessage<?> result = handler.handlePing(principal);

            assertThat(result.type()).isEqualTo(WsEventType.PONG);
        }
    }

    @Nested
    @DisplayName("Request Records Tests")
    class RequestRecordsTests {

        @Test
        @DisplayName("BuzzerRequest should have correct fields")
        void buzzerRequestShouldHaveCorrectFields() {
            long timestamp = System.currentTimeMillis();
            GameWebSocketHandler.BuzzerRequest request = new GameWebSocketHandler.BuzzerRequest(
                    TeamSide.A, timestamp);

            assertThat(request.team()).isEqualTo(TeamSide.A);
            assertThat(request.clientTimestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("AnswerRequest should have correct fields")
        void answerRequestShouldHaveCorrectFields() {
            UUID questionId = UUID.randomUUID();
            long timestamp = System.currentTimeMillis();
            String idempotencyKey = "key-123";

            GameWebSocketHandler.AnswerRequest request = new GameWebSocketHandler.AnswerRequest(
                    "Paris", questionId, TeamSide.B, timestamp, idempotencyKey);

            assertThat(request.answer()).isEqualTo("Paris");
            assertThat(request.questionId()).isEqualTo(questionId);
            assertThat(request.team()).isEqualTo(TeamSide.B);
            assertThat(request.clientTimestamp()).isEqualTo(timestamp);
            assertThat(request.idempotencyKey()).isEqualTo(idempotencyKey);
        }

        @Test
        @DisplayName("ThemeSelectionRequest should have correct fields")
        void themeSelectionRequestShouldHaveCorrectFields() {
            UUID themeId = UUID.randomUUID();
            GameWebSocketHandler.ThemeSelectionRequest request = new GameWebSocketHandler.ThemeSelectionRequest(
                    themeId, TeamSide.A);

            assertThat(request.themeId()).isEqualTo(themeId);
            assertThat(request.team()).isEqualTo(TeamSide.A);
        }

        @Test
        @DisplayName("SuspensionChoiceRequest should have correct fields")
        void suspensionChoiceRequestShouldHaveCorrectFields() {
            GameWebSocketHandler.SuspensionChoiceRequest request = new GameWebSocketHandler.SuspensionChoiceRequest(
                    "FOUR_QUESTIONS", TeamSide.B);

            assertThat(request.choice()).isEqualTo("FOUR_QUESTIONS");
            assertThat(request.team()).isEqualTo(TeamSide.B);
        }
    }
}
