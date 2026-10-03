package com.mopl.realtime.moderation.review;
import static org.assertj.core.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.core.common.enums.ModerationAction;
import com.mopl.realtime.moderation.dto.MessageReviewContext;
import com.mopl.realtime.moderation.exception.InvalidMessageReviewResponseException;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
class SpringAiMessageReviewerTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    @Test void nativeStructuredResponseUsesSeparateMessagePromptWithoutSanctionTools() throws Exception {
        String content = "{\"action\":\"MASK\",\"maskRanges\":[{\"start\":3,\"end\":6}]}";
        withResponse(content,(reviewer,request) -> {
            var decision = reviewer.review(new MessageReviewContext("너는 멍청이!",List.of()));
            assertThat(decision.action()).isEqualTo(ModerationAction.MASK);
            assertThat(decision.maskRanges()).hasSize(1);
            var json = mapper.readTree(request.get());
            assertThat(json.path("response_format").path("type").asText()).isEqualTo("json_schema");
            var schema = json.path("response_format").path("json_schema");
            assertThat(schema.path("strict").asBoolean()).isTrue();
            assertThat(schema.path("schema").toString()).contains("ALLOW","MASK","BLOCK","maskRanges");
            assertThat(json.has("tools")).isFalse();
            assertThat(json.path("messages").get(1).path("content").asText()).contains("currentMessage","recentConversation");
        });
    }
    @ParameterizedTest @ValueSource(strings={"not-json", "null", "{}",
        "{\"action\":\"MASK\",\"maskRanges\":[{\"end\":1}]}",
        "{\"action\":\"MASK\",\"maskRanges\":[{\"start\":0.5,\"end\":1}]}",
        "{\"action\":\"MASK\",\"maskRanges\":[{\"start\":\"0\",\"end\":1}]}", "{\"action\":\"OTHER\",\"maskRanges\":[]}",
        "{\"action\":\"ALLOW\",\"maskRanges\":null}",
        "{\"action\":\"ALLOW\",\"maskRanges\":[],\"replacement\":\"new text\"}",
        "{\"action\":\"MASK\",\"maskRanges\":[{\"start\":null,\"end\":1}]}",
        "{\"action\":\"ALLOW\",\"maskRanges\":[]} {}"})
    void malformedOrUntrustedOutputDoesNotEscapeAsDecision(String content) throws Exception {
        withResponse(content,(reviewer,request) -> assertThatThrownBy(() -> reviewer.review(new MessageReviewContext("꺼져",List.of())))
            .isInstanceOf(InvalidMessageReviewResponseException.class).hasMessage("Untrusted message review response"));
    }
    @Test void disabledAiIsExplicitErrorForFailOpenCaller() {
        var provider = new StaticListableBeanFactory().getBeanProvider(ChatClient.class);
        assertThatThrownBy(() -> new SpringAiMessageReviewer(provider,mapper).review(new MessageReviewContext("꺼져",List.of())))
            .isInstanceOf(IllegalStateException.class);
    }
    private void withResponse(String content,Verification verification) throws Exception {
        var request = new AtomicReference<String>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/v1/chat/completions",exchange -> {
            request.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            String body = mapper.writeValueAsString(Map.of("id","stub","object","chat.completion","created",1,"model","stub",
                "choices",List.of(Map.of("index",0,"finish_reason","stop","message",Map.of("role","assistant","content",content)))));
            byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try {
            var api=OpenAiApi.builder().baseUrl("http://127.0.0.1:"+server.getAddress().getPort()).apiKey("stub-key").build();
            var client=ChatClient.create(OpenAiChatModel.builder().openAiApi(api).build());
            var provider=new StaticListableBeanFactory(Map.of("client",client)).getBeanProvider(ChatClient.class);
            verification.verify(new SpringAiMessageReviewer(provider,mapper),request);
        } finally {server.stop(0);}
    }
    @FunctionalInterface private interface Verification {
        void verify(SpringAiMessageReviewer reviewer,AtomicReference<String> request) throws Exception;
    }
}
