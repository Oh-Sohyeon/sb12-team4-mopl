package com.mopl.realtime.moderation.rule;
import static org.assertj.core.api.Assertions.*;
import com.mopl.realtime.moderation.dto.MaskRange;
import com.mopl.realtime.moderation.exception.InvalidMessageReviewResponseException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
class MessageMaskerTest {
    private final MessageMasker masker = new MessageMasker();
    @Test void masksOnlyOriginalRangesAndAcceptsUnsortedNonOverlappingRanges() {
        assertThat(masker.mask("😀 너는 멍청이!", List.of(new MaskRange(6,9)))).isEqualTo("😀 너는 *!");
        assertThat(masker.mask("abcd", List.of(new MaskRange(3,4),new MaskRange(0,1)))).isEqualTo("*bc*");
        assertThat(masker.mask("ab", List.of(new MaskRange(0,1),new MaskRange(1,2)))).isEqualTo("**");
    }
    static Stream<List<MaskRange>> invalidRanges() {
        return Stream.of(List.of(), List.of(new MaskRange(-1,1)), List.of(new MaskRange(0,5)),
            List.of(new MaskRange(1,1)), List.of(new MaskRange(3,2)),
            List.of(new MaskRange(0,2),new MaskRange(1,3)),
            List.of(new MaskRange(0,1),new MaskRange(0,1)));
    }
    @ParameterizedTest @MethodSource("invalidRanges") void rejectsInvalidRanges(List<MaskRange> ranges) {
        assertThatThrownBy(() -> masker.mask("abcd", ranges)).isInstanceOf(InvalidMessageReviewResponseException.class);
    }
    @Test void rejectsNullAndCutsThroughUnicodeCharacters() {
        assertThatThrownBy(() -> masker.mask("x", null)).isInstanceOf(InvalidMessageReviewResponseException.class);
        assertThatThrownBy(() -> masker.mask("x", java.util.Arrays.asList((MaskRange)null)))
            .isInstanceOf(InvalidMessageReviewResponseException.class);
        for (String text : List.of("😀", "e\u0301", "👨‍👩‍👧", "가")) {
            assertThatThrownBy(() -> masker.mask(text, List.of(new MaskRange(0,1))))
                .isInstanceOf(InvalidMessageReviewResponseException.class);
            assertThat(masker.mask(text, List.of(new MaskRange(0,text.length())))).isEqualTo("*");
        }
    }
}
