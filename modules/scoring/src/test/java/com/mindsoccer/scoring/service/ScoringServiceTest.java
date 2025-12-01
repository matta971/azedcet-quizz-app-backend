package com.mindsoccer.scoring.service;

import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.model.ScoreResult;
import com.mindsoccer.shared.util.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ScoringService Tests")
class ScoringServiceTest {

    private ScoringService scoringService;
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        scoringService = new ScoringService();
    }

    @Nested
    @DisplayName("calculateCorrectAnswer Tests")
    class CalculateCorrectAnswerTests {

        @Test
        @DisplayName("Should calculate correct answer points")
        void shouldCalculateCorrectAnswerPoints() {
            ScoreResult result = scoringService.calculateCorrectAnswer(
                    RoundType.CASCADE, TeamSide.HOME, playerId);

            assertThat(result.points()).isPositive();
            assertThat(result.teamSide()).isEqualTo(TeamSide.HOME);
            assertThat(result.playerId()).isEqualTo(playerId);
            assertThat(result.isPenalty()).isFalse();
        }
    }

    @Nested
    @DisplayName("calculateCascadeScore Tests")
    class CalculateCascadeScoreTests {

        @Test
        @DisplayName("Should calculate first cascade answer as 10 points")
        void shouldCalculateFirstCascadeAnswer() {
            ScoreResult result = scoringService.calculateCascadeScore(1, TeamSide.HOME, playerId);

            assertThat(result.points()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should calculate progressive cascade points")
        void shouldCalculateProgressiveCascadePoints() {
            // 1st = 10, 2nd = 15, 3rd = 20, 4th = 25, etc.
            assertThat(scoringService.calculateCascadeScore(1, TeamSide.HOME, playerId).points()).isEqualTo(10);
            assertThat(scoringService.calculateCascadeScore(2, TeamSide.HOME, playerId).points()).isEqualTo(15);
            assertThat(scoringService.calculateCascadeScore(3, TeamSide.HOME, playerId).points()).isEqualTo(20);
            assertThat(scoringService.calculateCascadeScore(4, TeamSide.HOME, playerId).points()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("calculateEstocadeScore Tests")
    class CalculateEstocadeScoreTests {

        @Test
        @DisplayName("Should calculate estocade points")
        void shouldCalculateEstocadePoints() {
            ScoreResult result = scoringService.calculateEstocadeScore(TeamSide.AWAY, playerId);

            assertThat(result.points()).isEqualTo(GameConstants.ESTOCADE_POINTS_PER_QUESTION);
            assertThat(result.teamSide()).isEqualTo(TeamSide.AWAY);
        }
    }

    @Nested
    @DisplayName("calculateRelaisBonus Tests")
    class CalculateRelaisBonusTests {

        @Test
        @DisplayName("Should award bonus for fast completion")
        void shouldAwardBonusForFastCompletion() {
            ScoreResult result = scoringService.calculateRelaisBonus(
                    TeamSide.HOME, GameConstants.RELAIS_BONUS_TIMEOUT_MS - 1000);

            assertThat(result.points()).isEqualTo(GameConstants.RELAIS_BONUS_POINTS);
            assertThat(result.isBonus()).isTrue();
        }

        @Test
        @DisplayName("Should not award bonus for slow completion")
        void shouldNotAwardBonusForSlowCompletion() {
            ScoreResult result = scoringService.calculateRelaisBonus(
                    TeamSide.HOME, GameConstants.RELAIS_BONUS_TIMEOUT_MS + 1000);

            assertThat(result.points()).isZero();
        }
    }

    @Nested
    @DisplayName("calculateIdentificationScore Tests")
    class CalculateIdentificationScoreTests {

        @Test
        @DisplayName("Should give more points for early hints")
        void shouldGiveMorePointsForEarlyHints() {
            int firstHintPoints = scoringService.calculateIdentificationScore(0, TeamSide.HOME, playerId).points();
            int lastHintPoints = scoringService.calculateIdentificationScore(3, TeamSide.HOME, playerId).points();

            assertThat(firstHintPoints).isGreaterThan(lastHintPoints);
        }
    }

    @Nested
    @DisplayName("calculateCimeBonus Tests")
    class CalculateCimeBonusTests {

        @Test
        @DisplayName("Should award high threshold bonus")
        void shouldAwardHighThresholdBonus() {
            ScoreResult result = scoringService.calculateCimeBonus(
                    GameConstants.CIME_BONUS_THRESHOLD_HIGH + 1, TeamSide.HOME);

            assertThat(result.points()).isEqualTo(GameConstants.CIME_THRESHOLD_7_BONUS);
            assertThat(result.isBonus()).isTrue();
        }

        @Test
        @DisplayName("Should award low threshold bonus")
        void shouldAwardLowThresholdBonus() {
            int answers = GameConstants.CIME_BONUS_THRESHOLD_LOW + 1;
            if (answers <= GameConstants.CIME_BONUS_THRESHOLD_HIGH) {
                ScoreResult result = scoringService.calculateCimeBonus(answers, TeamSide.HOME);
                assertThat(result.points()).isEqualTo(GameConstants.CIME_THRESHOLD_4_BONUS);
            }
        }

        @Test
        @DisplayName("Should not award bonus below threshold")
        void shouldNotAwardBonusBelowThreshold() {
            ScoreResult result = scoringService.calculateCimeBonus(
                    GameConstants.CIME_BONUS_THRESHOLD_LOW - 1, TeamSide.HOME);

            assertThat(result.points()).isZero();
        }
    }

    @Nested
    @DisplayName("calculateTirsAuButVictory Tests")
    class CalculateTirsAuButVictoryTests {

        @Test
        @DisplayName("Should calculate victory points")
        void shouldCalculateVictoryPoints() {
            ScoreResult result = scoringService.calculateTirsAuButVictory(TeamSide.HOME);

            assertThat(result.points()).isEqualTo(GameConstants.TIRS_AU_BUT_VICTORY_POINTS);
            assertThat(result.teamSide()).isEqualTo(TeamSide.HOME);
        }
    }

    @Nested
    @DisplayName("calculateJackpotScore Tests")
    class CalculateJackpotScoreTests {

        @Test
        @DisplayName("Should calculate jackpot score")
        void shouldCalculateJackpotScore() {
            ScoreResult result = scoringService.calculateJackpotScore(50, TeamSide.AWAY, playerId);

            assertThat(result.points()).isEqualTo(50);
            assertThat(result.playerId()).isEqualTo(playerId);
        }
    }

    @Nested
    @DisplayName("calculateSmashTimeoutPenalty Tests")
    class CalculateSmashTimeoutPenaltyTests {

        @Test
        @DisplayName("Should calculate timeout penalty")
        void shouldCalculateTimeoutPenalty() {
            ScoreResult result = scoringService.calculateSmashTimeoutPenalty(TeamSide.HOME, playerId);

            assertThat(result.points()).isNegative();
            assertThat(result.isPenalty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getPointsForRoundType Tests")
    class GetPointsForRoundTypeTests {

        @Test
        @DisplayName("Should return correct points for different round types")
        void shouldReturnCorrectPointsForRoundTypes() {
            assertThat(scoringService.getPointsForRoundType(RoundType.ESTOCADE))
                    .isEqualTo(GameConstants.ESTOCADE_POINTS_PER_QUESTION);

            assertThat(scoringService.getPointsForRoundType(RoundType.TIRS_AU_BUT))
                    .isEqualTo(GameConstants.TIRS_AU_BUT_VICTORY_POINTS);
        }

        @Test
        @DisplayName("Should return default points for other types")
        void shouldReturnDefaultPointsForOtherTypes() {
            assertThat(scoringService.getPointsForRoundType(RoundType.CASCADE))
                    .isEqualTo(GameConstants.DEFAULT_POINTS);
        }
    }

    @Nested
    @DisplayName("getQuestionCountForRoundType Tests")
    class GetQuestionCountForRoundTypeTests {

        @Test
        @DisplayName("Should return correct question counts")
        void shouldReturnCorrectQuestionCounts() {
            assertThat(scoringService.getQuestionCountForRoundType(RoundType.CASCADE))
                    .isEqualTo(GameConstants.CASCADE_QUESTION_COUNT);

            assertThat(scoringService.getQuestionCountForRoundType(RoundType.MARATHON))
                    .isEqualTo(GameConstants.MARATHON_QUESTION_COUNT);

            assertThat(scoringService.getQuestionCountForRoundType(RoundType.DUEL))
                    .isEqualTo(GameConstants.DUEL_QUESTION_COUNT);
        }
    }
}
