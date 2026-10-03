package com.mopl.realtime.moderation.dto;

import com.mopl.core.common.enums.ModerationAction;
import java.util.List;
import java.util.Objects;

public record MessageReviewDecision(ModerationAction action, List<MaskRange> maskRanges) {
    public MessageReviewDecision {
        Objects.requireNonNull(action, "action");
        maskRanges = List.copyOf(maskRanges);
    }
}
