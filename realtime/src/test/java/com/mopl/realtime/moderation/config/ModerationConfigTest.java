package com.mopl.realtime.moderation.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.mopl.realtime.moderation.rule.ProfanityRule;
import com.mopl.realtime.moderation.dto.RuleAction;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ModerationConfigTest {
    @Test void defaultsBindAndAiDisabledNeedsNoModelOrApiKey() {
        new ApplicationContextRunner().withUserConfiguration(ModerationConfig.class, ProfanityRule.class)
            .withPropertyValues("mopl.ai.enabled=false").run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(ModerationProperties.class);
                assertThat(context).hasSingleBean(MessageReviewProperties.class);
                assertThat(context.getBean(MessageReviewProperties.class).timeout()).isEqualTo(Duration.ofSeconds(5));
                assertThat(context).hasBean("moderationMessageExecutor");
                assertThat(context).doesNotHaveBean("moderationChatClient");
                assertThat(context.getBean(ModerationProperties.class).violationThreshold()).isEqualTo(3);
                assertThat(context.getBean(ModerationProperties.class).reviewTimeout()).isEqualTo(Duration.ofSeconds(5));
                assertThat(context.getBean(ModerationProperties.class).applyTimeout()).isEqualTo(Duration.ofSeconds(1));
                assertThat(context.getBean(ModerationProperties.class).prohibitedWords()).contains("씨발", "ㅈ같", "ㅈ까", "니애미", "니애비", "느금마", "ㅆㅂ");
                assertThat(context).hasSingleBean(ProfanityRule.class);
                assertThat(context.getBean(ProfanityRule.class).inspect("니 애미").action()).isEqualTo(RuleAction.MASK);
                assertThat(context.getBean(ProfanityRule.class).inspect("시발주자").action()).isEqualTo(RuleAction.ALLOW);
            });
    }
    @Test void messageRoutingAndTimeoutCanBeConfiguredWithoutChangingSanctionDeadline() {
        new ApplicationContextRunner().withUserConfiguration(ModerationConfig.class)
            .withPropertyValues("mopl.ai.enabled=false", "mopl.moderation.message-review.timeout=2s",
                "mopl.moderation.message-review.review-expressions[0]=확인할 표현").run(context -> {
                assertThat(context).hasNotFailed();
                var message=context.getBean(MessageReviewProperties.class);
                assertThat(message.timeout()).isEqualTo(Duration.ofSeconds(2));
                assertThat(message.reviewExpressions()).containsExactly("확인할 표현");
                assertThat(context.getBean(ModerationProperties.class).reviewTimeout()).isEqualTo(Duration.ofSeconds(5));
            });
    }
    @Test void applyTimeoutCanBeConfiguredIndependently() {
        new ApplicationContextRunner().withUserConfiguration(ModerationConfig.class)
            .withPropertyValues("mopl.ai.enabled=false", "mopl.moderation.apply-timeout=750ms").run(context -> {
                assertThat(context).hasNotFailed();
                var properties = context.getBean(ModerationProperties.class);
                assertThat(properties.applyTimeout()).isEqualTo(Duration.ofMillis(750));
                assertThat(properties.reviewTimeout()).isEqualTo(Duration.ofSeconds(5));
            });
    }
}
