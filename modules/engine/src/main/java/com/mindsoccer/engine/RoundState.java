package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.RoundType;
import java.util.Map;
import java.util.UUID;

public record RoundState(
        RoundType type,
        Phase phase,
        int questionIndex,
        UUID currentQuestionId,
        UUID activePlayerId,
        long remainingTimeMs,
        boolean finished,
        Map<String, Object> extra
) {

    public enum Phase {
        WAITING,
        ANNOUNCE,
        QUESTION_SHOWN,
        ANSWER_WINDOW,
        VALIDATING,
        TRANSITION,
        COMPLETED
    }

    public static RoundState initial(RoundType type) {
        return new RoundState(type, Phase.WAITING, 0, null, null, 0L, false, Map.of());
    }

    public RoundState withPhase(Phase newPhase) {
        return new RoundState(type, newPhase, questionIndex, currentQuestionId,
                activePlayerId, remainingTimeMs, finished, extra);
    }

    public RoundState completed() {
        return new RoundState(type, Phase.COMPLETED, questionIndex, currentQuestionId,
                activePlayerId, 0L, true, extra);
    }
}
