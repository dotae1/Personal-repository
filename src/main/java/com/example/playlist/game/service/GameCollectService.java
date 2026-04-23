package com.example.playlist.game.service;

import com.example.playlist.game.dto.CollectResponse;
import com.example.playlist.game.dto.ItunesSearchResponse;
import com.example.playlist.game.entity.QuizTrack;
import com.example.playlist.game.repository.QuizTrackMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCollectService {

    private final Client geminiClient;
    @Qualifier("gameCollectConfig")
    private final GenerateContentConfig geminiConfig;
    private final WebClient itunesWebClient;
    private final QuizTrackMapper quizTrackMapper;
    private final ObjectMapper objectMapper;

    private static final int TARGET = 50;
    private static final int BATCH_SIZE = 15;
    private static final int MAX_GEMINI_CALLS = 10;

    // 진행 상태 메모리 저장 (decade → status)
    private final Map<String, CollectStatus> statusMap = new ConcurrentHashMap<>();

    public void collectTracksAsync(String decade) {
        CollectStatus current = statusMap.get(decade);
        if (current != null && "RUNNING".equals(current.status())) {
            throw new IllegalStateException("이미 수집이 진행 중입니다. 완료 후 다시 시도해주세요.");
        }
        statusMap.put(decade, new CollectStatus("RUNNING", 0, quizTrackMapper.countByDecade(decade)));
        CompletableFuture.runAsync(() -> collectTracks(decade));
    }

    public CollectStatus getStatus(String decade) {
        return statusMap.getOrDefault(decade,
                new CollectStatus("IDLE", 0, quizTrackMapper.countByDecade(decade)));
    }

    public int countByDecade(String decade) {
        return quizTrackMapper.countByDecade(decade);
    }

    public CollectResponse collectTracks(String decade) {
        String decadeLabel = toDecadeLabel(decade);
        int newlyCollected = 0;

        // 이번 수집 중 Gemini가 추천한 제목만 제외 목록으로 사용 (DB 전체 제목은 제외하지 않음)
        // 실제 중복 방지는 existsByItunesTrackId 에서 처리
        List<String> seenTitles = new ArrayList<>();
        log.info("[Collect] 시작 - decade={}, 기존 DB={}곡", decade, quizTrackMapper.countByDecade(decade));

        for (int attempt = 0; attempt < MAX_GEMINI_CALLS && newlyCollected < TARGET; attempt++) {
            try {
                List<SongRecommendation> songs = recommendBatchFromGemini(decadeLabel, seenTitles);
                log.info("[Collect] Gemini 추천 {}곡 - attempt={}", songs.size(), attempt + 1);

                for (SongRecommendation song : songs) {
                    if (newlyCollected >= TARGET) break;
                    seenTitles.add(song.title());

                    ItunesSearchResponse.ItunesTrack track = findOnItunes(song.searchQuery());
                    if (track == null) {
                        log.info("[Collect] iTunes 결과 없음 - title={}, query={}", song.title(), song.searchQuery());
                        continue;
                    }
                    if (track.getPreviewUrl() == null || track.getPreviewUrl().isBlank()) {
                        log.info("[Collect] iTunes preview URL 없음 - title={}, trackName={}", song.title(), track.getTrackName());
                        continue;
                    }

                    String itunesTrackId = String.valueOf(track.getTrackId());
                    if (quizTrackMapper.existsByItunesTrackId(itunesTrackId)) {
                        log.info("[Collect] 중복 스킵 - title={}", song.title());
                        continue;
                    }

                    quizTrackMapper.insert(QuizTrack.builder()
                            .title(song.title())
                            .artist(song.artist())
                            .titleAlias(track.getTrackName())    // iTunes 영어 제목
                            .artistAlias(track.getArtistName())  // iTunes 영어 아티스트명
                            .albumImageUrl(track.getArtworkUrl())
                            .previewUrl(track.getPreviewUrl())
                            .decade(decade)
                            .itunesTrackId(itunesTrackId)
                            .build());

                    newlyCollected++;
                    int total = quizTrackMapper.countByDecade(decade);
                    statusMap.put(decade, new CollectStatus("RUNNING", newlyCollected, total));
                    log.info("[Collect] 저장 [{}/{}] title={}, artist={}", newlyCollected, TARGET, song.title(), song.artist());
                }

                // iTunes rate limit 방지
                Thread.sleep(300);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("[Collect] Gemini 추천 실패 - attempt={}, error={}", attempt + 1, e.getMessage());
            }
        }

        int total = quizTrackMapper.countByDecade(decade);
        statusMap.put(decade, new CollectStatus("COMPLETED", newlyCollected, total));
        log.info("[Collect] 완료 - decade={}, 신규={}곡, DB총={}곡", decade, newlyCollected, total);
        return new CollectResponse(decade, newlyCollected, total);
    }

    private List<SongRecommendation> recommendBatchFromGemini(String decadeLabel, List<String> excludeTitles) throws Exception {
        String exclusionList = excludeTitles.isEmpty() ? "없음" : String.join(", ", excludeTitles);

        String prompt = String.format("""
                %s 한국 가요/K-pop 중 유명한 노래 %d곡을 추천해줘.
                조건:
                - 사람들이 노래만 듣고 제목을 맞출 수 있는 유명한 곡
                - 원곡만 (일본어 버전, MR버전, 리믹스, 라이브 버전 절대 제외)
                - Apple Music/iTunes에서 검색 가능한 곡
                - 아래 제목은 반드시 제외: %s
                - 아래 JSON 배열 형식으로만 응답 (설명 없이, 코드블록 없이):
                [
                  {"title": "한국어제목", "artist": "한국어아티스트명", "searchQuery": "IU Celebrity"},
                  {"title": "한국어제목2", "artist": "한국어아티스트명2", "searchQuery": "BTS Dynamite"}
                ]
                searchQuery는 반드시 영어로 작성 (아티스트 영어명 + 곡 제목). 예: "IU Celebrity", "BTS Butter", "NewJeans Hype Boy", "BLACKPINK How You Like That"
                """, decadeLabel, BATCH_SIZE, exclusionList);

        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-1.5-flash", prompt, geminiConfig);

        String json = response.text().trim()
                .replaceAll("```json", "").replaceAll("```", "").trim();

        log.info("[Collect] Gemini raw={}", json);

        JsonNode node = objectMapper.readTree(json);
        if (!node.isArray() && node.has("songs")) {
            node = node.get("songs");
        }

        List<SongRecommendation> result = new ArrayList<>();
        for (JsonNode item : node) {
            String title = item.path("title").asText(null);
            String artist = item.path("artist").asText(null);
            String searchQuery = item.path("searchQuery").asText(null);
            if (title == null || artist == null) continue;
            if (searchQuery == null) searchQuery = artist + " " + title;
            result.add(new SongRecommendation(title, artist, searchQuery));
        }
        return result;
    }

    private ItunesSearchResponse.ItunesTrack findOnItunes(String searchQuery) {
        try {
            // iTunes API가 text/javascript 콘텐츠 타입으로 응답하므로 String으로 받아 수동 파싱
            String body = itunesWebClient
                    .get()
                    .uri("/search?term={term}&country=KR&media=music&entity=song&limit=1", searchQuery)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (body == null) return null;

            ItunesSearchResponse response = objectMapper.readValue(body, ItunesSearchResponse.class);
            if (response.getResultCount() == 0 || response.getResults() == null || response.getResults().isEmpty()) {
                return null;
            }
            return response.getResults().get(0);
        } catch (Exception e) {
            log.warn("[Collect] iTunes 조회 실패 - query={}, error={}", searchQuery, e.getMessage());
            return null;
        }
    }

    private String toDecadeLabel(String decade) {
        return switch (decade) {
            case "1990_EARLY" -> "1990년대 초반(1990~1994년)";
            case "1990_LATE"  -> "1990년대 후반(1995~1999년)";
            case "2000"       -> "2000년대(2000~2009년)";
            case "2010"       -> "2010년대(2010~2019년)";
            case "2020"       -> "2020년대(2020~현재)";
            default -> throw new IllegalArgumentException("지원하지 않는 연대: " + decade);
        };
    }

    record SongRecommendation(String title, String artist, String searchQuery) {}

    public record CollectStatus(
            String status,        // IDLE / RUNNING / COMPLETED
            int newlyCollected,   // 이번 수집에서 추가된 곡 수
            int totalInDb         // DB 총 곡 수
    ) {}
}
