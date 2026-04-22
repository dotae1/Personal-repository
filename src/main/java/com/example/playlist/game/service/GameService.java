package com.example.playlist.game.service;

import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.entity.QuizTrack;
import com.example.playlist.game.repository.QuizTrackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final QuizTrackMapper quizTrackMapper;

    public QuizTrackResponse getQuizTrack(String decade) {
        QuizTrack track = quizTrackMapper.findRandomByDecade(decade);
        if (track == null) {
            throw new IllegalStateException("해당 연대의 곡이 없습니다. 먼저 수집을 진행해주세요.");
        }
        log.info("[Game] DB 조회 - title={}, artist={}", track.getTitle(), track.getArtist());
        return QuizTrackResponse.fromEntity(track);
    }
}
