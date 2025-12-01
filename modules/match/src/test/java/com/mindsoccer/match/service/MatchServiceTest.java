package com.mindsoccer.match.service;

import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.entity.PlayerEntity;
import com.mindsoccer.match.entity.TeamEntity;
import com.mindsoccer.match.repository.MatchRepository;
import com.mindsoccer.match.repository.PlayerRepository;
import com.mindsoccer.match.repository.TeamRepository;
import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.shared.exception.MatchException;
import com.mindsoccer.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService Tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    private MatchService matchService;

    @BeforeEach
    void setUp() {
        matchService = new MatchService(matchRepository, teamRepository, playerRepository);
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return match when found")
        void shouldReturnMatchWhenFound() {
            UUID id = UUID.randomUUID();
            MatchEntity match = new MatchEntity("ABC123");
            when(matchRepository.findById(id)).thenReturn(Optional.of(match));

            MatchEntity result = matchService.getById(id);

            assertThat(result.getCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("Should throw NotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(matchRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchService.getById(id))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByCode Tests")
    class GetByCodeTests {

        @Test
        @DisplayName("Should return match when found")
        void shouldReturnMatchWhenFound() {
            MatchEntity match = new MatchEntity("XYZ789");
            when(matchRepository.findByCode("XYZ789")).thenReturn(Optional.of(match));

            MatchEntity result = matchService.getByCode("xyz789");

            assertThat(result.getCode()).isEqualTo("XYZ789");
            verify(matchRepository).findByCode("XYZ789"); // Should be uppercase
        }

        @Test
        @DisplayName("Should throw when code not found")
        void shouldThrowWhenCodeNotFound() {
            when(matchRepository.findByCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> matchService.getByCode("INVALID"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createMatch Tests")
    class CreateMatchTests {

        @Test
        @DisplayName("Should create match with unique code")
        void shouldCreateMatchWithUniqueCode() {
            UUID creatorId = UUID.randomUUID();

            when(matchRepository.existsByCode(any())).thenReturn(false);
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            MatchEntity result = matchService.createMatch(creatorId, "Creator", false, false, TeamSide.A);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isNotBlank();
            assertThat(result.getCode()).hasSize(6);
            verify(matchRepository, times(2)).save(any()); // Initial save + after adding player
        }

        @Test
        @DisplayName("Should create ranked match")
        void shouldCreateRankedMatch() {
            UUID creatorId = UUID.randomUUID();

            when(matchRepository.existsByCode(any())).thenReturn(false);
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            MatchEntity result = matchService.createMatch(creatorId, "Creator", true, false, TeamSide.A);

            assertThat(result.isRanked()).isTrue();
        }

        @Test
        @DisplayName("Should create duo match")
        void shouldCreateDuoMatch() {
            UUID creatorId = UUID.randomUUID();

            when(matchRepository.existsByCode(any())).thenReturn(false);
            when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            MatchEntity result = matchService.createMatch(creatorId, "Creator", false, true, TeamSide.A);

            assertThat(result.isDuo()).isTrue();
        }
    }

    @Nested
    @DisplayName("startMatch Tests")
    class StartMatchTests {

        private MatchEntity createMatchWithPlayers(int teamACount, int teamBCount) {
            MatchEntity match = new MatchEntity("TEST01");
            TeamEntity teamA = new TeamEntity(TeamSide.A);
            TeamEntity teamB = new TeamEntity(TeamSide.B);

            for (int i = 0; i < teamACount; i++) {
                teamA.addPlayer(new PlayerEntity(UUID.randomUUID(), "PlayerA" + i));
            }
            for (int i = 0; i < teamBCount; i++) {
                teamB.addPlayer(new PlayerEntity(UUID.randomUUID(), "PlayerB" + i));
            }

            match.addTeam(teamA);
            match.addTeam(teamB);
            return match;
        }

        @Test
        @DisplayName("Should start match with sufficient players")
        void shouldStartMatchWithSufficientPlayers() {
            UUID matchId = UUID.randomUUID();
            UUID refereeId = UUID.randomUUID();
            MatchEntity match = createMatchWithPlayers(1, 1);

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchRepository.save(any())).thenReturn(match);

            MatchEntity result = matchService.startMatch(matchId, refereeId);

            assertThat(result.isPlaying()).isTrue();
            assertThat(result.getRefereeId()).isEqualTo(refereeId);
        }

        @Test
        @DisplayName("Should throw when match already started")
        void shouldThrowWhenAlreadyStarted() {
            UUID matchId = UUID.randomUUID();
            MatchEntity match = createMatchWithPlayers(1, 1);
            match.start();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.startMatch(matchId, UUID.randomUUID()))
                    .isInstanceOf(MatchException.class);
        }

        @Test
        @DisplayName("Should throw when teams incomplete")
        void shouldThrowWhenTeamsIncomplete() {
            UUID matchId = UUID.randomUUID();
            MatchEntity match = createMatchWithPlayers(1, 0); // Only team A has players

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.startMatch(matchId, UUID.randomUUID()))
                    .isInstanceOf(MatchException.class);
        }
    }

    @Nested
    @DisplayName("finishMatch Tests")
    class FinishMatchTests {

        @Test
        @DisplayName("Should finish match and determine winner")
        void shouldFinishMatchAndDetermineWinner() {
            UUID matchId = UUID.randomUUID();
            MatchEntity match = new MatchEntity("TEST02");
            TeamEntity teamA = new TeamEntity(TeamSide.A);
            TeamEntity teamB = new TeamEntity(TeamSide.B);
            match.addTeam(teamA);
            match.addTeam(teamB);
            match.start();
            match.addScoreTeamA(100);
            match.addScoreTeamB(50);

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchRepository.save(any())).thenReturn(match);

            MatchEntity result = matchService.finishMatch(matchId);

            assertThat(result.isFinished()).isTrue();
            assertThat(result.getWinnerTeamId()).isEqualTo(teamA.getId());
        }

        @Test
        @DisplayName("Should throw when match not started")
        void shouldThrowWhenNotStarted() {
            UUID matchId = UUID.randomUUID();
            MatchEntity match = new MatchEntity("TEST03");

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.finishMatch(matchId))
                    .isInstanceOf(MatchException.class);
        }
    }

    @Nested
    @DisplayName("updateScore Tests")
    class UpdateScoreTests {

        @Test
        @DisplayName("Should update team A score")
        void shouldUpdateTeamAScore() {
            UUID matchId = UUID.randomUUID();
            MatchEntity match = new MatchEntity("TEST04");

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchRepository.save(any())).thenReturn(match);

            matchService.updateScore(matchId, TeamSide.A, 25);

            assertThat(match.getScoreTeamA()).isEqualTo(25);
            verify(matchRepository).save(match);
        }

        @Test
        @DisplayName("Should update team B score")
        void shouldUpdateTeamBScore() {
            UUID matchId = UUID.randomUUID();
            MatchEntity match = new MatchEntity("TEST05");

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchRepository.save(any())).thenReturn(match);

            matchService.updateScore(matchId, TeamSide.B, 30);

            assertThat(match.getScoreTeamB()).isEqualTo(30);
        }
    }
}
