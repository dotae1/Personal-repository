package com.example.playlist.global.config;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Bean
    public Client geminiClient() {
        return new Client.Builder()
                .apiKey(apiKey)
                .build();
    }

    /** 플레이리스트 추천용 설정 (playlistTitle + songs[title, artist]) */
    @Primary
    @Bean
    public GenerateContentConfig generateContentConfig() {
        return GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText("""
                            너는 음악 큐레이터 전문가다.
                            사용자의 감정과 상황을 분석해서
                            정확한 분위기의 곡만 추천해야 한다.
                            절대 JSON 형식을 깨지 마라.
                """)))
                .temperature(0.5f)
                .topP(0.8f)
                .responseMimeType("application/json")
                .responseSchema(Schema.builder()
                        .type("object")
                        .properties(Map.of(
                                "playlistTitle", Schema.builder().type("string").build(),
                                "songs", Schema.builder()
                                        .type("array")
                                        .items(Schema.builder()
                                                .type("object")
                                                .properties(Map.of(
                                                        "title", Schema.builder().type("string").build(),
                                                        "artist", Schema.builder().type("string").build()
                                                ))
                                                .build())
                                        .build()
                        )).build()
                )
                .build();
    }

    /** 곡 수집용 설정 (songs[title, artist, searchQuery]) */
    @Bean
    public GenerateContentConfig gameCollectConfig() {
        return GenerateContentConfig.builder()
                .temperature(0.7f)
                .responseMimeType("application/json")
                .responseSchema(Schema.builder()
                        .type("object")
                        .properties(Map.of(
                                "songs", Schema.builder()
                                        .type("array")
                                        .items(Schema.builder()
                                                .type("object")
                                                .properties(Map.of(
                                                        "title", Schema.builder().type("string").build(),
                                                        "artist", Schema.builder().type("string").build(),
                                                        "searchQuery", Schema.builder().type("string").build()
                                                ))
                                                .build())
                                        .build()
                        )).build()
                )
                .build();
    }
}
