package com.mindsoccer.scoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RatingService Tests")
class RatingServiceTest {

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService();
    }

    @Nested
    @DisplayName("calculateNewRating Tests")
    class CalculateNewRatingTests {

        @Test
        @DisplayName("Should increase rating on win")
        void shouldIncreaseRatingOnWin() {
            int newRating = ratingService.calculateNewRating(1500, 1500, 1.0);

            assertThat(newRating).isGreaterThan(1500);
        }

        @Test
        @DisplayName("Should decrease rating on loss")
        void shouldDecreaseRatingOnLoss() {
            int newRating = ratingService.calculateNewRating(1500, 1500, 0.0);

            assertThat(newRating).isLessThan(1500);
        }

        @Test
        @DisplayName("Should barely change on draw between equals")
        void shouldBarelyChangeOnDrawBetweenEquals() {
            int newRating = ratingService.calculateNewRating(1500, 1500, 0.5);

            assertThat(newRating).isEqualTo(1500);
        }

        @Test
        @DisplayName("Should gain more points when underdog wins")
        void shouldGainMorePointsWhenUnderdogWins() {
            int underdogWin = ratingService.calculateNewRating(1200, 1800, 1.0);
            int favoriteWin = ratingService.calculateNewRating(1800, 1200, 1.0);

            int underdogGain = underdogWin - 1200;
            int favoriteGain = favoriteWin - 1800;

            assertThat(underdogGain).isGreaterThan(favoriteGain);
        }

        @Test
        @DisplayName("Should not go below minimum rating")
        void shouldNotGoBelowMinimumRating() {
            int newRating = ratingService.calculateNewRating(100, 2000, 0.0);

            assertThat(newRating).isGreaterThanOrEqualTo(100);
        }

        @Test
        @DisplayName("Should not go above maximum rating")
        void shouldNotGoAboveMaximumRating() {
            int newRating = ratingService.calculateNewRating(3000, 1000, 1.0);

            assertThat(newRating).isLessThanOrEqualTo(3000);
        }
    }

    @Nested
    @DisplayName("calculateExpectedScore Tests")
    class CalculateExpectedScoreTests {

        @Test
        @DisplayName("Should return 0.5 for equal ratings")
        void shouldReturnHalfForEqualRatings() {
            double expected = ratingService.calculateExpectedScore(1500, 1500);

            assertThat(expected).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Should return higher value for higher rated player")
        void shouldReturnHigherForHigherRated() {
            double expected = ratingService.calculateExpectedScore(1800, 1400);

            assertThat(expected).isGreaterThan(0.5);
        }

        @Test
        @DisplayName("Should return lower value for lower rated player")
        void shouldReturnLowerForLowerRated() {
            double expected = ratingService.calculateExpectedScore(1200, 1600);

            assertThat(expected).isLessThan(0.5);
        }

        @ParameterizedTest
        @DisplayName("Should always return value between 0 and 1")
        @CsvSource({
            "1500, 1500",
            "1000, 2000",
            "2000, 1000",
            "100, 3000",
            "3000, 100"
        })
        void shouldReturnValueBetweenZeroAndOne(int playerRating, int opponentRating) {
            double expected = ratingService.calculateExpectedScore(playerRating, opponentRating);

            assertThat(expected).isBetween(0.0, 1.0);
        }
    }

    @Nested
    @DisplayName("calculateRatingChange Tests")
    class CalculateRatingChangeTests {

        @Test
        @DisplayName("Should return positive change on win")
        void shouldReturnPositiveChangeOnWin() {
            int change = ratingService.calculateRatingChange(1500, 1500, 1.0);

            assertThat(change).isPositive();
        }

        @Test
        @DisplayName("Should return negative change on loss")
        void shouldReturnNegativeChangeOnLoss() {
            int change = ratingService.calculateRatingChange(1500, 1500, 0.0);

            assertThat(change).isNegative();
        }

        @Test
        @DisplayName("Should return zero change on expected draw")
        void shouldReturnZeroChangeOnExpectedDraw() {
            int change = ratingService.calculateRatingChange(1500, 1500, 0.5);

            assertThat(change).isZero();
        }
    }

    @Nested
    @DisplayName("calculateTeamAverageRating Tests")
    class CalculateTeamAverageRatingTests {

        @Test
        @DisplayName("Should return base rating for null array")
        void shouldReturnBaseRatingForNull() {
            int average = ratingService.calculateTeamAverageRating(null);

            assertThat(average).isEqualTo(1500);
        }

        @Test
        @DisplayName("Should return base rating for empty array")
        void shouldReturnBaseRatingForEmpty() {
            int average = ratingService.calculateTeamAverageRating(new int[0]);

            assertThat(average).isEqualTo(1500);
        }

        @Test
        @DisplayName("Should calculate correct average")
        void shouldCalculateCorrectAverage() {
            int[] ratings = {1400, 1500, 1600};
            int average = ratingService.calculateTeamAverageRating(ratings);

            assertThat(average).isEqualTo(1500);
        }

        @Test
        @DisplayName("Should handle single player")
        void shouldHandleSinglePlayer() {
            int[] ratings = {1700};
            int average = ratingService.calculateTeamAverageRating(ratings);

            assertThat(average).isEqualTo(1700);
        }
    }

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("Should return base rating of 1500")
        void shouldReturnBaseRating() {
            assertThat(ratingService.getBaseRating()).isEqualTo(1500);
        }

        @Test
        @DisplayName("Should return K-factor of 32")
        void shouldReturnKFactor() {
            assertThat(ratingService.getKFactor()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("TeamRatingResult Tests")
    class TeamRatingResultTests {

        @Test
        @DisplayName("Should calculate team rating results correctly on win")
        void shouldCalculateTeamRatingResultsOnWin() {
            int[] playerRatings = {1500, 1600};
            int opponentAvg = 1500;

            var result = RatingService.TeamRatingResult.calculate(
                    playerRatings, opponentAvg, 1.0, ratingService);

            assertThat(result.oldRatings()).containsExactly(1500, 1600);
            assertThat(result.newRatings()[0]).isGreaterThan(1500);
            assertThat(result.newRatings()[1]).isGreaterThan(1600);
            assertThat(result.changes()[0]).isPositive();
            assertThat(result.changes()[1]).isPositive();
            assertThat(result.averageChange()).isPositive();
        }

        @Test
        @DisplayName("Should calculate team rating results correctly on loss")
        void shouldCalculateTeamRatingResultsOnLoss() {
            int[] playerRatings = {1500, 1600};
            int opponentAvg = 1500;

            var result = RatingService.TeamRatingResult.calculate(
                    playerRatings, opponentAvg, 0.0, ratingService);

            assertThat(result.newRatings()[0]).isLessThan(1500);
            assertThat(result.changes()[0]).isNegative();
            assertThat(result.averageChange()).isNegative();
        }
    }
}
