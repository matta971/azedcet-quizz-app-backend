package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.Map;

/**
 * Payload d'enchère JACKPOT.
 */
public record AuctionPayload(
        int questionIndex,
        int hintLevel,
        String currentHint,
        Map<TeamSide, Integer> pools,
        TeamSide highestBidder,
        int highestBid,
        int minNextBid,
        String phase,
        long timeoutMs
) {}
