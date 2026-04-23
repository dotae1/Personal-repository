package com.example.playlist.game.service;

import com.example.playlist.game.dto.QuizAnswerRequest;
import com.example.playlist.game.dto.QuizAnswerResponse;
import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.entity.QuizTrack;
import com.example.playlist.game.repository.QuizTrackMapper;
import com.example.playlist.global.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final QuizTrackMapper quizTrackMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    private static final long QUIZ_TTL_SECONDS = 5 * 60; // 5분

    public QuizTrackResponse getQuizTrack(String decade) {
        QuizTrack track = quizTrackMapper.findRandomByDecade(decade);
        if (track == null) {
            throw new IllegalStateException("해당 연대의 곡이 없습니다. 먼저 수집을 진행해주세요.");
        }

        String quizId = UUID.randomUUID().toString();
        try {
            redisUtil.setDataExpire("quiz:" + quizId, objectMapper.writeValueAsString(track), QUIZ_TTL_SECONDS);
        } catch (Exception e) {
            log.warn("[Quiz] Redis 저장 실패 - quizId={}", quizId, e);
            throw new IllegalStateException("퀴즈 생성에 실패했습니다.");
        }

        return new QuizTrackResponse(quizId, track.getPreviewUrl(), track.getAlbumImageUrl());
    }

    public QuizAnswerResponse checkAnswer(QuizAnswerRequest request) {
        String json = redisUtil.getData("quiz:" + request.quizId());
        if (json == null) {
            throw new IllegalArgumentException("만료되었거나 존재하지 않는 퀴즈입니다.");
        }

        QuizTrack track;
        try {
            track = objectMapper.readValue(json, QuizTrack.class);
        } catch (Exception e) {
            throw new IllegalStateException("퀴즈 데이터를 읽을 수 없습니다.");
        }

        String userAnswer = normalize(request.answer());
        boolean correct = userAnswer.equals(normalize(track.getTitle()))
                || (track.getTitleAlias() != null && userAnswer.equals(normalize(track.getTitleAlias())));

        return new QuizAnswerResponse(correct, track.getTitle(), track.getArtist(), track.getAlbumImageUrl());
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\[.*?\\]", "")
                .replaceAll("\\s+", "")
                .toLowerCase()
                .trim();
    }
}
