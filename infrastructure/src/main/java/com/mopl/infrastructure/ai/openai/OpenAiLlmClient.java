package com.mopl.infrastructure.ai.openai;

import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mopl.infrastructure.ai.client.LlmClient;
import com.mopl.infrastructure.ai.config.AiProperties;
import com.mopl.infrastructure.ai.dto.LlmRequest;
import com.mopl.infrastructure.ai.dto.LlmResponse;
import com.mopl.infrastructure.ai.exception.AiClientException;
import com.mopl.infrastructure.ai.openai.dto.OpenAiLlmRequest;
import com.mopl.infrastructure.ai.openai.dto.OpenAiLlmResponse;

@Component
public class OpenAiLlmClient implements LlmClient {

    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 1000;

    private final RestClient openAiRestClient;
    private final AiProperties aiProperties;

    public OpenAiLlmClient(
            RestClient.Builder restClientBuilder,
            AiProperties aiProperties
    ) {
        this.openAiRestClient = restClientBuilder
                .baseUrl(aiProperties.baseUrl())
                .build();
        this.aiProperties = aiProperties;
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        validateApiKey();

        OpenAiLlmRequest openAiRequest = new OpenAiLlmRequest(
                aiProperties.llmModel(),
                request.systemPrompt(),
                request.userPrompt(),
                DEFAULT_MAX_OUTPUT_TOKENS
        );

        try {
            OpenAiLlmResponse response = openAiRestClient
                    .post()
                    .uri("/responses")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + aiProperties.apiKey()
                    )
                    .body(openAiRequest)
                    .retrieve()
                    .body(OpenAiLlmResponse.class);

            return new LlmResponse(extractText(response));
        } catch (RestClientException exception) {
            throw new AiClientException(
                    "OpenAI LLM 호출에 실패했습니다.",
                    exception
            );
        }
    }

    private String extractText(OpenAiLlmResponse response) {
        if (response == null || response.output() == null) {
            throw new AiClientException(
                    "OpenAI LLM 응답이 비어 있습니다."
            );
        }

        return response.output().stream()
                .filter(output -> "message".equals(output.type()))
                .filter(output -> output.content() != null)
                .flatMap(output -> output.content().stream())
                .filter(content -> "output_text".equals(content.type()))
                .map(OpenAiLlmResponse.Content::text)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(
                        () -> new AiClientException(
                                "OpenAI LLM 응답에서 텍스트를 찾을 수 없습니다."
                        )
                );
    }

    private void validateApiKey() {
        if (aiProperties.apiKey() == null
                || aiProperties.apiKey().isBlank()) {
            throw new AiClientException(
                    "OpenAI API Key가 설정되지 않았습니다."
            );
        }
    }
}