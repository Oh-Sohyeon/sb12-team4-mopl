package com.mopl.realtime.moderation.rule;

import com.mopl.realtime.moderation.dto.MaskRange;
import com.mopl.realtime.moderation.exception.InvalidMessageReviewResponseException;
import java.text.BreakIterator;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class MessageMasker {
    public String mask(String text, List<MaskRange> ranges) {
        if (ranges == null || ranges.isEmpty() || ranges.size() > text.length() || ranges.stream().anyMatch(Objects::isNull)) {
            throw new InvalidMessageReviewResponseException();
        }
        var ordered = ranges.stream().sorted(Comparator.comparingInt(MaskRange::start)).toList();
        BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
        iterator.setText(text);
        BitSet boundaries = new BitSet(text.length() + 1);
        for (int index = iterator.first(); index != BreakIterator.DONE; index = iterator.next()) boundaries.set(index);
        int previousEnd = 0;
        StringBuilder result = new StringBuilder();
        for (var range : ordered) {
            if (range.start() < previousEnd || range.start() < 0 || range.end() <= range.start()
                || range.end() > text.length() || !boundaries.get(range.start()) || !boundaries.get(range.end())) {
                throw new InvalidMessageReviewResponseException();
            }
            result.append(text, previousEnd, range.start()).append('*');
            previousEnd = range.end();
        }
        return result.append(text, previousEnd, text.length()).toString();
    }
}
