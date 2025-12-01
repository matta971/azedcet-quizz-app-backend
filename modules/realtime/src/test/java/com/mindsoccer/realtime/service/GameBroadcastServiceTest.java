package com.mindsoccer.realtime.service;

import com.mindsoccer.protocol.dto.websocket.WsEventType;
import com.mindsoccer.protocol.dto.websocket.WsMessage;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameBroadcastService Tests")
class GameBroadcastServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Captor
    private ArgumentCaptor<WsMessage<?>> messageCaptor;

    private GameBroadcastService broadcastService;
    private final UUID matchId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        broadcastService = new GameBroadcastService(messagingTemplate);
    }

    @Nested
    @DisplayName("broadcastToMatch Tests")
    class BroadcastToMatchTests {

        @Test
        @DisplayName("Should send message to correct destination")
        void shouldSendToCorrectDestination() {
            WsMessage<String> message = WsMessage.of(WsEventType.TIMER_TICK, "test");

            broadcastService.broadcastToMatch(matchId, message);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    eq(message)
            );
        }
    }

    @Nested
    @DisplayName("sendToUser Tests")
    class SendToUserTests {

        @Test
        @DisplayName("Should send message to user queue")
        void shouldSendToUserQueue() {
            WsMessage<String> message = WsMessage.of(WsEventType.ERROR, "test");
            UUID userId = UUID.randomUUID();

            broadcastService.sendToUser(userId, message);

            verify(messagingTemplate).convertAndSendToUser(
                    eq(userId.toString()),
                    eq("/queue/events"),
                    eq(message)
            );
        }
    }

    @Nested
    @DisplayName("Match Events Tests")
    class MatchEventsTests {

        @Test
        @DisplayName("Should broadcast match started")
        void shouldBroadcastMatchStarted() {
            broadcastService.broadcastMatchStarted(matchId);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.MATCH_STARTED);
        }

        @Test
        @DisplayName("Should broadcast match ended with winner and scores")
        void shouldBroadcastMatchEnded() {
            UUID winnerId = UUID.randomUUID();

            broadcastService.broadcastMatchEnded(matchId, winnerId, 100, 80);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.MATCH_ENDED);
        }

        @Test
        @DisplayName("Should broadcast score update")
        void shouldBroadcastScoreUpdate() {
            broadcastService.broadcastScoreUpdate(matchId, 50, 40);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.SCORE_UPDATED);
        }
    }

    @Nested
    @DisplayName("Round Events Tests")
    class RoundEventsTests {

        @Test
        @DisplayName("Should broadcast round started")
        void shouldBroadcastRoundStarted() {
            broadcastService.broadcastRoundStarted(matchId, RoundType.CASCADE, 1);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.ROUND_STARTED);
        }

        @Test
        @DisplayName("Should broadcast round ended")
        void shouldBroadcastRoundEnded() {
            broadcastService.broadcastRoundEnded(matchId, RoundType.SMASH_A, 30, 20);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.ROUND_ENDED);
        }
    }

    @Nested
    @DisplayName("Question Events Tests")
    class QuestionEventsTests {

        @Test
        @DisplayName("Should broadcast question")
        void shouldBroadcastQuestion() {
            UUID questionId = UUID.randomUUID();

            broadcastService.broadcastQuestion(matchId, questionId, "What is 2+2?",
                    10000, TeamSide.A, 1);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.QUESTION);
        }

        @Test
        @DisplayName("Should broadcast answer result")
        void shouldBroadcastAnswerResult() {
            broadcastService.broadcastAnswerResult(matchId, playerId, TeamSide.A,
                    true, 10, "Paris");

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.ANSWER_RESULT);
        }

        @Test
        @DisplayName("Should broadcast question timeout")
        void shouldBroadcastQuestionTimeout() {
            broadcastService.broadcastQuestionTimeout(matchId, "Correct Answer");

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.QUESTION_TIMEOUT);
        }
    }

    @Nested
    @DisplayName("Buzzer Events Tests")
    class BuzzerEventsTests {

        @Test
        @DisplayName("Should broadcast buzzer")
        void shouldBroadcastBuzzer() {
            broadcastService.broadcastBuzzer(matchId, playerId, TeamSide.B);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.BUZZER);
        }
    }

    @Nested
    @DisplayName("Player Events Tests")
    class PlayerEventsTests {

        @Test
        @DisplayName("Should broadcast penalty")
        void shouldBroadcastPenalty() {
            broadcastService.broadcastPenalty(matchId, playerId, TeamSide.A, 3, "Faute");

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.PENALTY);
        }

        @Test
        @DisplayName("Should broadcast suspension")
        void shouldBroadcastSuspension() {
            broadcastService.broadcastSuspension(matchId, playerId, TeamSide.A, 40);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.PLAYER_SUSPENDED);
        }

        @Test
        @DisplayName("Should broadcast suspension ended")
        void shouldBroadcastSuspensionEnded() {
            broadcastService.broadcastSuspensionEnded(matchId, playerId, TeamSide.B);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.SUSPENSION_ENDED);
        }

        @Test
        @DisplayName("Should broadcast player joined")
        void shouldBroadcastPlayerJoined() {
            broadcastService.broadcastPlayerJoined(matchId, playerId, "NewPlayer", TeamSide.A);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.PLAYER_JOINED);
        }

        @Test
        @DisplayName("Should broadcast player left")
        void shouldBroadcastPlayerLeft() {
            broadcastService.broadcastPlayerLeft(matchId, playerId, TeamSide.A);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.PLAYER_LEFT);
        }
    }

    @Nested
    @DisplayName("Timer Events Tests")
    class TimerEventsTests {

        @Test
        @DisplayName("Should broadcast timer tick")
        void shouldBroadcastTimerTick() {
            broadcastService.broadcastTimerTick(matchId, 5000);

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/match/" + matchId),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.TIMER_TICK);
        }
    }

    @Nested
    @DisplayName("Error Events Tests")
    class ErrorEventsTests {

        @Test
        @DisplayName("Should send error to user")
        void shouldSendErrorToUser() {
            UUID userId = UUID.randomUUID();

            broadcastService.sendError(userId, "ERROR_CODE", "Error message");

            verify(messagingTemplate).convertAndSendToUser(
                    eq(userId.toString()),
                    eq("/queue/events"),
                    messageCaptor.capture()
            );

            WsMessage<?> captured = messageCaptor.getValue();
            assertThat(captured.type()).isEqualTo(WsEventType.ERROR);
        }
    }
}
