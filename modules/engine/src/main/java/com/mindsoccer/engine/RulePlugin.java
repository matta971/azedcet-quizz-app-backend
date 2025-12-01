package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.RoundType;
import java.time.Duration;

public interface RulePlugin {

    RoundType type();

    RoundState init(MatchContext ctx);

    RoundState onTick(MatchContext ctx, Duration dt);

    RoundState onAnswer(MatchContext ctx, AnswerPayload payload);

    void applyScoring(MatchContext ctx);
}
