package com.example.playlist.gemini.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeminiRequest {
    private String prompt;
    private String category;

    @Min(1)
    @Max(30)
    private int songCount = 10;

    public String toPrompt() {
        String categoryLine = (category != null && !category.isBlank())
                ? String.format("장르/카테고리: %s\n", category)
                : "";
        return String.format("""
      사용자 요청: %s
      %s
      위 요청의 분위기와 상황에 정확히 맞는 곡을 추천해줘.
      분위기에 맞지 않는 유명한 곡보다, 분위기에 맞는 곡을 우선으로 추천해야해.
      한국 가수의 곡이면 노래 제목을 한국어로 알려줘.
      만약 곡 수 언급이 없으면 %d곡을 추천해줘.
      반드시 한국어로 응답하고, 아래 JSON 형식으로만 응답해. 설명이나 마크다운 없이 순수 JSON만.
      """, prompt, categoryLine,songCount);
    }
}
