package com.mindsoccer.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindsoccer.api.entity.UserEntity;
import com.mindsoccer.api.repository.UserRepository;
import com.mindsoccer.api.security.JwtService;
import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.entity.PlayerEntity;
import com.mindsoccer.match.entity.TeamEntity;
import com.mindsoccer.match.repository.MatchRepository;
import com.mindsoccer.protocol.dto.request.CreateMatchRequest;
import com.mindsoccer.protocol.dto.request.JoinMatchRequest;
import com.mindsoccer.protocol.enums.TeamSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.mindsoccer.api.IntegrationTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("MatchController Integration Tests")
class MatchControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private UserEntity testUser;
    private UserEntity testUser2;
    private String accessToken;
    private String accessToken2;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserEntity("player1", "player1@example.com", passwordEncoder.encode("Password123!"));
        testUser = userRepository.save(testUser);
        accessToken = jwtService.generateAccessToken(testUser.getId(), testUser.getHandle(), testUser.getRole());

        testUser2 = new UserEntity("player2", "player2@example.com", passwordEncoder.encode("Password123!"));
        testUser2 = userRepository.save(testUser2);
        accessToken2 = jwtService.generateAccessToken(testUser2.getId(), testUser2.getHandle(), testUser2.getRole());
    }

    @Nested
    @DisplayName("GET /api/matches")
    class GetWaitingMatchesTests {

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/matches")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get("/api/matches"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/matches")
    class CreateMatchTests {

        @Test
        @DisplayName("Should create match successfully")
        void shouldCreateMatchSuccessfully() throws Exception {
            CreateMatchRequest request = new CreateMatchRequest("CLASSIC", 4, "EU", false, null);

            mockMvc.perform(post("/api/matches")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.code").isNotEmpty())
                    .andExpect(jsonPath("$.data.code", hasLength(6)))
                    .andExpect(jsonPath("$.data.status").value("WAITING"));
        }

        @Test
        @DisplayName("Should create duo match")
        void shouldCreateDuoMatch() throws Exception {
            CreateMatchRequest request = new CreateMatchRequest("CLASSIC", 2, "EU", false, null);

            mockMvc.perform(post("/api/matches")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.duo").value(true));
        }

        @Test
        @DisplayName("Should add creator to preferred team")
        void shouldAddCreatorToPreferredTeam() throws Exception {
            CreateMatchRequest request = new CreateMatchRequest("CLASSIC", 4, "EU", false, null);

            mockMvc.perform(post("/api/matches")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.teamA.players", hasSize(1)))
                    .andExpect(jsonPath("$.data.teamA.players[0].handle").value("player1"));
        }
    }

    @Nested
    @DisplayName("GET /api/matches/{id}")
    class GetMatchByIdTests {

        @Test
        @DisplayName("Should return match by id")
        void shouldReturnMatchById() throws Exception {
            // Create a match
            MatchEntity match = createTestMatch();

            mockMvc.perform(get("/api/matches/" + match.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(match.getId().toString()))
                    .andExpect(jsonPath("$.data.code").value(match.getCode()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent match")
        void shouldReturn404ForNonExistent() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/matches/" + nonExistentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/matches/code/{code}")
    class GetMatchByCodeTests {

        @Test
        @DisplayName("Should return match by code")
        void shouldReturnMatchByCode() throws Exception {
            MatchEntity match = createTestMatch();

            mockMvc.perform(get("/api/matches/code/" + match.getCode())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value(match.getCode()));
        }

        @Test
        @DisplayName("Should be case insensitive")
        void shouldBeCaseInsensitive() throws Exception {
            MatchEntity match = createTestMatch();

            mockMvc.perform(get("/api/matches/code/" + match.getCode().toLowerCase())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/matches/{id}/join")
    class JoinMatchTests {

        @Test
        @DisplayName("Should join match successfully")
        void shouldJoinMatchSuccessfully() throws Exception {
            MatchEntity match = createTestMatch();

            mockMvc.perform(post("/api/matches/" + match.getId() + "/join")
                            .header("Authorization", "Bearer " + accessToken2)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should join with preferred side")
        void shouldJoinWithPreferredSide() throws Exception {
            MatchEntity match = createTestMatch();
            JoinMatchRequest request = new JoinMatchRequest(match.getId(), TeamSide.B, null);

            mockMvc.perform(post("/api/matches/" + match.getId() + "/join")
                            .header("Authorization", "Bearer " + accessToken2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject if already participant")
        void shouldRejectIfAlreadyParticipant() throws Exception {
            MatchEntity match = createTestMatchWithPlayer(testUser.getId(), testUser.getHandle());

            mockMvc.perform(post("/api/matches/" + match.getId() + "/join")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/matches/{id}/leave")
    class LeaveMatchTests {

        @Test
        @DisplayName("Should leave match successfully")
        void shouldLeaveMatchSuccessfully() throws Exception {
            MatchEntity match = createTestMatchWithPlayer(testUser.getId(), testUser.getHandle());

            mockMvc.perform(post("/api/matches/" + match.getId() + "/leave")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    private MatchEntity createTestMatch() {
        MatchEntity match = new MatchEntity("TEST01");
        TeamEntity teamA = new TeamEntity(TeamSide.A);
        TeamEntity teamB = new TeamEntity(TeamSide.B);
        match.addTeam(teamA);
        match.addTeam(teamB);
        return matchRepository.save(match);
    }

    private MatchEntity createTestMatchWithPlayer(UUID userId, String handle) {
        MatchEntity match = new MatchEntity("TEST02");
        TeamEntity teamA = new TeamEntity(TeamSide.A);
        TeamEntity teamB = new TeamEntity(TeamSide.B);

        PlayerEntity player = new PlayerEntity(userId, handle);
        teamA.addPlayer(player);
        teamA.setCaptainId(userId);

        match.addTeam(teamA);
        match.addTeam(teamB);
        return matchRepository.save(match);
    }
}
